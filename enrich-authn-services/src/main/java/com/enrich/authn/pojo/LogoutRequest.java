package com.enrich.authn.pojo;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LogoutRequest {
   @NotBlank(message = "The user ID is required.")
    private String user_id;
	private Boolean isAllDevice;
	private String mac_address; 
	private String ip_details; 
	
}
