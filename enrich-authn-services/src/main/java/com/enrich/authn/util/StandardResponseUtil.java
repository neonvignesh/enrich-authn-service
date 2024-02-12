package com.enrich.authn.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.response.ErrorResponsePojo;

@Component
public class StandardResponseUtil {

	public static StandardMessageResponse prepareInternalServerErrorResponse() {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_500_INTERNAL_SERVER_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		return standardResponse;
	}

	public static StandardMessageResponse prepareInternalServerErrorResponse(String errorMsg) {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		ErrorResponsePojo msg = new ErrorResponsePojo();
		msg.setMessage(errorMsg);
		msg.setStatus(Constants.NOT_OK);
		msg.setRequestDateTime(new Date().toString());
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_500_INTERNAL_SERVER_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		standardResponse.setData(Collections.singletonList(msg));
		return standardResponse;
	}

	public static StandardMessageResponse prepareForbiddenResponse(List<?> data) {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_403_FORBIDDEN_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		standardResponse.setData(data);
		return standardResponse;
	}

	public static StandardMessageResponse prepareBadRequestResponse() {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_400_BAD_REQUEST_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		return standardResponse;
	}

	public static StandardMessageResponse prepareBadRequestResponseList(List<?> data) {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_400_BAD_REQUEST_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		standardResponse.setData(data);
		return standardResponse;
	}

	public static StandardMessageResponse prepareUnauthorizedResponse() {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_401_UNAUTHORIZED_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		return standardResponse;
	}

	public static StandardMessageResponse prepareTooManyRequestResponse() {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage("Too many requests within one minute.");
		standardResponse.setSystemMessageType("429-Too Many Requests");
		return standardResponse;
	}

	public static StandardMessageResponse prepareUnAuthorizedListResponse(List<?> data) {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_401_UNAUTHORIZED_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		standardResponse.setData(data);
		return standardResponse;
	}

	public static StandardMessageResponse prepareSuccessListResponse(List<?> data) {
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_TRUE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_200);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_SUCCESS);
		standardResponse.setData(data);
		return standardResponse;

	}

	public static StandardMessageResponse prepareBadRequestResponseWithMsg(String error) {
		ErrorResponsePojo msg = new ErrorResponsePojo();
		msg.setMessage(error);
		msg.setStatus(Constants.NOT_OK);
		msg.setRequestDateTime(new Date().toString());
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_400_BAD_REQUEST_ERROR);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_FAILED);
		standardResponse.setData(Collections.singletonList(msg));
		return standardResponse;
	}

	public static StandardMessageResponse prepareSuccessResponse() {
		List<?> Data = new ArrayList<>();
		StandardMessageResponse standardResponse = new StandardMessageResponse();
		standardResponse.setSystemMessage(Constants.MESSAGE_TYPE_200);
		standardResponse.setSystemMessageType(Constants.MESSAGE_TYPE_SUCCESS);
		standardResponse.setSuccess(Constants.MESSAGE_FALSE);
		standardResponse.setData(Data);
		return standardResponse;

	}

	public static ResponseEntity<StandardMessageResponse> generateResponseEntity(StandardMessageResponse result) {
		HttpStatus status;
		if (result.getSystemMessage().equals(Constants.MESSAGE_TYPE_403_FORBIDDEN_ERROR)) {
			status = HttpStatus.FORBIDDEN;
		} else if (result.getSystemMessage().equals(Constants.MESSAGE_TYPE_401_UNAUTHORIZED_ERROR)) {
			status = HttpStatus.UNAUTHORIZED;
		} else if (result.getSystemMessage().equals(Constants.MESSAGE_TYPE_400_BAD_REQUEST_ERROR)) {
			status = HttpStatus.BAD_REQUEST;
		} else if (result.getSystemMessage().equals(Constants.MESSAGE_TYPE_500_INTERNAL_SERVER_ERROR)) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		} else {
			status = HttpStatus.OK;
		}
		return ResponseEntity.status(status).body(result);
	}
}