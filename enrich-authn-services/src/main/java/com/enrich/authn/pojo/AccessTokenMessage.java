package com.enrich.authn.pojo;

import lombok.Data;

@Data
public class AccessTokenMessage {
	
	private String message;
	private String access_token;

}
