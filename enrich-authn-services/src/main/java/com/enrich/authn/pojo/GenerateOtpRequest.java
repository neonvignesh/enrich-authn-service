package com.enrich.authn.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class GenerateOtpRequest {
	
	   @NotBlank(message = "User ID cannot be blank")
	   private String user_id;
	
	   @NotBlank(message = "PAN number cannot be blank")
	   @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN number format")
	   private String pan_no;
}
