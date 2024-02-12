package com.enrich.authn.pojo;

import lombok.Data;

@Data
public class UserToken {
	
	    private String jwtToken;
	    private String jkey;
	    private long expiry;
	    private String partner_code;
	    private String partner_channel;
	    private String access_type;
	    
}
