package com.enrich.authn.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
  
	 @NotBlank(message = "User ID cannot be blank")
	   private String user_id;
	   
	   @NotBlank(message = "PAN number cannot be blank")
	   @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN number format")
	   private String pan_no;
	   
	   @NotBlank(message = "Date of Birth cannot be blank")
	   @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[0-2])[0-9]{4}$", message = "Invalid date of birth format. Use ddmmyyyy")
	   private String dob;
	   

	   

}
