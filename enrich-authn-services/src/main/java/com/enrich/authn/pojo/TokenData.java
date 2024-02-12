package com.enrich.authn.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Data;
@Data
public class TokenData {
    @JsonProperty("jwtToken")
    private String jwtToken;

    @JsonProperty("jkey")
    private String jkey;

    @JsonProperty("expiry")
    private long expiry;

    @JsonProperty("partner_code")
    private String partnerCode;
    
    @JsonProperty("partner_channel")
    private String partnerChannel;
    
    @JsonProperty("access_type")
    private String access_type;

}