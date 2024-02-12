package com.enrich.authn.util;

import org.springframework.stereotype.Component;

import com.enrich.authn.constants.Constants;
import com.google.gson.JsonObject;

@Component
public class UnauthorizedUtil {
	   public static JsonObject createCustomErrorResponse(String errorMessage) {
	        JsonObject customErrorResponse = new JsonObject();
	        customErrorResponse.addProperty("system_message", Constants.MESSAGE_TYPE_401_UNAUTHORIZED_ERROR);
	        customErrorResponse.addProperty("system_message_type", "failed");
	        customErrorResponse.addProperty("success", false);   
	        JsonObject data = new JsonObject();
	       // data.addProperty("request_date_time", getCurrentDateTime());
	        data.addProperty("status", "Not_Ok");
	        data.addProperty("error_message", errorMessage);
	        
	        customErrorResponse.add("data", data);
	        
	        return customErrorResponse;
	    }

	   
}
