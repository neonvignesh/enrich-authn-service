package com.enrich.authn.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GuestOtpValidateRequest {

	@JsonProperty("otp")
	private String otp;
	
	@JsonProperty("user_id")
	private String userId;
}
