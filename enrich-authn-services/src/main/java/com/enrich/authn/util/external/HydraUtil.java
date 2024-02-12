package com.enrich.authn.util.external;
import org.json.simple.JSONObject;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.pojo.ChangePasswordRequest;
import com.enrich.authn.pojo.ForgotPasswordRequest;
import com.enrich.authn.pojo.GenerateOtpRequest;
import com.enrich.authn.pojo.LoginAuthnRequest;
import com.enrich.authn.pojo.LogoutRequest;
import com.enrich.authn.pojo.LoginRequest;
import com.enrich.authn.provider.AuthnServiceProvider;
import com.enrich.authn.util.HttpClientExceptionUtil;
import com.enrich.authn.util.UnauthorizedUtil;
import com.enrich.authn.util.Validator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
@RequiredArgsConstructor
public class HydraUtil {
	
	private final AuthnServiceProvider authnServiceProvider;

	private final RestTemplate restTemplate = new RestTemplate();

	public JsonObject authenticate(LoginAuthnRequest loginAuthnRequest) {
		return executeHydraRequest(authnServiceProvider.getAuthenticateUrl(), createAuthRequestJson(loginAuthnRequest));
	}
	
	
	public JsonObject authenticateWithoutOtp(LoginRequest mobileLoginRequest) {
		return executeHydraWithoutOtpRequest(authnServiceProvider.getAuthnUrl(), createMobileLoginRequest(mobileLoginRequest));
	}
	
