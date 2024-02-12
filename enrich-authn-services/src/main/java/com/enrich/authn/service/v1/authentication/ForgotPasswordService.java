package com.enrich.authn.service.v1.authentication;


import java.util.Set;
import javax.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.ForgotPasswordRequest;
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
public class ForgotPasswordService {
	@Autowired
	private HydraUtil hydraUtil;

	@Autowired
	private CryptoUtil cryptoUtil;

	public StandardMessageResponse forgotPassword(AuthnEncryptRequest authnEncryptRequest) {
	    try {
	    	if (Validator.hasData(authnEncryptRequest.getData())) {
	        ForgotPasswordRequest forgotPasswordRequest = cryptoUtil.decrypt(Base64Util.decode(authnEncryptRequest.getData()), ForgotPasswordRequest.class);
	        Set<ConstraintViolation<ForgotPasswordRequest>> violations = FieldValidationUtil.validate(forgotPasswordRequest);
	        if (!violations.isEmpty())return FieldValidationUtil.handleValidationErrors(violations);  
	        JsonObject forgotOutput = hydraUtil.forgotPassword(forgotPasswordRequest);
	        if (Validator.hasData(forgotOutput)) {
	            JsonNode root = new ObjectMapper().readTree(forgotOutput.toString());
	            if (Validator.hasData(root) && root.has(Constants.DATA) && root.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.OK))
	                return createSuccessfulResponse();
	            else return handleErrorResponse(root);
	        } else return StandardResponseUtil.prepareBadRequestResponse();
	    	}
	    	return StandardResponseUtil.prepareBadRequestResponse();
	    } catch (Exception e) {
	    	 log.error("Error occurred in ForgotPasswordService  -- forgotPassword method: {}", e.getMessage());
	        return StandardResponseUtil.prepareInternalServerErrorResponse();
	    }
	}
	
	private StandardMessageResponse createSuccessfulResponse() throws Exception {
	    return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessage(Constants.FORGOT_PASSWORD_SUCCESS_MSG));
	}
	
	private StandardMessageResponse handleErrorResponse(JsonNode root) throws Exception {
	    return StandardResponseUtil.prepareBadRequestResponseList(
	            MessageUtil.getErrorMessage(root.get(Constants.DATA).get(Constants.ERROR_MEESAGE).asText()));
	}
}