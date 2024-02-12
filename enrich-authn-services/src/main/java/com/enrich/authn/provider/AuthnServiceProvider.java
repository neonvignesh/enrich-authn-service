package com.enrich.authn.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Configuration
@Data
public class AuthnServiceProvider {


	@Value("${hydra.Login.url}")
	private String authenticateUrl;

	@Value("${hydra.changepassword.url}")
	private String changeUrl;

	@Value("${hydra.forgotpassword.url}")
	private String forgotUrl;

	@Value("${hydra.logout.url}")
	private String logoutUrl;
	
	@Value("${hydra.authenticate.url}")
	private String authnUrl;

	@Value("${hydra.api.key}")
	private String hydraApiKey;

	@Value("${jwt.secretkey}")
	private String secretKey;

	@Value("${hydra.generateotp.url}")
	private String generateOtpUrl;
	
	@Value("${hydra.mwlist.url}")
	private String mwlistUrl;
	
	@Value("${hydra.totp.url}")
	private String totp;

	@Value("${rsa.publickey}")
	private String publickey;
	
	@Value("${rsa.privatekey}")
	private String privatekey;
	
	@Value("${sms.sender.url}")
	private String smsSenderUrl;
	
	@Value("${sms.sender.api-key}")
	private String smsSenderApiKEy;
	
	@Value("${email.sender.url}")
	private String emailSenderUrl;
	
	@Value("${email.sender.api-key}")
	private String emailSenderApiKEy;
}

