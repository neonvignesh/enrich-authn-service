package com.enrich.authn.service.v1.authentication;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.entity.LoginAudit;
import com.enrich.authn.exception.AccessDeniedException;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.LoginAuthnRequest;
import com.enrich.authn.pojo.LoginAuthnResponse;
import com.enrich.authn.pojo.LogoutRequest;
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
import com.enrich.authn.util.JsonParseUtil;
import com.enrich.authn.util.JwtExecptionUtil;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

	
	private final CryptoUtil cryptoUtil;
	
	private final HydraUtil hydraUtil;
	
	private final CacheUtil cacheUtil;

	private final LoginAuditRepository loginAuditRepository;

	private final PartnerRepository partnerRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	
	  public StandardMessageResponse authenticate(AuthnEncryptRequest encryptedData) {
	        try {   	
	            if (isValidEncryptedData(encryptedData)) {
	                LoginAuthnRequest loginAuthnRequest = cryptoUtil.decrypt(Base64Util.decode(encryptedData.getData()), LoginAuthnRequest.class);
	                return validateAndProcessAuthentication(loginAuthnRequest);
	            }
	            return StandardResponseUtil.prepareBadRequestResponse();
	        } catch (Exception e) {
	            log.error("Error occurred in authenticate Service -- authenticate method: {}", e.getMessage());
	            return StandardResponseUtil.prepareInternalServerErrorResponse();
	        }
	    }

	    private  static boolean isValidEncryptedData(AuthnEncryptRequest encryptedData) {
	        return Optional.ofNullable(encryptedData)
	                .map(data -> Validator.hasData(data.getData()))
	                .orElse(false);
	    }

	    public StandardMessageResponse validateAndProcessAuthentication(LoginAuthnRequest loginAuthnRequest) {
	        Set<ConstraintViolation<LoginAuthnRequest>> violations = FieldValidationUtil.validate(loginAuthnRequest);
	        return violations.isEmpty() ? processAuthenticationResponse(loginAuthnRequest) :
	                FieldValidationUtil.handleValidationErrors(violations);
	    }

	    private StandardMessageResponse processAuthenticationResponse(LoginAuthnRequest loginAuthnRequest) {
	        if (partnerExists(loginAuthnRequest)) {
	            try {
	                JsonObject response = hydraUtil.authenticate(loginAuthnRequest);
	                return Validator.hasData(response) ?
	                        handleHydraResponse(response, loginAuthnRequest) :
	                        StandardResponseUtil.prepareBadRequestResponse();
	            } catch (Exception e) { 
	                return StandardResponseUtil.prepareInternalServerErrorResponse();
	            }
	        } else {
	            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.INVALID_PARTNER));
	        }
	    }

	    private boolean partnerExists(LoginAuthnRequest loginAuthnRequest) {
	        return partnerRepository.findByPartnerCodeAndPartnerChannel(
	                loginAuthnRequest.getPartner_code(), loginAuthnRequest.getPartner_channel()).isPresent();
	    }

	    private StandardMessageResponse handleHydraResponse(JsonObject response, LoginAuthnRequest loginAuthnRequest) {
	        JsonNode root = JsonParseUtil.parseResponseToJsonNode(response);
	        return Validator.hasData(root) ?
	                handleAuthenticatedUser(root, loginAuthnRequest) :
	                StandardResponseUtil.prepareBadRequestResponse();
	    }

	    private StandardMessageResponse handleAuthenticatedUser(JsonNode root, LoginAuthnRequest loginAuthnRequest) {

	    	if (root.has(Constants.DATA) && root.get(Constants.DATA).has(Constants.USER_TOKEN)) {
	            String token = root.get(Constants.DATA).get(Constants.USER_TOKEN).asText();
	            String userId = root.get(Constants.DATA).get(Constants.USER_ID).asText();
	                return createSuccessfulResponse(loginAuthnRequest, token, userId);  
	        } else {
	            return handleErrorResponse(root, loginAuthnRequest);
	        }
	    }

	    private StandardMessageResponse createSuccessfulResponse(LoginAuthnRequest loginAuthnRequest, String token, String userId) {
	        try {

	            // Create the ObjectMapper once
	            // Generate JWT token
	            String jwtToken = JwtUtil.generateJwt(loginAuthnRequest);
	            // Get existing user token data
	            UserTokenData userTokenData = getUserTokenData(loginAuthnRequest);
	            // Add token to user token data
	            String updatedJkeyResponse = addToken(userTokenData, jwtToken, token, loginAuthnRequest);
	            // Remove expired JWT tokens
	            String updatedJsonData = removeExpiredJwt(updatedJkeyResponse, loginAuthnRequest);
	            // Update the cache
	            String cacheKey = Constants.COM_ENRICH_AUTHN_API + loginAuthnRequest.getUser_id() + ":" + Constants.JWT_ARRAY;
	            addToCache(cacheKey, updatedJsonData);
	            // Save login audit entity
	            loginAuditEntity(loginAuthnRequest);
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
	    
	    private String addToken(UserTokenData userTokenData, String jwtToken, String token, LoginAuthnRequest loginAuthnRequest) throws Exception {
	        UserToken userToken = createNewUserToken(jwtToken, token, loginAuthnRequest);
	        return updateTokenData(userTokenData, userToken, token);
	    }
	    private UserToken createNewUserToken(String jwtToken, String token, LoginAuthnRequest loginAuthnRequest) throws Exception {
	        Claims claims = JwtUtil.verify(jwtToken);
	        UserToken userToken = new UserToken();
	        userToken.setJwtToken(jwtToken);
	        userToken.setJkey(token);
	        userToken.setAccess_type(Constants.INTERNAL);
	        userToken.setExpiry(Long.parseLong(claims.get(Constants.EXP).toString()));
	        userToken.setPartner_code(loginAuthnRequest.getPartner_code());
	        userToken.setPartner_channel(loginAuthnRequest.getPartner_channel());
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
	    
	    private UserTokenData getUserTokenData(LoginAuthnRequest loginAuthnRequest) throws JsonProcessingException {
	        Object redisData = cacheUtil.get(Constants.COM_ENRICH_AUTHN_API + loginAuthnRequest.getUser_id() + ":" + Constants.JWT_ARRAY);
	        return Validator.hasData(redisData) ? UserTokenData.fromJson(redisData.toString()) : new UserTokenData();
	    }

	    private void loginAuditEntity(LoginAuthnRequest loginAuthnRequest) {
	        saveLoginAudit(loginAuthnRequest, Constants.SUCCESS_LOGIN);
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

	    private static String removeExpiredJwt(String updatedjkeyRespone, LoginAuthnRequest loginAuthnRequest) throws JsonMappingException, JsonProcessingException {
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

	    private StandardMessageResponse handleErrorResponse(JsonNode root, LoginAuthnRequest loginAuthnRequest) {
	        String errorMessage = root.get(Constants.DATA).get(Constants.ERROR_MEESAGE).asText();
	        saveLoginAudit(loginAuthnRequest, errorMessage);

	        if (errorMessage.equals(Constants.INVALID_BLOCK)) {
	            return StandardResponseUtil.prepareForbiddenResponse(MessageUtil.getErrorMessage(errorMessage));
	        } else if (errorMessage.equals(Constants.INVALID_WRONG)) {
	            return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(errorMessage));
	        } else {
	            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(errorMessage));
	        }
	    }

	    private LoginAudit saveLoginAudit(LoginAuthnRequest loginAuthnRequest, String remarks) {

	        LoginAudit loginAudit = new LoginAudit();
	        loginAudit.setIpAddress(loginAuthnRequest.getIp_details());
	        loginAudit.setMacAddress(loginAuthnRequest.getMac_address());
	        loginAudit.setRemarks(remarks);
	        loginAudit.setInOutTime(LocalDateTime.now());
	        loginAudit.setSessionId(UUID.randomUUID().toString());
	        loginAudit.setUserAccessType(loginAuthnRequest.getPartner_code());
	        loginAudit.setPartnerChannel(loginAuthnRequest.getPartner_channel());
	        loginAudit.setPartnerCode(loginAuthnRequest.getPartner_code());
	        loginAudit.setUserId(loginAuthnRequest.getUser_id());
	        return loginAuditRepository.save(loginAudit);
	    }
	    
//---logout service
	    public StandardMessageResponse logout(LogoutRequest logoutRequest, String jwtToken) {
	        try {
	            if (!Validator.hasData(jwtToken)) {
	                return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.INVALID_JWTTOKEN));
	            }
	            Claims jwtClaims = JwtUtil.verify(jwtToken);
	            String partnerCode = jwtClaims.get(Constants.PARTNER_CODE, String.class);
	      
	            Set<ConstraintViolation<LogoutRequest>> violations = FieldValidationUtil.validate(logoutRequest);
	            if (!violations.isEmpty()) {
	                return FieldValidationUtil.handleValidationErrors(violations);
	            }
	            if (logoutRequest == null || !validateParameterForLogout(logoutRequest)) {
	                return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.INVALID_USER));
	            }
	            String jwtKey = Constants.COM_ENRICH_AUTHN_API + Constants.JWT +":"+jwtToken;
	            String jwtArrayKey = Constants.COM_ENRICH_AUTHN_API + logoutRequest.getUser_id()+":"+Constants.JWT_ARRAY;
	            Object jwtValue = cacheUtil.get(jwtKey);
	            if (jwtValue == null) {
	                return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getMessage(Constants.SESSION_EXPIRED));
	            }
	            if (logoutRequest.getIsAllDevice() && partnerCode.equalsIgnoreCase(Constants.ENRICH)  ) {
	                return logoutFromAllDevices(logoutRequest, jwtKey, jwtArrayKey, jwtValue.toString());
	            } else  if (logoutRequest.getIsAllDevice() && !partnerCode.equalsIgnoreCase(Constants.ENRICH)) {
                      return unauthorizedResponse(Constants.LOGOUT_ALLOW_ONLY_ENRICH);
	            }else {
	                return logoutFromSingleDevice(jwtKey, jwtArrayKey, jwtToken);
	            }
	        } catch (ExpiredJwtException e) {
				return JwtExecptionUtil.handleExpiredJwtException(e);
			} catch (AccessDeniedException e) {
				return JwtExecptionUtil.handleAccessDeniedException(e);
			} catch (Exception e) {
				return JwtExecptionUtil.handleInternalServerError(e);
			}
	    }

	    private StandardMessageResponse logoutFromAllDevices(LogoutRequest logoutRequest, String jwtKey, String jwtArrayKey, String jwtValue) throws MalformedURLException {
	        JsonNode  root =  JsonParseUtil.parseResponseToJsonNode(hydraUtil.logout(logoutRequest, jwtValue));
	       
	        String status = root.get(Constants.DATA).get(Constants.STATUS).asText();
	        if(!status.equalsIgnoreCase(Constants.OK))
	        	 return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getErrorMessage(Constants.SESSION_EXPIRED));
	        cacheUtil.delete(jwtKey);
	        cacheUtil.delete(jwtArrayKey);
	        return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessage(Constants.LOGOUT_ALL_DEVICES));
	    }

	    private StandardMessageResponse logoutFromSingleDevice(String jwtKey, String jwtArrayKey, String jwtToken) throws JsonMappingException, JsonProcessingException {
	        String jwtArrayValue = cacheUtil.get(jwtArrayKey).toString();
	        String arrayObject = removeExpiryJwtLogout(jwtArrayValue, jwtToken);
	        cacheUtil.set(jwtArrayKey, arrayObject, 898808);
	        cacheUtil.delete(jwtKey);
	        return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessage(Constants.LOGOUT_SUCCESS));
	    }

	    private static  boolean validateParameterForLogout(LogoutRequest logoutRequest) {
	        return Validator.hasData(logoutRequest.getUser_id());
	    }

	    private String removeExpiryJwtLogout(String updatedjkeyRespone, String jwtToken) throws JsonMappingException, JsonProcessingException {
	        Claims claim = JwtUtil.verify(jwtToken);
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonNode = objectMapper.readTree(updatedjkeyRespone);
	        JsonNode modifiedJsonData = removeJwtTokens( claim,jsonNode,jwtToken);
	        return convertJsonToString(modifiedJsonData, objectMapper);
	    }

	    private JsonNode removeJwtTokens(Claims claim, JsonNode jsonData, String jwtToken ) {
	        ArrayNode dataArray = (ArrayNode) jsonData.get(Constants.DATA);

	        Iterator<JsonNode> iterator = dataArray.iterator();
	        while (iterator.hasNext()) {
	            JsonNode item = iterator.next();
	            if (item.has(Constants.JWT_TOKEN) && item.get(Constants.JWT_TOKEN).asText().equals(jwtToken) &&
	                item.has(Constants.PARTNER_CHANNEL) && item.has(Constants.PARTNER_CODE)) {

	                String partnerChannel = item.get(Constants.PARTNER_CHANNEL).asText();
	                String partnerCode = item.get(Constants.PARTNER_CODE).asText();

	                // Check if the current item matches the criteria
	                if (partnerChannel.equalsIgnoreCase(claim.get(Constants.PARTNER_CHANNEL).toString()) &&
	                    partnerCode.equalsIgnoreCase(claim.get(Constants.PARTNER_CODE).toString())) {

	                    // Remove the current item from the array
	                    iterator.remove();
	                }
	            }
	        }
	        return jsonData;
	    }
	    
	    private static StandardMessageResponse unauthorizedResponse(String errorMessage) {
		    return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(errorMessage));
		}

	    private static String convertJsonToString(JsonNode jsonData, ObjectMapper objectMapper) throws JsonProcessingException {
	        return objectMapper.writeValueAsString(jsonData);
	    }

	}


