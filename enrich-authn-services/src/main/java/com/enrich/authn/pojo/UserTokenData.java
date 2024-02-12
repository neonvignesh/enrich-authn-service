package com.enrich.authn.pojo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
@lombok.Data
public class UserTokenData {
	 private List<UserToken> data = new ArrayList();
	 
		
	    public String toJson() throws JsonProcessingException {
	        ObjectMapper objectMapper = new ObjectMapper();
	        return objectMapper.writeValueAsString(this);
	    }

	    public static UserTokenData fromJson(String json) throws JsonProcessingException {
	        ObjectMapper objectMapper = new ObjectMapper();
	        return objectMapper.readValue(json, UserTokenData.class);
	    }
}


	

