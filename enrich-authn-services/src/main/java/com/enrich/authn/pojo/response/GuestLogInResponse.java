package com.enrich.authn.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GuestLogInResponse {
	
	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("message")
	private String message;

}
