package com.enrich.authn.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JsonParseUtil {

	
	 private static ObjectMapper objectMapper = new ObjectMapper();

	    public static JsonNode parseResponseToJsonNode(JsonObject response) {
	        try {
	            return objectMapper.readTree(response.toString());
	        } catch (JsonProcessingException e) {
	            log.error("Error occurred while parseResponseToJsonNode: {}", e.getMessage());
	            return null; 
	        }
	    }
}
