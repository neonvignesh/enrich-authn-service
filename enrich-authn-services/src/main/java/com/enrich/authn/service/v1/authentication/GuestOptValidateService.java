package com.enrich.authn.service.v1.authentication;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.entity.GuestActivity;
import com.enrich.authn.pojo.GuestOtpValidateRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.response.GuestOtpValidationResponse;
import com.enrich.authn.service.repository.GuestActivityRepository;
import com.enrich.authn.util.JwtUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.CacheUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuestOptValidateService {

	
	private final GuestActivityRepository guestActivityRepo;

	
	private final CacheUtil cacheUtil;

	public StandardMessageResponse guestOtpValidate(GuestOtpValidateRequest requestPojo) {
		try {
			if (!isValidRequest(requestPojo))
				return prepareInvalidInputResponse(requestPojo);
			GuestActivity guestDetails = getGuestDetails(requestPojo.getUserId());
			if (!Validator.hasData(guestDetails)) {
				log.error("User Id not found in our DB ");
				return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_USER_ID);
			}
			return getOtpValidationResult(requestPojo, guestDetails);
		} catch (Exception e) {
			log.error("Error Occurred in Guest otp Validate Service: " + e.getMessage());
			return StandardResponseUtil.prepareInternalServerErrorResponse(e.getLocalizedMessage());
		}
	}

	private StandardMessageResponse getOtpValidationResult(GuestOtpValidateRequest requestPojo,
			GuestActivity guestDetails) {
		if (isOtpExpired(guestDetails.getExpiryDate())) {
			log.error("OTP expired");
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.OTP_EXPIRED);
		}
		if (isOtpAlreadyVerified(guestDetails.getStatus())) {
			log.error("OTP for this user is already validated. ");
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.OTP_ALREADY_VALIDATED);
		}
		return processOtpValidation(requestPojo, guestDetails);
	}

	private GuestActivity getGuestDetails(String userId) {
		return guestActivityRepo.findByGuestId(userId);
	}

	private boolean isOtpAlreadyVerified(String status) {
		return status.equals(Constants.VERIFIED);
	}

	private boolean isOtpExpired(Timestamp expiryTime) {
		Instant currentTimestamp = Instant.now();
		Instant expiryInstant = expiryTime.toInstant();
		return currentTimestamp.isAfter(expiryInstant);
	}

	private StandardMessageResponse processOtpValidation(GuestOtpValidateRequest requestPojo,
			GuestActivity guestDetails) {
		if (requestPojo.getOtp().equalsIgnoreCase(guestDetails.getOtp())) {
			return handleValidOtp(requestPojo, guestDetails);
		} else {
			return handleInvalidOtp(guestDetails);
		}
	}

	private StandardMessageResponse handleValidOtp(GuestOtpValidateRequest requestPojo, GuestActivity guestDetails) {
		GuestOtpValidationResponse response = new GuestOtpValidationResponse();
		String guestId = guestDetails.getGuestId();
		String contextType = guestDetails.getContextType();
		String jwt = generateNewAccessToken(contextType, guestDetails.getGuestId());
		response.setMessage(Constants.OTP_VERIFIED);
		response.setXAuthorization(jwt);
		response.setUserId(guestId);
		guestDetails.setStatus(Constants.VERIFIED);
		guestActivityRepo.save(guestDetails);
		getAccessToken(guestId, jwt, contextType);
		return StandardResponseUtil.prepareSuccessListResponse(Collections.singletonList(response));
	}

	private StandardMessageResponse handleInvalidOtp(GuestActivity guestDetails) {
		log.error("Invalid OTP");
		guestDetails.setStatus(Constants.NOT_VERIFIED);
		guestActivityRepo.save(guestDetails);
		return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_OTP);
	}

	private boolean isValidRequest(GuestOtpValidateRequest requestPojo) {
		return Validator.hasData(requestPojo.getOtp()) && Validator.hasData(requestPojo.getUserId());
	}

	private StandardMessageResponse prepareInvalidInputResponse(GuestOtpValidateRequest pojo) {
		if (!Validator.hasData(pojo.getUserId()))
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_USER_ID);
		else if (!Validator.hasData(pojo.getOtp()))
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_OTP);
		else
			return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_INPUT);
	}

	private String generateNewAccessToken(String partnerChannel, String userId) {
		Instant now = Instant.now();
		Instant expiration = now.plus(1, ChronoUnit.DAYS);
		String newAccessToken = Jwts.builder().claim(Constants.PARTNER_CODE, Constants.ENRICH)
				.claim(Constants.PARTNER_CHANNEL, partnerChannel).claim(Constants.USER_ID, userId)
				.setIssuedAt(Date.from(now)).setExpiration(Date.from(expiration))
				.signWith(SignatureAlgorithm.HS256, Constants.SECRET_KEY).compact();
		return newAccessToken;
	}

	private void getAccessToken(String userId, String jwt, String partnerChannel) {
		Gson gson = new Gson();
		JsonArray jsonArray = new JsonArray();
		JsonObject redisObj = new JsonObject();
		boolean accessTokenUpdated = false;
		String cacheKey = Constants.COM_ENRICH_AUTHN_API + userId + ":" + Constants.JWT_ARRAY;
		Object existingCache = cacheUtil.get(cacheKey);
		if (Validator.hasData(existingCache)) {
			accessTokenUpdated = addExistingCache(jwt, partnerChannel, gson, jsonArray, accessTokenUpdated,
					existingCache);
		}
		if (!accessTokenUpdated) {
			addNewRedisCache(jwt, partnerChannel, jsonArray);
		}
		redisObj.add(Constants.DATA, jsonArray);
		cacheUtil.set(cacheKey, redisObj.toString(), 380000);
	}

	private void addNewRedisCache(String jwt, String partnerChannel, JsonArray jsonArray) {
		JsonObject newObject = new JsonObject();
		setCacheData(jwt, partnerChannel, newObject);
		jsonArray.add(newObject);
	}

	private boolean addExistingCache(String jwt, String partnerChannel, Gson gson, JsonArray jsonArray,
			boolean accessTokenUpdated, Object existingCache) {
		JsonObject existingObj = gson.fromJson(existingCache.toString(), JsonObject.class);
		JsonArray dataArray = existingObj.getAsJsonArray(Constants.DATA);
		for (JsonElement element : dataArray) {
			JsonObject dataObject = element.getAsJsonObject();
			if (Constants.GUEST_TOKEN.equals(dataObject.get(Constants.ACCESS_TYPE).getAsString())) {
				setCacheData(jwt, partnerChannel, dataObject);
				accessTokenUpdated = true;
			}
			jsonArray.add(dataObject);
		}
		return accessTokenUpdated;
	}

	private void setCacheData(String jwt, String partnerChannel, JsonObject jsonObj) {
		Claims jwtClaims = JwtUtil.verify(jwt);
		Long expiry = Long.parseLong(jwtClaims.get(Constants.EXP).toString());
		jsonObj.addProperty(Constants.JWT_TOKEN, jwt);
		jsonObj.addProperty(Constants.JKEY, Constants.NO_JKEY_FOR_GUEST);
		jsonObj.addProperty(Constants.EXPIRY, expiry);
		jsonObj.addProperty(Constants.PARTNER_CODE, Constants.ENRICH);
		jsonObj.addProperty(Constants.PARTNER_CHANNEL, partnerChannel);
		jsonObj.addProperty(Constants.ACCESS_TYPE, Constants.GUEST_TOKEN);
	}

}
