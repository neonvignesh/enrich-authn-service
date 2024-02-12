package com.enrich.authn.service.v1.authentication;
import java.time.Instant;
import java.util.Date;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.entity.UserTokenAction;
import com.enrich.authn.exception.AccessDeniedException;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.TokenRequest;
import com.enrich.authn.service.repository.UserTokenActionRepository;
import com.enrich.authn.util.JwtUtil;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;
import com.enrich.authn.util.external.CacheUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor

public class AccessTokenService {

	@Lazy
	private final CacheUtil cacheUtil;

	@Lazy
	private final UserTokenActionRepository userTokenActionRepository;

	public StandardMessageResponse generateAccessToken(String jwtToken, TokenRequest tokenrequest) {
		try {
			
			
			if (validateTokenRequest(tokenrequest) != null)
				return validateTokenRequest(tokenrequest);
			Claims jwtClaims = JwtUtil.verify(jwtToken);
			String jwtUserId = jwtClaims.get(Constants.USER_ID, String.class);
	        String partnerCode = jwtClaims.get(Constants.PARTNER_CODE, String.class);
	        String partnerChannel = jwtClaims.get(Constants.PARTNER_CHANNEL, String.class);
	        if (!isValidPartner(partnerCode, partnerChannel)) {
	            return unauthorizedResponse(Constants.ACCESS_DENIED_PARTNER);
	        }

			String cacheKey = Constants.COM_ENRICH_AUTHN_API + jwtUserId + ":" + Constants.JWT_ARRAY;
			Object jwtValue = cacheUtil.get(cacheKey);
			if (jwtValue == null)
				return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.SESSION_EXPIRED));
			String newAccessToken = JwtUtil.generateNewAccessToken(jwtClaims, tokenrequest.getValidity());
			Claims newclaims = JwtUtil.verify(newAccessToken);
			Long expiry = Long.parseLong(newclaims.get(Constants.EXP).toString());
			updateAccessTokenInCache(jwtValue, tokenrequest, newAccessToken, cacheKey);
			saveAccessTokenHistory(jwtToken, jwtUserId, newAccessToken, expiry);
			return StandardResponseUtil
					.prepareSuccessListResponse(MessageUtil.getAccessTokenMsg(Constants.SUCCESS_LOGIN, newAccessToken));
		} catch (ExpiredJwtException e) {
			return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.JWT_EXPIRED));
		} catch (AccessDeniedException e) {
			return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(e.getMessage()));
		} catch (Exception e) {
			log.error("Error occurred in generateAccessToken Service -- generateAccessToken method: {}", e.getMessage());
			return StandardResponseUtil.prepareInternalServerErrorResponse();
		}
	}
	
	private StandardMessageResponse validateTokenRequest(TokenRequest tokenRequest) {
	    if (differenceInSeconds(tokenRequest) < 0) {
	        return StandardResponseUtil.prepareBadRequestResponseList(
	                MessageUtil.getErrorMessage(Constants.VALIDITY_LESSER_1_DAY));
	    }
	    if (differenceInSeconds(tokenRequest) >= 2678400) {
	        return StandardResponseUtil.prepareBadRequestResponseList(
	                MessageUtil.getErrorMessage("Validity period cannot be greater than 30 days"));
	    }
	    return null;
	}
	private static long differenceInSeconds(TokenRequest tokenrequest) { 
		long currentEpoch = Instant.now().getEpochSecond();
		long differenceInSeconds = (tokenrequest.getValidity() - currentEpoch);
		return differenceInSeconds;
	}

	private JsonArray updateDataArray(JsonArray dataArray, String newAccessToken, TokenRequest tokenRequest) {
	    JsonArray modifiedArray = new JsonArray();
	    String token = null;
	    boolean accessTokenUpdated = false;
	    for (JsonElement element : dataArray) {
	        JsonObject dataObject = element.getAsJsonObject();
	        token = dataObject.get(Constants.JKEY).getAsString();
	        if (Constants.ACCESS_TOKEN.equals(dataObject.get(Constants.ACCESS_TYPE).getAsString())) {
	            updateAccessTokenProperties(dataObject, newAccessToken, token, tokenRequest);
	            accessTokenUpdated = true;
	        }
	        modifiedArray.add(dataObject);
	    }
	    if (!accessTokenUpdated) {
	        JsonObject newObject = createAccessTokenJsonObject(newAccessToken, token, tokenRequest);
	        modifiedArray.add(newObject);
	    }
	    return modifiedArray;
	}

	private void updateAccessTokenProperties(JsonObject dataObject, String newAccessToken, String token, TokenRequest tokenRequest) {
	    Claims newClaims = JwtUtil.verify(newAccessToken);
	    Long expiry = Long.parseLong(newClaims.get(Constants.EXP).toString());
	    dataObject.addProperty(Constants.JWT_TOKEN, newAccessToken);
	    dataObject.addProperty(Constants.JKEY, token);
	    dataObject.addProperty(Constants.EXPIRY, expiry);
	    dataObject.addProperty(Constants.PARTNER_CODE, Constants.ENRICH);
	    dataObject.addProperty(Constants.PARTNER_CHANNEL, Constants.CLIENT);
	    dataObject.addProperty(Constants.ACCESS_TYPE,Constants.ACCESS_TOKEN);
	    String jkeyRedisKey = Constants.COM_ENRICH_AUTHN_API + Constants.JWT +":" +newAccessToken;
	    cacheUtil.set(jkeyRedisKey, token, (int) differenceInSeconds(tokenRequest));
	}

	private  JsonObject createAccessTokenJsonObject(String newAccessToken, String token, TokenRequest tokenRequest) {
	    Claims newClaims = JwtUtil.verify(newAccessToken);
	    Long expiry = Long.parseLong(newClaims.get(Constants.EXP).toString());
	    JsonObject newObject = new JsonObject();
	    newObject.addProperty(Constants.JWT_TOKEN, newAccessToken);
	    newObject.addProperty(Constants.JKEY, token);
	    newObject.addProperty(Constants.EXPIRY, expiry);
	    newObject.addProperty(Constants.PARTNER_CODE, Constants.ENRICH);
	    newObject.addProperty(Constants.PARTNER_CHANNEL, Constants.CLIENT);
	    newObject.addProperty(Constants.ACCESS_TYPE, Constants.ACCESS_TOKEN);
	    String jkeyRedisKey = Constants.COM_ENRICH_AUTHN_API + Constants.JWT +":"+ newAccessToken;
	    cacheUtil.set(jkeyRedisKey, token, (int) differenceInSeconds(tokenRequest));
	    return newObject;
	}

	private void updateAccessTokenInCache(Object jwtValue, TokenRequest tokenRequest, String newAccessToken, String cacheKey) {
	    Gson gson = new Gson();
	    JsonObject myObject = gson.fromJson(jwtValue.toString(), JsonObject.class);
	    JsonArray dataArray = myObject.getAsJsonArray(Constants.DATA);
	    JsonArray modifiedArray = updateDataArray(dataArray, newAccessToken, tokenRequest);
	    myObject.add(Constants.DATA, modifiedArray);
	    cacheUtil.set(cacheKey, myObject.toString(), 380000);
	}

	private void saveAccessTokenHistory(String jwtToken, String userId, String newAccessToken, Long expiry) {
        UserTokenAction userTokenAction = new UserTokenAction();
        userTokenAction.setInputToken(jwtToken);
        userTokenAction.setUserId(userId);
        userTokenAction.setAccessToken(newAccessToken);
        userTokenAction.setExpiryDate(new Date(expiry * 1000)); 
        userTokenActionRepository.save(userTokenAction);
    }

	//=========================
	// get access token generate service
	public StandardMessageResponse getAccessToken(String jwtToken, String userId) {
	    try {
	        Claims jwtClaims = JwtUtil.verify(jwtToken);
	        if (jwtClaims == null) 
	            return invalidJwtResponse();
	        String jwtUserId = jwtClaims.get(Constants.USER_ID, String.class);
	        String partnerCode = jwtClaims.get(Constants.PARTNER_CODE, String.class);
	        String partnerChannel = jwtClaims.get(Constants.PARTNER_CHANNEL, String.class);
	        if (!isValidPartner(partnerCode, partnerChannel)) {
	            return unauthorizedResponse(Constants.ACCESS_DENIED_PARTNER);
	        }
	        String accessToken = getAccessTokenFromCache(jwtUserId);
	        if (accessToken != null) {
	            return successResponse(Constants.ACCESS_TOKEN_FETCHED, accessToken);
	        } else {
	            return unauthorizedResponse1();
	        }
	    } catch (ExpiredJwtException e) {
	        return unauthorizedResponse(Constants.JWT_EXPIRED);
	    } catch (AccessDeniedException e) {
	        return unauthorizedResponse(e.getMessage());
	    } catch (Exception e) {
	    	log.error("Error occurred in getAccessToken Service -- getAccessToken method: {}", e.getMessage());
	        return StandardResponseUtil.prepareInternalServerErrorResponse();
	    }
	}

	private static  StandardMessageResponse invalidJwtResponse() {
	    return StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage("Invalid JWT token"));
	}

	private static StandardMessageResponse unauthorizedResponse(String errorMessage) {
	    return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(errorMessage));
	}
	
	
	private static StandardMessageResponse unauthorizedResponse1() {
	    return StandardResponseUtil.prepareSuccessListResponse(Collections.emptyList());
	}

	private static StandardMessageResponse successResponse(String message, String accessToken) {
	    return StandardResponseUtil.prepareSuccessListResponse(MessageUtil.getAccessTokenMsg(message, accessToken));
	}

	private static boolean isValidPartner(String partnerCode, String partnerChannel) {
	    return Constants.ENRICH.equals(partnerCode) &&
	            (Constants.API.equalsIgnoreCase(partnerChannel) || Constants.WEB.equalsIgnoreCase(partnerChannel) || Constants.MOB.equalsIgnoreCase(partnerChannel));
	}

	private String getAccessTokenFromCache(String jwtUserId) {
	    String cacheKey = Constants.COM_ENRICH_AUTHN_API + jwtUserId + ":" + Constants.JWT_ARRAY;
	    Object jwtValue = cacheUtil.get(cacheKey);
	    if (jwtValue == null) {
	        return null;
	    }
	    Gson gson = new Gson();
	    JsonObject myObject = gson.fromJson(jwtValue.toString(), JsonObject.class);
	    JsonArray dataArray = myObject.getAsJsonArray(Constants.DATA);

	    for (JsonElement element : dataArray) {
	        JsonObject dataObject = element.getAsJsonObject();
	        JsonElement accessTypeElement = dataObject.get(Constants.ACCESS_TYPE);

	        if (isAccessToken(accessTypeElement)) {
	            return dataObject.get(Constants.JWT_TOKEN).getAsString();
	        }
	    }
	    return null;
	}

	private static boolean isAccessToken(JsonElement accessTypeElement) {
	    return accessTypeElement != null && accessTypeElement.isJsonPrimitive() &&
	            Constants.ACCESS_TOKEN.equals(accessTypeElement.getAsString());
	}

}