package com.enrich.authn.util;

import com.enrich.authn.constants.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
@Component
@Slf4j
public class HttpClientExceptionUtil {
	
	public static JsonObject handleHttpClientErrorException(String url, HttpClientErrorException e) {
	    if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
	        return UnauthorizedUtil.createCustomErrorResponse(Constants.INVALID_USERID_PASSWORD);
	    } else {
	        log.error("Error occurred in executeHydraRequest: URL: {} {}", url, e.getResponseBodyAsString());
	        return new Gson().fromJson(e.getResponseBodyAsString(), JsonObject.class);
	    }
	}

	public static JsonObject handleGenericException(Exception e) {
	    log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
	    return new Gson().fromJson(e.getMessage(), JsonObject.class);
	}
}
