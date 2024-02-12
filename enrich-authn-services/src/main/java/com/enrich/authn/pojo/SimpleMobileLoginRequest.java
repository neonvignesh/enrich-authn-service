package com.enrich.authn.pojo;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SimpleMobileLoginRequest {
	  private String user_id;
	  private String password;
    @NotBlank(message = "The partner code is required.")
    private String partner_code;

    @NotBlank(message = "The partner channel is required.")
    private String partner_channel;
  
}
