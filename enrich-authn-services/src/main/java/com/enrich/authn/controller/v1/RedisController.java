package com.enrich.authn.controller.v1;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.util.external.CacheUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/v1")
public class RedisController {
	@Autowired
	CacheUtil  cacheUtil;
	@GetMapping("/redis/{key}")
	public ResponseEntity<?> getRedisValue(@PathVariable String key) {
		String emptyDataJson = "{\"data\":[]}";
		
        Gson gson = new Gson();
        JsonObject ddddd = gson.fromJson(emptyDataJson, JsonObject.class);

        // Convert JsonObject to Map
        Map<String, Object> responseMapemty = gson.fromJson(ddddd, Map.class);

	    if (key != null) {
	        String cacheKey = Constants.COM_ENRICH_AUTHN_API + key + ":" + Constants.JWT_ARRAY;
	        Object cacheValue = cacheUtil.get(cacheKey);
	        
	        if (cacheValue != null) {
	            // Creating a JSON object using Gson
	         //   Gson gson = new Gson();
	            JsonObject jj = gson.fromJson(cacheValue.toString(), JsonObject.class);

	            // Convert JsonObject to Map
	            Map<String, Object> responseMap = gson.fromJson(jj, Map.class);

	            // Return the JSON response as a Map
	            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseMap);
	        } else {
	            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseMapemty);
	        }
	    }
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMapemty);
	}
	}


