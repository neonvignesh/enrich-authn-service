package com.enrich.authn.service.v1.authentication;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.exception.AccessDeniedException;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.ChangePasswordRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.util.JwtUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.RSAUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.CacheUtil;
import com.enrich.authn.util.external.HydraUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

@Service
public class TotpService {

	@Autowired
	HydraUtil hydraUtil;
	
	@Autowired
	private  CacheUtil cacheUtil;
	
	
	public ResponseEntity<?> generateQr(String userId, String jwtToken) {
	    try {
	        validateJwtToken(jwtToken);
	        String jwtKey = Constants.COM_ENRICH_AUTHN_API +Constants.JWT+":"+ jwtToken;
	        Object cachedData = cacheUtil.get(jwtKey);
	        if (cachedData != null) {
	            String response = hydraUtil.enableTOTP(userId, cachedData.toString());
	            return handleEnableTOTPResponse(response);
	        }
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.INVALID_TOKEN)));
	    } catch (ExpiredJwtException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.JWT_EXPIRED)));
	    } catch (AccessDeniedException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(e.getMessage())));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(StandardResponseUtil.prepareInternalServerErrorResponse());
	    }
	}

	private void validateJwtToken(String jwtToken) {
	    if (!Validator.hasData(jwtToken)) {
	        ResponseEntity.badRequest().body(StandardResponseUtil.prepareBadRequestResponseList(
	                MessageUtil.getErrorMessage(Constants.INVALID_JWTTOKEN)));
	    }
	    JwtUtil.verify(jwtToken);
	}

	private ResponseEntity<?> handleEnableTOTPResponse(String response) throws IOException {
	    if (Validator.hasData(response)) {
	        if (response.contains(Constants.DATA_IMG)) {
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        }
	        JsonNode root = new ObjectMapper().readTree(response);
	        if (Validator.hasData(root) && root.has(Constants.DATA)
	                && root.get(Constants.DATA).has(Constants.STATUS) &&
	                root.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.NOT_OK)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.INVALID_TOKEN)));
	        }
	    }
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponseUtil.prepareBadRequestResponse());
	}	
	
}
