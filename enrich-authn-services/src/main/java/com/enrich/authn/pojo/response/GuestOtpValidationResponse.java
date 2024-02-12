package com.enrich.authn.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GuestOtpValidationResponse {

	@JsonProperty("user-Id")
	private String userId;

	@JsonProperty("message")
	private String message;

	@JsonProperty("x_authorization")
	private String xAuthorization;

}
