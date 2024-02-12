package com.enrich.authn.pojo;

import lombok.Data;

@Data
public class ValidateOtpRequest {
	
	
	private String user_id;
	private String otp;
	
}
