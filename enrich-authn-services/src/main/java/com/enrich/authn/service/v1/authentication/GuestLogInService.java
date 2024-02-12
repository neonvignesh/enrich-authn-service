package com.enrich.authn.service.v1.authentication;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.entity.GuestAccounts;
import com.enrich.authn.entity.GuestActivity;
import com.enrich.authn.pojo.GuestOtpRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.response.GuestLogInResponse;
import com.enrich.authn.service.repository.GuestAccountRepository;
import com.enrich.authn.service.repository.GuestActivityRepository;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.EmailSenderUtil;
import com.enrich.authn.util.external.SmsSenderUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GuestLogInService {

	@Autowired
	private SmsSenderUtil smsSender;

	@Autowired
	private EmailSenderUtil emailSender;

	@Autowired
	private GuestAccountRepository guestAccountRepo;

	@Autowired
	private GuestActivityRepository guestActivityRepo;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Random random = new Random();
	private final Map<String, Long> userLastRequestTimestamps = new ConcurrentHashMap<>();
	private static final long MINIMUM_OTP_RESEND_INTERVAL = 60000; // 60 seconds in milliseconds

	public StandardMessageResponse guestLogin(GuestOtpRequest requestPojo) {
		try {
			if (!isValidRequest(requestPojo))
				return prepareInvalidInputResponse(requestPojo);
			if (!requestPojo.getContextType().equals(Constants.GUEST_MOB)
					&& !requestPojo.getContextType().equals(Constants.GUEST_WEB)) {
				log.error("Context type should be GUESTMOB or GUESTWEB");
				return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_CONTEXT_TYPE);
			}
			String emailId = Optional.ofNullable(requestPojo.getEmailId()).orElse(null);
			if (Validator.hasData(emailId) && !isValidEmail(emailId))
				return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_EMAILID_FORMAT);
			Long mobileNumber = Optional.ofNullable(requestPojo.getMobileNumber()).orElse(null);
			if (Validator.hasData(mobileNumber) && Validator.hasData(emailId))
				return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.ONLY_ONE_INPUT);
			return getResponse(requestPojo, emailId, mobileNumber);
		} catch (Exception e) {
			log.error("Error Occurred in Guest Login Service: " + e.getMessage());
			return StandardResponseUtil.prepareInternalServerErrorResponse(e.getLocalizedMessage());
		}
	}

	private StandardMessageResponse getResponse(GuestOtpRequest requestPojo, String emailId, Long mobileNumber)
			throws JsonProcessingException {
		String guestContext = (mobileNumber != null) ? mobileNumber.toString() : emailId;
		long currentTime = System.currentTimeMillis();
		GuestLogInResponse responseObj = new GuestLogInResponse();
		if (isRequestRateLimited(guestContext, currentTime))
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.TOO_MANY_REQUEST);
		if (Validator.hasData(mobileNumber)) {
			handleMobileLogin(requestPojo, mobileNumber, guestContext, currentTime, responseObj);
		} else if (Validator.hasData(emailId)) {
			handleEmailLogin(requestPojo, emailId, guestContext, currentTime, responseObj);
		}
		return StandardResponseUtil.prepareSuccessListResponse(Collections.singletonList(responseObj));
	}

	private boolean isRequestRateLimited(String guestContext, long currentTime) {
		if (userLastRequestTimestamps.containsKey(guestContext)) {
			long lastRequestTimestamp = userLastRequestTimestamps.get(guestContext);
			return currentTime - lastRequestTimestamp < MINIMUM_OTP_RESEND_INTERVAL;
		}
		return false;
	}

	private void handleMobileLogin(GuestOtpRequest requestPojo, Long mobileNumber, String guestContext,
			long currentTime, GuestLogInResponse responseObj) throws JsonProcessingException {
		int randomOtp = generateRandomOtp();
		String responseBody = smsSender.sendSms(mobileNumber, randomOtp).getBody();
		JsonNode responseNode = parseJsonResponse(responseBody);
		if (Constants.OK.equalsIgnoreCase(responseNode.get(Constants.STATUS).asText())) {
			String uuid = generateUuid();
			String otpId = responseNode.get(Constants.DATA).get(0).get(Constants.ID).asText();
			setResponse(requestPojo, mobileNumber.toString(), currentTime, responseObj, randomOtp, uuid, otpId);
		} else {
			log.error("Error occurs with Sms Sender Vendor API: " + responseBody);
			responseObj.setMessage(responseNode.get(Constants.MESSAGE).asText());
		}
	}

	private void handleEmailLogin(GuestOtpRequest requestPojo, String emailId, String guestContext, long currentTime,
			GuestLogInResponse responseObj) throws JsonProcessingException {
		int randomOtp = generateRandomOtp();
		String responseBody = emailSender.sendSms(emailId, randomOtp).getBody();
		JsonNode responseNode = parseJsonResponse(responseBody);
		if (Constants.SUCCESS.equalsIgnoreCase(responseNode.get(Constants.SYSTEM_MESSAGE_TYPE).asText())) {
			String uuid = generateUuid();
			String otpId = generateUuid();
			setResponse(requestPojo, emailId, currentTime, responseObj, randomOtp, uuid, otpId);
		} else {
			log.error("Error occurs with Email Sender External API: " + responseBody);
			responseObj.setMessage(responseNode.get(Constants.MESSAGE).asText());
		}
	}

	private void setResponse(GuestOtpRequest requestPojo, String contextId, long currentTime,
			GuestLogInResponse responseObj, int randomOtp, String uuid, String otpId) {
		GuestAccounts checkGuestHasAccount = guestAccountRepo.findByCustomerContextId(contextId);
		if (!Validator.hasData(checkGuestHasAccount)) {
			GuestAccounts guestAccounts = new GuestAccounts();
			createGuestAccount(guestAccounts, requestPojo, uuid, contextId);
			GuestAccounts savedGuest = guestAccountRepo.save(guestAccounts);
			createGuestActivity(savedGuest, uuid, otpId, randomOtp);
			responseObj.setUserId(uuid);
		} else {
			String guestId = checkGuestHasAccount.getGuestId();
			updateGuestActivity(guestId, checkGuestHasAccount.getCustomerContextId(), otpId, randomOtp);
			responseObj.setUserId(guestId);
		}
		userLastRequestTimestamps.put(contextId, currentTime);
		responseObj.setMessage(Constants.OTP_SENT_SUCCESSFULLY);
	}

	private boolean isValidRequest(GuestOtpRequest requestPojo) {
		return Validator.hasData(requestPojo.getContextType())
				&& (Validator.hasData(requestPojo.getEmailId()) || Validator.hasData(requestPojo.getMobileNumber()));
	}

	private StandardMessageResponse prepareInvalidInputResponse(GuestOtpRequest pojo) {
		if (!Validator.hasData(pojo.getContextType()))
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_CONTEXT_TYPE);
		else if (!Validator.hasData(pojo.getMobileNumber()) && !Validator.hasData(pojo.getEmailId()))
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_MOBILE_OR_EMAIL);
		else
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_INPUT);
	}

	private JsonNode parseJsonResponse(String responseBody) throws JsonProcessingException {
		return objectMapper.readTree(responseBody);
	}

	private void createGuestAccount(GuestAccounts guestAccounts, GuestOtpRequest requestPojo, String uuid,
			String contextId) {
		Instant expirationInstant = Instant.now().plus(3, ChronoUnit.DAYS);
		Timestamp expirationDate = Timestamp.from(expirationInstant);
		guestAccounts.setContextType(requestPojo.getContextType());
		guestAccounts.setCustomerContextId(contextId);
		guestAccounts.setExpirationDate(expirationDate);
		guestAccounts.setGuestId(uuid);

	}

	private void createGuestActivity(GuestAccounts guestAccounts, String uuid, String otpId, int otp) {
		Instant expirationInstant = Instant.now().plus(5, ChronoUnit.MINUTES);
		Timestamp expirationDate = Timestamp.from(expirationInstant);
		GuestActivity guestActivity = new GuestActivity();
		guestActivity.setCustomerContextId(guestAccounts.getCustomerContextId());
		guestActivity.setExpiryDate(expirationDate);
		guestActivity.setGuestId(guestAccounts.getGuestId());
		guestActivity.setContextType(guestAccounts.getContextType());
		guestActivity.setStatus(Constants.SENT);
		guestActivity.setOtp(Integer.toString(otp));
		guestActivity.setOtpId(otpId);
		guestActivityRepo.save(guestActivity);
	}

	private void updateGuestActivity(String guestId, String customerContextId, String otpId, int otp) {
		Instant currentTimestamp = Instant.now();
		Instant expirationInstant = currentTimestamp.plus(5, ChronoUnit.MINUTES);
		Timestamp expiryDate = Timestamp.from(expirationInstant);
		guestActivityRepo.updateGuestActivity(guestId, customerContextId, Integer.toString(otp), otpId, Constants.SENT,
				expiryDate);
	}

	private int generateRandomOtp() {
		return 100000 + random.nextInt(900000);
	}

	private String generateUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "").chars().filter(Character::isLetterOrDigit)
				.limit(15 + (int) (Math.random() * 4))
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
	}

	private boolean isValidEmail(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
