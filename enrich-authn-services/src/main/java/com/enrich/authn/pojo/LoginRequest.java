package com.enrich.authn.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
@Data
public class LoginRequest {
	@NotBlank(message = "The user_id is required.")
	@Size(min = 3, max = 20, message = "The user_id must be from 3 to 20 characters.")
	private String user_id;
	
    @NotBlank(message = "The password is required.")
    private String password;

    @NotBlank(message = "The partner code is required.")
    private String partner_code;

    @NotBlank(message = "The partner channel is required.")
    private String partner_channel;

    private String mac_address;

    private String ip_details;
    
}
