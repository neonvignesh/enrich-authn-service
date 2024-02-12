package com.enrich.authn.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.enrich.authn.constants.Constants;

@Component
public class HttpStatusUtil {
	public static HttpStatus getHttpStatusFromSystemMessage(String systemMessage) {
		switch (systemMessage) {
		case Constants.MESSAGE_TYPE_401_UNAUTHORIZED_ERROR:
			return HttpStatus.UNAUTHORIZED;
		case Constants.MESSAGE_TYPE_500_INTERNAL_SERVER_ERROR:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		case Constants.MESSAGE_TYPE_400_BAD_REQUEST_ERROR:
			return HttpStatus.BAD_REQUEST;
		case Constants.MESSAGE_TYPE_404_NOT_FOUND_ERROR:
			return HttpStatus.NOT_FOUND;
		case Constants.MESSAGE_TYPE_403_FORBIDDEN_ERROR:
			return HttpStatus.FORBIDDEN;
		default:
			return HttpStatus.OK;
		}
	}
}
