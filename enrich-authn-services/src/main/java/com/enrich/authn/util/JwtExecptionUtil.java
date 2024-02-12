package com.enrich.authn.util;

import org.springframework.stereotype.Component;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.exception.AccessDeniedException;
import com.enrich.authn.pojo.StandardMessageResponse;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtExecptionUtil {
	

	public static StandardMessageResponse handleExpiredJwtException(ExpiredJwtException e) {
	    return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(Constants.JWT_EXPIRED));
	}

	public static  StandardMessageResponse handleAccessDeniedException(AccessDeniedException e) {
	    return StandardResponseUtil.prepareUnAuthorizedListResponse(MessageUtil.getErrorMessage(e.getMessage()));
	}

	public static StandardMessageResponse handleInternalServerError(Exception e) {
	    // Log the exception for debugging purposes
	    log.error("Internal Server Error", e);
	    return StandardResponseUtil.prepareInternalServerErrorResponse();
	}

}
