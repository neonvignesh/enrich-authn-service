package com.enrich.authn.util;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class GenericUtil {
	
	public static List<Double> convertStringToDoubleList(String value){
		List<String> items = Stream.of(value.replaceAll("^\"+|\"+$", "").split(","))
	   		     .map(String::trim)
	   		     .collect(Collectors.toList());
		List<Double> doubleList = items.stream().map(Double::valueOf).collect(Collectors.toList());
		return doubleList;
	}
	
	public static List<String> convertStringToStringList(String value){
		List<String> stringList = Stream.of(value.replaceAll("^\"+|\"+$", "").split(","))
   		     .map(String::trim)
   		     .collect(Collectors.toList());
		return stringList;
	}
	
	public static List<Double> convertJsonElementToDoubleList(JsonElement jsonElement) {
		Gson gson = new Gson();
		Type listTypeDouble = new TypeToken<List<Double>>() {}.getType();
			
		return gson.fromJson(jsonElement, listTypeDouble);
	}
	
	public static List<String> convertJsonElementToStringList(JsonElement jsonElement) {
		Gson gson = new Gson();
		Type listTypeString = new TypeToken<List<String>>() {}.getType();
		
		return gson.fromJson(jsonElement, listTypeString);
	}	
}