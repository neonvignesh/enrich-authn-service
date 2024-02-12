package com.enrich.authn.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GuestOtpRequest {
	
	@JsonProperty("mobile_number")
	private Long mobileNumber;
	
	@JsonProperty("email_id")
	private String emailId;
	
	@JsonProperty("context_type")
	private String contextType;
	
}
