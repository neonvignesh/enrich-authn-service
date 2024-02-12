package com.enrich.authn.service.v1.authentication;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.LoginAuthnRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.ValidateOtpRequest;
import com.enrich.authn.util.Base64Util;
import com.enrich.authn.util.CryptoUtil;
import com.enrich.authn.util.FieldValidationUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.external.CacheUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidateOtpService {
	
	@Autowired
	CryptoUtil cryptoUtil;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	CacheUtil cacheUtil;
	
	public StandardMessageResponse validateOtp(AuthnEncryptRequest encryptedData) {
	    try {
	    	
	        ValidateOtpRequest validateOtpRequest = cryptoUtil.decrypt(Base64Util.decode(encryptedData.getData()), ValidateOtpRequest.class);
	        Set<ConstraintViolation<ValidateOtpRequest>> violations = FieldValidationUtil.validate(validateOtpRequest);
	        if (!violations.isEmpty()) {
	            return FieldValidationUtil.handleValidationErrors(violations);
	        }
	        String user_id = validateOtpRequest.getUser_id();
	        String redisKey = Constants.USER_CREDENTIALS + user_id;
	        Object value = cacheUtil.get(redisKey);
	        if (value == null) {
	            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.SESSION_TIMEOUT));
	        }
	        return processAuthentication(validateOtpRequest, value);

	    } catch (Exception e) {
	        log.error("Error occurred in validateOtp Service -- validateOtp method : {}", e.getMessage());
	        return StandardResponseUtil.prepareInternalServerErrorResponse();
	    }
	}

	private StandardMessageResponse processAuthentication(ValidateOtpRequest validateOtpRequest, Object value) {
	        Gson gson = new Gson();
	        JsonObject redisObject = gson.fromJson(value.toString(), JsonObject.class);
	        LoginAuthnRequest loginAuthnRequest = buildLoginAuthnRequest(validateOtpRequest, redisObject);
	        return authenticationService.validateAndProcessAuthentication(loginAuthnRequest);

	}

	private LoginAuthnRequest buildLoginAuthnRequest(ValidateOtpRequest validateOtpRequest, JsonObject redisObject) {
	    LoginAuthnRequest loginAuthnRequest = new LoginAuthnRequest();
	    loginAuthnRequest.setUser_id(validateOtpRequest.getUser_id());
	    loginAuthnRequest.setPassword(redisObject.get(Constants.PASSWORD).getAsString());
	    loginAuthnRequest.setOtp(validateOtpRequest.getOtp());
	    loginAuthnRequest.setIp_details(Constants.MAC_ADDRESS);
	    loginAuthnRequest.setMac_address(Constants.MAC_ADDRESS);
	    loginAuthnRequest.setPartner_channel(redisObject.get(Constants.PARTNER_CHANNEL).getAsString());
	    loginAuthnRequest.setPartner_code(redisObject.get(Constants.PARTNER_CODE).getAsString());
	    return loginAuthnRequest;
	}
	
}
