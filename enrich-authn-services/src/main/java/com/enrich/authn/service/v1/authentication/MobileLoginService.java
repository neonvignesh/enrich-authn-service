package com.enrich.authn.service.v1.authentication;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.entity.LoginAudit;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.LoginAuthnRequest;
import com.enrich.authn.pojo.LoginAuthnResponse;
import com.enrich.authn.pojo.LoginRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.TokenData;
import com.enrich.authn.pojo.TokenDataList;
import com.enrich.authn.pojo.UserToken;
import com.enrich.authn.pojo.UserTokenData;
import com.enrich.authn.service.repository.LoginAuditRepository;
import com.enrich.authn.service.repository.PartnerRepository;
import com.enrich.authn.util.Base64Util;
import com.enrich.authn.util.CryptoUtil;
import com.enrich.authn.util.FieldValidationUtil;
import com.enrich.authn.util.JwtUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.CacheUtil;
import com.enrich.authn.util.external.HydraUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MobileLoginService {

	private final CryptoUtil cryptoUtil;

	private final HydraUtil hydraUtil;
	
	private final CacheUtil cacheUtil;

	private final LoginAuditRepository loginAuditRepository;

	private final PartnerRepository partnerRepository;
	
	@Lazy
	private final ObjectMapper objectMapper = new ObjectMapper();

	
	
	
	  public StandardMessageResponse authenticate(AuthnEncryptRequest encryptedData) {
	        try {   	
	            if (isValidEncryptedData(encryptedData)) {
	            	LoginRequest loginRequest = cryptoUtil.decrypt(Base64Util.decode(encryptedData.getData()), LoginRequest.class);
	                return validateAndProcessAuthentication(loginRequest);
	            }
	            return StandardResponseUtil.prepareBadRequestResponse();
	        } catch (Exception e) {
	            log.error("Error occurred in MobileLoginService -- authenticate  method : {}", e.getMessage());
	            return StandardResponseUtil.prepareInternalServerErrorResponse();
	        }
	    }

	    private  static boolean isValidEncryptedData(AuthnEncryptRequest encryptedData) {
	        return Optional.ofNullable(encryptedData)
	                .map(data -> Validator.hasData(data.getData()))
	                .orElse(false);
	    }

	    private StandardMessageResponse validateAndProcessAuthentication(LoginRequest loginRequest) {
	        Set<ConstraintViolation<LoginRequest>> violations = FieldValidationUtil.validate(loginRequest);
	        return violations.isEmpty() ? processAuthenticationResponse(loginRequest) :
	                FieldValidationUtil.handleValidationErrors(violations);
	    }

	    private StandardMessageResponse processAuthenticationResponse(LoginRequest loginRequest) {
	        if (partnerExists(loginRequest)) {
	            try {
	                JsonObject response = hydraUtil.authenticateWithoutOtp(loginRequest);
	                return Validator.hasData(response) ?
	                        handleHydraResponse(response, loginRequest) :
	                        StandardResponseUtil.prepareBadRequestResponse();
	            } catch (Exception e) {
	                return StandardResponseUtil.prepareInternalServerErrorResponse();
	            }
	        } else {
	            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.INVALID_PARTNER));
	        }
	    }

	    private boolean partnerExists(LoginRequest loginRequest) {
	        return partnerRepository.findByPartnerCodeAndPartnerChannel(
	        		loginRequest.getPartner_code(), loginRequest.getPartner_channel()).isPresent();
	    }

	    private StandardMessageResponse handleHydraResponse(JsonObject response, LoginRequest loginRequest) {
	        JsonNode root = parseResponseToJsonNode(response);
	        return Validator.hasData(root) ?
	                handleAuthenticatedUser(root, loginRequest) :
	                StandardResponseUtil.prepareBadRequestResponse();
	    }

	    private  JsonNode parseResponseToJsonNode(JsonObject response) {
	        try {
	            return  objectMapper.readTree(response.toString());
	        } catch (JsonProcessingException e) {
	            
	            return null;
	        }
	    }

	    private StandardMessageResponse handleAuthenticatedUser(JsonNode root, LoginRequest loginRequest) {

	    	if (root.has(Constants.DATA) && root.get(Constants.DATA).has(Constants.USER_TOKEN)) {
	            String token = root.get(Constants.DATA).get(Constants.USER_TOKEN).asText();
	            String userId = root.get(Constants.DATA).get(Constants.USER_ID).asText();
	            try {
	                return createSuccessfulResponse(loginRequest, token, userId);
	            } catch (Exception e) {
	                
	                return StandardResponseUtil.prepareInternalServerErrorResponse();
	            }
	        } else {
	            return handleErrorResponse(root, loginRequest);
	        }
	    }

	    private StandardMessageResponse createSuccessfulResponse(LoginRequest loginRequest, String token, String userId) {
	        try {

	            // Create the ObjectMapper once
	            // Generate JWT token
	            String jwtToken = JwtUtil.generateMobile(loginRequest);
	            // Get existing user token data
	            UserTokenData userTokenData = getUserTokenData(loginRequest);
	            // Add token to user token data
	            String updatedJkeyResponse = addToken(userTokenData, jwtToken, token, loginRequest);
	            // Remove expired JWT tokens
	            String updatedJsonData = removeExpiryJwt(updatedJkeyResponse, loginRequest);
	            // Update the cache
	            String cacheKey = Constants.COM_ENRICH_AUTHN_API + loginRequest.getUser_id() + ":" + Constants.JWT_ARRAY;
	            addToCache(cacheKey, updatedJsonData);
	            // Save login audit entity
	            LoginAuditEntity(loginRequest);
	            // Prepare response data
	            LoginAuthnResponse.ResponseData responseData = prepareResponseData(jwtToken, userId);
	            return prepareSuccessResponse(Collections.singletonList(responseData));
	        } catch (Exception e) {
	            
	            return StandardResponseUtil.prepareInternalServerErrorResponse();
	        }
	    }

	    private void addToCache(String key, String value) {
	        cacheUtil.set(key, value, 86400);
	    }
	    
	    private String addToken(UserTokenData userTokenData, String jwtToken, String token, LoginRequest loginRequest) throws Exception {
	        UserToken userToken = createNewUserToken(jwtToken, token, loginRequest);
	        return updateTokenData(userTokenData, userToken, token);
	    }
	    private UserToken createNewUserToken(String jwtToken, String token, LoginRequest loginRequest) throws Exception {
	        Claims claims = JwtUtil.verify(jwtToken);
	        UserToken userToken = new UserToken();
	        userToken.setJwtToken(jwtToken);
	        userToken.setJkey(token);
	        userToken.setAccess_type(Constants.INTERNAL);
	        userToken.setExpiry(Long.parseLong(claims.get(Constants.EXP).toString()));
	        userToken.setPartner_code(loginRequest.getPartner_code());
	        userToken.setPartner_channel(loginRequest.getPartner_channel());
	        return userToken;
	    }

	    private String updateTokenData(UserTokenData userTokenData, UserToken userToken, String token) throws Exception {
	        ObjectMapper objectMapper = new ObjectMapper(); 
	        List<UserToken> newDataList = userTokenData.getData().stream()
	                .filter(existingToken ->
	                        !existingToken.getPartner_code().equals(userToken.getPartner_code())
	                                || !existingToken.getPartner_channel().equals(userToken.getPartner_channel()))
	                .collect(Collectors.toList());
	        newDataList.add(userToken);
	        userTokenData.setData(newDataList);
	        String updatedJsonData = userTokenData.toJson();
	        TokenDataList tokenDataList = objectMapper.readValue(updatedJsonData, TokenDataList.class);
	        TokenData[] tokenDataArray = tokenDataList.getData();
	        for (TokenData tokenData : tokenDataArray) {
	            tokenData.setJkey(token);
	            String key = Constants.COM_ENRICH_AUTHN_API + Constants.JWT + ":" + tokenData.getJwtToken();
	            cacheUtil.set(key, token, 86400);
	        }
	        return objectMapper.writeValueAsString(tokenDataList);
	    }
	    
	    private UserTokenData getUserTokenData(LoginRequest loginRequest) throws JsonProcessingException {
	        Object redisData = cacheUtil.get(Constants.COM_ENRICH_AUTHN_API + loginRequest.getUser_id() + ":" + Constants.JWT_ARRAY);
	        return Validator.hasData(redisData) ? UserTokenData.fromJson(redisData.toString()) : new UserTokenData();
	    }

	    private void LoginAuditEntity(LoginRequest loginRequest) {
	        saveLoginAudit(loginRequest, Constants.SUCCESS_LOGIN);
	    }

	    private static StandardMessageResponse prepareSuccessResponse(List<LoginAuthnResponse.ResponseData> responseDataList) {
	        return StandardResponseUtil.prepareSuccessListResponse(responseDataList);
	    }

	    private static  LoginAuthnResponse.ResponseData prepareResponseData(String encodedData, String userId) {
	        LoginAuthnResponse.ResponseData responseData = new LoginAuthnResponse.ResponseData();
	        responseData.setMessage(Constants.SUCCESS_LOGIN);
	        responseData.setX_authorization(encodedData);
	        responseData.setUser_id(userId);
	        return responseData;
	    }

	    private static String removeExpiryJwt(String updatedjkeyRespone, LoginRequest loginRequest) throws JsonMappingException, JsonProcessingException {
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonNode = objectMapper.readTree(updatedjkeyRespone);
	        JsonNode dataArray = jsonNode.get(Constants.DATA);
	        long currentTime = Instant.now().getEpochSecond();
	        Iterator<JsonNode> iterator = dataArray.iterator();

	        while (iterator.hasNext()) {
	            JsonNode item = iterator.next();
	            if (item.has(Constants.EXPIRY) && item.get(Constants.EXPIRY).asLong() < currentTime) {
	                iterator.remove();
	            }
	        }
	        return objectMapper.writeValueAsString(jsonNode);
	    }

	    private StandardMessageResponse handleErrorResponse(JsonNode root,  LoginRequest loginRequest) {
	        String errorMessage = root.get(Constants.DATA).get(Constants.ERROR_MEESAGE).asText();
	        saveLoginAudit(loginRequest, errorMessage);

	        if (errorMessage.equals(Constants.INVALID_BLOCK)) {
	            return StandardResponseUtil.prepareForbiddenResponse(MessageUtil.getErrorMessage(errorMessage));
	        } else if (errorMessage.equals(Constants.INVALID_WRONG)) {
	            return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(errorMessage));
	        } else {
	            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(errorMessage));
	        }
	    }

	    private  LoginAudit saveLoginAudit(LoginRequest loginRequest, String remarks) {

	        LoginAudit loginAudit = new LoginAudit();
	        loginAudit.setIpAddress(loginRequest.getIp_details());
	        loginAudit.setMacAddress(loginRequest.getMac_address());
	        loginAudit.setRemarks(remarks);
	        loginAudit.setInOutTime(LocalDateTime.now());
	        loginAudit.setSessionId(UUID.randomUUID().toString());
	        loginAudit.setUserAccessType(loginRequest.getPartner_code());
	        loginAudit.setPartnerChannel(loginRequest.getPartner_channel());
	        loginAudit.setPartnerCode(loginRequest.getPartner_code());
	        loginAudit.setUserId(loginRequest.getUser_id());
	        return loginAuditRepository.save(loginAudit);
	    }

}
