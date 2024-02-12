package com.enrich.authn.util;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

public class Validator {

	public static boolean hasData(String s) {
		if (s != null && s.trim().length()>0){	
			return true;
		}
		return false;			
	}
	public static boolean hasData(List list) {
		if (list != null && list.size()>0){	
			return true;
		}
		return false;			
	}
	
	public static boolean hasData(Map map) {
		if (map != null && map.size()>0){	
			return true;
		}
		return false;			
	}
	
	
	public static boolean hasData(JsonObject s) {
		if (s != null && s.size()>0){	
			return true;
		}
		return false;			
	}
	
	public static boolean hasData(JsonNode s) {
		if (s != null && s.size()>0){	
			return true;
		}
		return false;			
	}
	
	
	public static boolean hasData(Object s) {
		if (s != null ){	
			return true;
		}
		return false;			
	}
	
	public static boolean hasData(Long s) {
		if (s != null ){	
			return true;
		}
		return false;			
	}
}
