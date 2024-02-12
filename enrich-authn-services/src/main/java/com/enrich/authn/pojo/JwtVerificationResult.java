package com.enrich.authn.pojo;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum JwtVerificationResult {
    INVALID_USER_ID,
    USER_ID_MISMATCH,
    JWT_EXPIRED,
    JWT_VERIFIED,
    ERROR;

    public StandardMessageResponse process(String jwtToken, String userId, Claims jwtClaims, Object jwtValue) {
        try {
            switch (this) {
                case INVALID_USER_ID:
                    return prepareInvalidUserIdResponse();
                case USER_ID_MISMATCH:
                    return prepareUserIdMismatchResponse();
                case JWT_EXPIRED:
                    return prepareJwtExpiredResponse();
                case JWT_VERIFIED:
                    return prepareJwtVerifiedResponse();


                default:
                    return prepareErrorResponse("Invalid verification result.");
            }
        } catch (Exception e) {
            log.error("Error Occurred in verifyJwt service: " + e.getMessage());
            return prepareErrorResponse(e.getMessage());
        }
    }

    private StandardMessageResponse prepareInvalidUserIdResponse() {
        return StandardResponseUtil.prepareBadRequestResponseList(
                MessageUtil.getErrorMessage(Constants.INVALID_USER_ID));
    }

    private StandardMessageResponse prepareUserIdMismatchResponse() {
        return StandardResponseUtil.prepareUnAuthorizedListResponse(
                MessageUtil.getErrorMessage(Constants.USER_ID_MISMATCH));
    }

    private StandardMessageResponse prepareJwtExpiredResponse() {
        return StandardResponseUtil.prepareUnAuthorizedListResponse(
                MessageUtil.getErrorMessage(Constants.JWT_EXPIRED));
    }

    private StandardMessageResponse prepareJwtVerifiedResponse() {
        return StandardResponseUtil.prepareSuccessListResponse(
                MessageUtil.getMessage(Constants.JWT_VERIFIED));
    }

    private StandardMessageResponse prepareErrorResponse(String errorMessage) {
        return StandardResponseUtil.prepareUnAuthorizedListResponse(
                MessageUtil.getErrorMessage(errorMessage));
    }
}
