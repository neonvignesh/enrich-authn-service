package com.enrich.authn.service.internal;

import java.io.IOException;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.exception.AccessDeniedException;
import com.enrich.authn.pojo.JwtVerificationResult;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.util.JwtExecptionUtil;
import com.enrich.authn.util.JwtUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.Validator;
import com.enrich.authn.util.external.CacheUtil;
import com.enrich.authn.util.external.HydraUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import java.util.List;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JkeyVerifyService {

	private final CacheUtil cacheUtil;

	private final HydraUtil hydraUtil;

	public StandardMessageResponse getJkey(String jwtToken) {
		try {
			if (!Validator.hasData(jwtToken)) 
				return handleInvalidJwtToken();
			Claims jwtClaims = JwtUtil.verify(jwtToken);
			String jwtUserId = jwtClaims.get(Constants.USER_ID, String.class);
			String partnerChannel = jwtClaims.get(Constants.PARTNER_CHANNEL, String.class);
			String jwtKey = Constants.COM_ENRICH_AUTHN_API + Constants.JWT + ":" + jwtToken;
			Object cachedData = cacheUtil.get(jwtKey);
			if (List.of(Constants.GUEST_WEB, Constants.GUEST_MOB).contains(partnerChannel.toUpperCase())) {
				return getGuestJkey(jwtToken, jwtUserId);
			}
			if (Validator.hasData(cachedData) ) {
				return handleCachedData(jwtUserId, cachedData.toString());
			}
			return StandardResponseUtil
					.prepareUnAuthorizedListResponse(MessageUtil.getMessage(Constants.SESSION_EXPIRED));
		} catch (ExpiredJwtException e) {
			return JwtExecptionUtil.handleExpiredJwtException(e);
		} catch (AccessDeniedException e) {
			return JwtExecptionUtil.handleAccessDeniedException(e);
		} catch (Exception e) {
			return JwtExecptionUtil.handleInternalServerError(e);
		}
	}

	private StandardMessageResponse getGuestJkey(String jwtToken, String jwtUserId) {
		String cacheKey = Constants.COM_ENRICH_AUTHN_API + jwtUserId +":"+ Constants.JWT_ARRAY;
		Object existingCache = cacheUtil.get(cacheKey);
		if (Validator.hasData(existingCache)) {
			Gson gson = new Gson();
			JsonObject existingObj = gson.fromJson(existingCache.toString(), JsonObject.class);
			JsonArray dataArray = existingObj.getAsJsonArray(Constants.DATA);
			if (Validator.hasData(dataArray)) {
				return getExistingJwt(jwtToken, dataArray);
			}
		}
		return StandardResponseUtil.prepareBadRequestResponseWithMsg(Constants.INVALID_JWTTOKEN);
	}

	private StandardMessageResponse getExistingJwt(String jwtToken, JsonArray dataArray) {
		String existingJwt = dataArray.get(0).getAsJsonObject().get(Constants.JWT_TOKEN).getAsString();
		if (existingJwt.equals(jwtToken))
			return StandardResponseUtil
					.prepareSuccessListResponse(MessageUtil.getMessageJkey(Constants.NO_JKEY_FOR_GUEST));
		return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getMessage(Constants.SESSION_EXPIRED));
	}

	private StandardMessageResponse handleInvalidJwtToken() {
		return StandardResponseUtil
				.prepareBadRequestResponseList(MessageUtil.getErrorMessage(Constants.INVALID_JWTTOKEN));
	}

	private StandardMessageResponse handleCachedData(String jwtUserId, String cachedData) throws IOException {
		JsonObject response = hydraUtil.verifyJkey(jwtUserId, cachedData);
		if (Validator.hasData(response)) {
			JsonNode root = new ObjectMapper().readTree(response.toString());
			if (Validator.hasData(root) && root.has(Constants.DATA) && root.get(Constants.DATA).has(Constants.STATUS)
					&& root.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.OK)) {
				return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessageJkey(cachedData));
			} else 
				return StandardResponseUtil
						.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.SESSION_EXPIRED));
		}
		return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getMessage(Constants.SESSION_EXPIRED));
	}
	

	// ===============================================
	public StandardMessageResponse verifyJwt(String jwtToken, String userId) {
		try {

			if (isInvalidUserId(userId))
				return JwtVerificationResult.INVALID_USER_ID.process(null, null, null, null);
			Claims jwtClaims = JwtUtil.verify(jwtToken);
			if (isUserIdMismatch(userId, jwtClaims))
				return JwtVerificationResult.USER_ID_MISMATCH.process(null, null, null, null);
			String cacheKey = buildCacheKey(userId);
			Object jwtValue = cacheUtil.get(cacheKey);
			return validateJwt(jwtToken, userId, jwtClaims, jwtValue);
		}
		catch (ExpiredJwtException e) {
			return JwtExecptionUtil.handleExpiredJwtException(e);
		} catch (AccessDeniedException e) {
			return JwtExecptionUtil.handleAccessDeniedException(e);
		}	
		catch (Exception e) {
			log.error("Error Occurred in verifyJwt service: " + e.getMessage());
			return JwtVerificationResult.ERROR.process(null, null, null, null);
		}
	}

	
	private StandardMessageResponse validateJwt(String jwtToken, String userId, Claims jwtClaims, Object jwtValue) throws IOException {
		String jwtKey = Constants.COM_ENRICH_AUTHN_API + Constants.JWT +":"+jwtToken;
		Object token=cacheUtil.get(jwtKey);
		if (Validator.hasData(token) ) 
			return handleJwtExpiry(userId, token.toString());
		if (isJwtTokenExpired(jwtValue))
			return JwtVerificationResult.JWT_EXPIRED.process(null, null, null, null);
		if (!isTokenValid(jwtToken, jwtValue))
			return JwtVerificationResult.JWT_EXPIRED.process(null, null, null, null);
		return JwtVerificationResult.JWT_VERIFIED.process(jwtToken, userId, jwtClaims, jwtValue);
	}

	private static boolean isInvalidUserId(String userId) {
		return !Validator.hasData(userId);
	}

	private static boolean isUserIdMismatch(String userId, Claims jwtClaims) {
		String jwtUserId = jwtClaims.get(Constants.USER_ID, String.class);
		return !userId.equals(jwtUserId);
	}

	private static String buildCacheKey(String userId) {
		return Constants.COM_ENRICH_AUTHN_API + userId + ":" + Constants.JWT_ARRAY;
	}

	private static boolean isJwtTokenExpired(Object jwtValue) {
		return !Validator.hasData(jwtValue) ;
	}

	private StandardMessageResponse handleJwtExpiry(String jwtUserId, String cachedData) throws IOException {
		JsonObject response = hydraUtil.verifyJkey(jwtUserId, cachedData);
		if (Validator.hasData(response)) {
			JsonNode root = new ObjectMapper().readTree(response.toString());
			if (Validator.hasData(root) && root.has(Constants.DATA) && root.get(Constants.DATA).has(Constants.STATUS)
					&& root.get(Constants.DATA).get(Constants.STATUS).asText().equalsIgnoreCase(Constants.OK)) {
				return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getMessage(Constants.JWT_VERIFIED));
			} else 
				return StandardResponseUtil
						.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.JWT_EXPIRED));
		}
		return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.JWT_EXPIRED));
	}
	
	private static boolean isTokenValid(String jwtToken, Object jwtValue) {
		Gson gson = new Gson();
		JsonObject myObject = gson.fromJson(jwtValue.toString(), JsonObject.class);
		JsonArray dataArray = myObject.getAsJsonArray(Constants.DATA);
		for (JsonElement element : dataArray) {
			if (element.isJsonObject()) {
				JsonObject dataObject = element.getAsJsonObject();
				String storedJwtToken = dataObject.get(Constants.JWT_TOKEN).getAsString();
				if (jwtToken.equals(storedJwtToken)) {
					return true;
				}
			}
		}
		return false;
	}
}
