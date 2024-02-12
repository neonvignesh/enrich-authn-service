package com.enrich.authn.pojo;

import com.enrich.authn.pojo.LoginAuthnResponse.ResponseData;

import lombok.Data;
@Data
public class AcessTokenResponse {
	
	
	//public class LoginAuthnResponse {
	    private boolean success;
	    private String systemMessage;
	    private String systemMessageType;
	    private ResponseData data;

	    @Data
	    public static class AccessResponseData {
	        private String message;
	        private String access_token;
	    }
	}


