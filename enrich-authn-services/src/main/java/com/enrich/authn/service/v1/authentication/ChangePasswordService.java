package com.enrich.authn.service.v1.authentication;

import java.util.Set;

import javax.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.ChangePasswordRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.util.Base64Util;
import com.enrich.authn.util.CryptoUtil;
import com.enrich.authn.util.FieldValidationUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.HydraUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChangePasswordService {

	@Autowired
	private HydraUtil hydraUtil;

	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	com.enrich.authn.provider.AuthnServiceProvider AuthnServiceProvider;

	public StandardMessageResponse changePassword(AuthnEncryptRequest authnEncryptRequest) {
	    try {
	        if (!Validator.hasData(authnEncryptRequest.getData()))return StandardResponseUtil.prepareBadRequestResponse();
	        ChangePasswordRequest changePasswordRequest = cryptoUtil.decrypt(Base64Util.decode(authnEncryptRequest.getData()), ChangePasswordRequest.class);
	        Set<ConstraintViolation<ChangePasswordRequest>> violations = FieldValidationUtil.validate(changePasswordRequest);
	        if (!violations.isEmpty())return FieldValidationUtil.handleValidationErrors(violations);      
	        if (Validator.hasData(changePasswordRequest)) return processChangePasswordRequest(changePasswordRequest);
	        return StandardResponseUtil.prepareBadRequestResponse();
	    } catch (Exception e) {
	    	 log.error("Error occurred in ChangePasswordService  -- changePassword method: {}", e.getMessage());
	        return StandardResponseUtil.prepareInternalServerErrorResponse();
	    }
	}

	private StandardMessageResponse processChangePasswordRequest(ChangePasswordRequest changePasswordRequest) throws Exception {
	    ObjectMapper objectMapper = new ObjectMapper();
	    JsonObject forgotOutput = hydraUtil.changePassword(changePasswordRequest);
	    if (Validator.hasData(forgotOutput)) {
	        JsonNode root = objectMapper.readTree(forgotOutput.toString());
	        if (Validator.hasData(root) && root.has(Constants.DATA)
	                && root.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.OK)) {
	            return prepareSuccessResponse(root);
	        } else {
	            return handleErrorResponse(root);
	        }
	    }
	    return StandardResponseUtil.prepareBadRequestResponse();
	}
	
	private StandardMessageResponse prepareSuccessResponse(JsonNode root) throws Exception {
	    return StandardResponseUtil.prepareSuccessListResponse(MessageUtil
	            .getMessage(root.get(Constants.DATA).get(Constants.EXPIRY_MESSAGE).asText()));
	}

	private StandardMessageResponse handleErrorResponse(JsonNode root) throws Exception {
	    return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(root.get(Constants.DATA).get(Constants.ERROR_MEESAGE).asText()));
	}


}