	public JsonObject forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
		return executeHydraRequest(authnServiceProvider.getForgotUrl(),
				createForgotPasswordRequestJson(forgotPasswordRequest));
	}
	
	public JsonObject generateOtp(GenerateOtpRequest generateOtpRequest) {
		return executeHydraRequest(authnServiceProvider.getGenerateOtpUrl(),
				createGenerateOtpRequestJson(generateOtpRequest));
	}

	public JsonObject changePassword(ChangePasswordRequest changePasswordRequest) {
		return executeHydraRequest(authnServiceProvider.getChangeUrl(),
				createChangePasswordRequestJson(changePasswordRequest));
	}
	
	public JsonObject logout(LogoutRequest logoutRequest, String jkey) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, logoutRequest.getUser_id().toUpperCase());
		return executeHydraLogoutRequest(authnServiceProvider.getLogoutUrl(), inputJson, jkey);
	}

	public JsonObject verifyJkey(String userId, String jkey) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, userId.toUpperCase());
		return executeJkeyRequest(authnServiceProvider.getMwlistUrl(), inputJson, jkey);
	}
	
	private JsonObject executeJkeyRequest(String url, JSONObject inputJson, String jkey) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(Constants.X_API_KEY, authnServiceProvider.getHydraApiKey());
			headers.add(Constants.JKEY_TOKEN, jkey);
			HttpEntity<JSONObject> requestEntity = new HttpEntity<>(inputJson, headers);
			ResponseEntity<JSONObject> output = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					JSONObject.class);
			if (Validator.hasData(output)) {
				String response = output.getBody().toString();
				return new Gson().fromJson(response, JsonObject.class);
			}
		} catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				return UnauthorizedUtil.createCustomErrorResponse("Session has been expired");
			} else {
				log.error("Error occurred in executeHydraRequest: URL: {} {}", url, e.getMessage());
				return new Gson().fromJson(e.getResponseBodyAsString(), JsonObject.class);
			}
		} catch (Exception e) {
			log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
			return new Gson().fromJson(e.getMessage(), JsonObject.class);
		}
		return null;
	}
	
	public String enableTOTP(String userId, String jKey) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(authnServiceProvider.getTotp()).queryParam(Constants.USER_ID, userId);
			HttpHeaders headers = new HttpHeaders();
			headers.set(Constants.X_API_KEY, authnServiceProvider.getHydraApiKey());
			headers.set(Constants.JKEY_TOKEN, jKey);
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<?> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
					requestEntity, String.class);
			String customResponse = responseEntity.getBody().toString();
			return customResponse;
		}
		catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				return UnauthorizedUtil.createCustomErrorResponse(Constants.INVALID_TOKEN).toString();
			} else {
				log.error("Error occurred in executeHydraRequest: URL: {} {}", e.getMessage());
				return new Gson().fromJson(e.getResponseBodyAsString(), String.class);
			}
		} catch (Exception e) {
			log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
			return new Gson().fromJson(e.getMessage(), String.class);
		}
	}

	private JsonObject executeHydraRequest(String url, JSONObject inputJson) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(Constants.X_API_KEY, authnServiceProvider.getHydraApiKey());
			HttpEntity<JSONObject> requestEntity = new HttpEntity<>(inputJson, headers);
			ResponseEntity<JSONObject> output = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					JSONObject.class);
			if (Validator.hasData(output)) {
			
				String response = output.getBody().toString();
				return new Gson().fromJson(response, JsonObject.class);
			}
		} catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				return UnauthorizedUtil.createCustomErrorResponse(Constants.INVALID_USERID_PASSWORD);
			} else {
				log.error("Error occurred in executeHydraRequest: URL: {} {}", url, e.getMessage());
				return new Gson().fromJson(e.getResponseBodyAsString(), JsonObject.class);
			}
		} catch (Exception e) {
			log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
			return new Gson().fromJson(e.getMessage(), JsonObject.class);
		}
		return null;
	}
	
	
	
	private JsonObject executeHydraWithoutOtpRequest(String url, JSONObject inputJson) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(Constants.X_API_KEY, authnServiceProvider.getHydraApiKey());
			HttpEntity<JSONObject> requestEntity = new HttpEntity<>(inputJson, headers);
			ResponseEntity<JSONObject> output = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					JSONObject.class);
			if (Validator.hasData(output)) {
				String response = output.getBody().toString();
				return new Gson().fromJson(response, JsonObject.class);
			}
		} catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				return UnauthorizedUtil.createCustomErrorResponse(Constants.INVALID_USERID_PASSWORD_MOBILE);
			} else {
				log.error("Error occurred in executeHydraRequest: URL: {} {}", url, e.getMessage());
				return new Gson().fromJson(e.getResponseBodyAsString(), JsonObject.class);
			}
		} catch (Exception e) {
			log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
			return new Gson().fromJson(e.getMessage(), JsonObject.class);
		}
		return null;
	}
	
	
	private JsonObject executeHydraLogoutRequest(String url, JSONObject inputJson, String jKey) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(Constants.X_API_KEY, authnServiceProvider.getHydraApiKey());
			headers.add(Constants.JKEY, jKey);
			HttpEntity<JSONObject> requestEntity = new HttpEntity<>(inputJson, headers);
			ResponseEntity<JSONObject> output = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					JSONObject.class);
			if (Validator.hasData(output)) {
				String response = output.getBody().toString();
				return new Gson().fromJson(response, JsonObject.class);
			}
		} catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				return UnauthorizedUtil.createCustomErrorResponse("Invalid Token");
			} else {
				log.error("Error occurred in executeHydraRequest: URL: {} {}", url, e.getMessage());
				return new Gson().fromJson(e.getResponseBodyAsString(), JsonObject.class);
			}
		}
			catch (Exception e) {
				log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
				return new Gson().fromJson(e.getMessage(), JsonObject.class);
			}
		
		return null;
	}
	
	private static JSONObject createAuthRequestJson(LoginAuthnRequest loginAuthnRequest) {
		return createRequestJson(loginAuthnRequest.getUser_id().toUpperCase(), loginAuthnRequest.getPassword(),
				loginAuthnRequest.getOtp());
	}
	
	private static JSONObject createForgotPasswordRequestJson(ForgotPasswordRequest forgotPasswordRequest) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, forgotPasswordRequest.getUser_id().toUpperCase());
		inputJson.put(Constants.PAN_NO, forgotPasswordRequest.getPan_no().toUpperCase());
		inputJson.put(Constants.DOB, forgotPasswordRequest.getDob());
		return inputJson;
	}
	
	private static JSONObject createGenerateOtpRequestJson(GenerateOtpRequest generateOtpRequest) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, generateOtpRequest.getUser_id());
		inputJson.put(Constants.PAN_NO, generateOtpRequest.getPan_no());
		return inputJson;
	}
	
	private static JSONObject createChangePasswordRequestJson(ChangePasswordRequest changePasswordRequest) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, changePasswordRequest.getUser_id().toUpperCase());
		inputJson.put(Constants.OLD_PASSWORD, changePasswordRequest.getOld_password());
		inputJson.put(Constants.PASSWORD, changePasswordRequest.getPassword());
		return inputJson;

	}
	
	private static JSONObject createRequestJson(String userId, String password, String otp) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, userId);
		inputJson.put(Constants.PASSWORD, password);
		inputJson.put(Constants.OTP, otp);
		
		return inputJson;
	}
	

	private static JSONObject createMobileLoginRequest(LoginRequest mobileLoginRequest) {
		JSONObject inputJson = new JSONObject();
		inputJson.put(Constants.USER_ID, mobileLoginRequest.getUser_id());
		inputJson.put(Constants.PASSWORD, mobileLoginRequest.getPassword());
		return inputJson;
	}
}
