package com.enrich.authn.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TokenDataList {
    @JsonProperty("data")
    private TokenData[] data;

    // Getters and setters for the 'data' field
}
