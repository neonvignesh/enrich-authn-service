package com.enrich.authn.service.v1.authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.GenerateOtpRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.util.Base64Util;
import com.enrich.authn.util.CryptoUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.HydraUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateOtpService {


	private final  	HydraUtil hydraUtil;


	private  final CryptoUtil cryptoUtil;
	
	private final Map<String, Long> userLastRequestTimestamps = new ConcurrentHashMap<>();

	public StandardMessageResponse generateOtp(AuthnEncryptRequest authnEncryptRequest) {
	    try {
	        if (Validator.hasData(authnEncryptRequest)) {
	            GenerateOtpRequest generateOtpRequestPayload = parseGenerateOtpRequest(authnEncryptRequest.getData());
	            long currentTime = System.currentTimeMillis();
	            if (isRateLimited(generateOtpRequestPayload.getUser_id(), currentTime)) return StandardResponseUtil.prepareTooManyRequestResponse();
	            JsonObject otpGenerationOutput = hydraUtil.generateOtp(generateOtpRequestPayload);
	            if (Validator.hasData(otpGenerationOutput)) {
	                return handleOtpGenerationResponse(otpGenerationOutput, generateOtpRequestPayload, currentTime);
	            }
	        }
	        return StandardResponseUtil.prepareBadRequestResponse();
	    } catch (Exception e) {
	    	 log.error("Error occurred in GenerateOtpService  -- generateOtp method: {}", e.getMessage());
	        return StandardResponseUtil.prepareInternalServerErrorResponse();
	    }
	}

	private GenerateOtpRequest parseGenerateOtpRequest(String data) throws Exception {
	    return cryptoUtil.decrypt(Base64Util.decode(data), GenerateOtpRequest.class);
	}

	private boolean isRateLimited(String userId, long currentTime) {
	    if (userLastRequestTimestamps.containsKey(userId)) {
	        long lastTimestamp = userLastRequestTimestamps.get(userId);
	        if (currentTime - lastTimestamp < 60000) {
	            // Less than a minute has passed since the last successful request.
	            return true;
	        }
	    }
	    return false;
	}

	private StandardMessageResponse handleOtpGenerationResponse(JsonObject otpGenerationOutput, GenerateOtpRequest generateOtpRequestPayload, long currentTime) throws Exception {
	    JsonNode root = new ObjectMapper().readTree(otpGenerationOutput.toString());
	    if (Validator.hasData(root) && root.has(Constants.DATA) && root.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.OTP_GENERATION_SUCCESS)) {
	        userLastRequestTimestamps.put(generateOtpRequestPayload.getUser_id(), currentTime);
	        return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessage(root.get(Constants.DATA).get(Constants.STATUS).asText()));
	    } else {
	        return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.FAILED_TO_GENERATE_OTP));
	    }
	}

}
