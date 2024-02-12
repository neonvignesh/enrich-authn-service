package com.enrich.authn.service.v2.authentication;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.entity.LoginAudit;
import com.enrich.authn.entity.UserCredentials;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.ClientInfo;
import com.enrich.authn.pojo.GenerateOtpRequest;
import com.enrich.authn.pojo.LoginRequest;
import com.enrich.authn.pojo.SimpleMobileLoginRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.repository.LoginAuditRepository;
import com.enrich.authn.service.repository.PartnerRepository;
import com.enrich.authn.service.repository.UserCredentialsRepository;
import com.enrich.authn.util.Base64Util;
import com.enrich.authn.util.CryptoUtil;
import com.enrich.authn.util.FieldValidationUtil;
import com.enrich.authn.util.JsonParseUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.CacheUtil;
import com.enrich.authn.util.external.HydraUtil;
import com.enrich.authn.util.external.TechExcelUtil;
import com.enrich.authn.util.external.TechExcelUtil1;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateLoginService {
	
	private final CryptoUtil cryptoUtil;
	
	private final HydraUtil hydraUtil;

	private final CacheUtil cacheUtil;
	private final TechExcelUtil1 techExcelUtil;

	private final LoginAuditRepository loginAuditRepository;

	private final PartnerRepository partnerRepository;
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final UserCredentialsRepository userCredentialsRepository;

	  public StandardMessageResponse authenticate(AuthnEncryptRequest encryptedData){
	        try {   	
	            if (isValidEncryptedData(encryptedData)) {
	            	LoginRequest loginRequest = cryptoUtil.decrypt(Base64Util.decode(encryptedData.getData()), LoginRequest.class);
	            
	            	return validateAndProcessAuthentication(loginRequest);
	            }
	            return StandardResponseUtil.prepareBadRequestResponse();
	        } catch (Exception e) {
	            log.error("Error occurred in authenticate Service: {}", e.getMessage());
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
	                log.error("Error occurred in processAuthenticationResponse: {}", e.getMessage());
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
	        JsonNode root = JsonParseUtil.parseResponseToJsonNode(response);
	        return Validator.hasData(root) ?
	                handleAuthenticatedUser1(root, loginRequest) :
	                StandardResponseUtil.prepareBadRequestResponse();
	        
	        
	    }

	    private StandardMessageResponse handleAuthenticatedUser(JsonNode root, LoginRequest loginRequest) {
	      
	    	if (root.has(Constants.DATA) && root.get(Constants.DATA).has(Constants.USER_TOKEN)) {
	            try {
	                Optional<UserCredentials> userCredentialsOptional = getUserCredentialsById(loginRequest.getUser_id());
	                if (userCredentialsOptional.isPresent()) {
	                    return sendOtp(root, loginRequest, userCredentialsOptional.get());
	                } else {
	                    String response = techExcelUtil.getClientInfo(loginRequest.getUser_id());
	                    JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();

	                    if (jsonArray.size() > 0) {
	                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
	                        JsonArray dataArray = jsonObject.getAsJsonArray("DATA");

	                        if (dataArray != null && dataArray.size() > 0) {
	                            JsonArray firstDataRow = dataArray.get(0).getAsJsonArray();
	                            String panNoValue = firstDataRow.get(1).getAsString();

	                            if (panNoValue != null) {
	                                UserCredentials newUserCredentials = new UserCredentials();
	                                newUserCredentials.setUserId(loginRequest.getUser_id());	                               
	                                newUserCredentials.setPanNo(panNoValue);
	                                userCredentialsRepository.save(newUserCredentials);

	                                return sendOtp(root, loginRequest, newUserCredentials);
	                            } else {
	                                return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getMessage(Constants.PAN_NOT_FOUND));
	                            }
	                        } else {
	                            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getMessage(Constants.PAN_NOT_FOUND));
	                        }
	                    } else {
	                        return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getMessage(Constants.PAN_NOT_FOUND));
	                    }
	                }
	            } catch (Exception e) {
	                log.error("Error occurred in handleAuthenticatedUser method: {}", e.getMessage());
	                return StandardResponseUtil.prepareInternalServerErrorResponse();
	            }
	        } else {
	            return handleErrorResponse(root, loginRequest);
	        }
	    }
	    
	    private StandardMessageResponse handleAuthenticatedUser1(JsonNode root, LoginRequest loginRequest) {
	        if (root.has(Constants.DATA) && root.get(Constants.DATA).has(Constants.USER_TOKEN)) {
	            try {
	                Optional<UserCredentials> userCredentialsOptional = getUserCredentialsById(loginRequest.getUser_id());
	                if (userCredentialsOptional.isPresent()) {
	                  
	                    return sendOtp(root, loginRequest, userCredentialsOptional.get());
	                } else {
	                    // Fetch client info to retrieve PAN number
	                    String response = techExcelUtil.getClientInfo(loginRequest.getUser_id());
	                    
	                    // Parse the JSON response
	                    JSONObject jsonResponse = new JSONObject(response);

	                    // Extract data array
	                    JSONArray dataArray = jsonResponse.getJSONArray("data");
	                    
	                    // Check if data array is not empty
	                    if (dataArray != null && dataArray.length() > 0) {
	                        // Get the first element of data array
	                        JSONObject clientData = dataArray.getJSONObject(0);
	                        
	                        // Extract PAN number
	                        String panNumber = clientData.getString("pan_number");
	                        
	                        // Check if PAN number is found
	                        if (panNumber != null && !panNumber.isEmpty()) {
	                            // Save the PAN number to the database
	                            UserCredentials newUserCredentials = new UserCredentials();
	                            newUserCredentials.setUserId(loginRequest.getUser_id());
	                            newUserCredentials.setPanNo(panNumber);
	                            userCredentialsRepository.save(newUserCredentials);
	                            
	                            // Proceed with sending OTP
	                            return sendOtp(root, loginRequest, newUserCredentials);
	                        } else {
	                            // PAN number not found in the response
	                            return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getMessage(Constants.PAN_NOT_FOUND));
	                        }
	                    } else {
	                        // Data array is empty
	                        return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getMessage(Constants.PAN_NOT_FOUND));
	                    } 
	                }
	            } catch (IOException  e) {
	                // Error handling
	                log.error("Error occurred in handleAuthenticatedUser method: {}", e.getMessage());
	                return StandardResponseUtil.prepareInternalServerErrorResponse();
	            }
	        } else {
	            // Handling error response
	            return handleErrorResponse(root, loginRequest);
	        }
	    }

	    
	    private StandardMessageResponse sendOtp(JsonNode root, LoginRequest loginRequest, UserCredentials userCredentials) throws JsonMappingException, JsonProcessingException  {
	    
	            JsonObject otpGenerationOutput = generateOtpForUser(userCredentials);
	            JsonNode otpResponseBody = new ObjectMapper().readTree(otpGenerationOutput.toString());

	            if (Validator.hasData(otpResponseBody) && otpResponseBody.has(Constants.DATA) && otpResponseBody.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.OTP_GENERATION_SUCCESS)) {
	                String redisKey = Constants.USER_CREDENTIALS + loginRequest.getUser_id();
	                SimpleMobileLoginRequest simplifiedRequest = createSimplifiedRequest(loginRequest);
	                cacheUserCredentials(redisKey, simplifiedRequest);
	                return getResponse();
	            } else {
	                return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(otpResponseBody.get(Constants.DATA).get(Constants.STATUS).asText()));
	            }
	        
	    }

	    private JsonObject generateOtpForUser(UserCredentials userCredentials) {
	        GenerateOtpRequest generateOtpRequest = new GenerateOtpRequest();
	        generateOtpRequest.setUser_id(userCredentials.getUserId());
	        generateOtpRequest.setPan_no(userCredentials.getPanNo());
	        return hydraUtil.generateOtp(generateOtpRequest);
	    }

	    private SimpleMobileLoginRequest createSimplifiedRequest(LoginRequest loginRequest) {
	        SimpleMobileLoginRequest simplifiedRequest = new SimpleMobileLoginRequest();
	        simplifiedRequest.setUser_id(loginRequest.getUser_id());
	        simplifiedRequest.setPassword(loginRequest.getPassword());
	        simplifiedRequest.setPartner_channel(loginRequest.getPartner_channel());
	        simplifiedRequest.setPartner_code(loginRequest.getPartner_code());
	        return simplifiedRequest;
	    }

	    private void cacheUserCredentials(String redisKey, SimpleMobileLoginRequest simplifiedRequest) throws JsonProcessingException  {
	        String userDataJson = objectMapper.writeValueAsString(simplifiedRequest);
	        cacheUtil.set(redisKey, userDataJson, 420);
	    }
	    
	    
	    private StandardMessageResponse getResponse() {
	    	return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessage("OTP sent Successfully.."));
	    	
	    }
	    
	    public Optional<UserCredentials> getUserCredentialsById(String userId) {
	        return userCredentialsRepository.findById(userId);
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
