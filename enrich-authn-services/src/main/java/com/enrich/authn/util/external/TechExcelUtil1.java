package com.enrich.authn.util.external;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TechExcelUtil1 {
	
	
	public  String getClientInfo(String user_id) {
		
		
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://dev.cluster.apps.enrichmoney.in/tep-reader-api/internal/profile/pan/details");
		HttpHeaders headers = new HttpHeaders();
		headers.set("user-Id", user_id);
	    headers.set("x-api-key", "a4b97a24awq");
	    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<?> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				requestEntity, String.class);
		String customResponse = responseEntity.getBody().toString();
		//System.out.println("\nhelllo teckexcel"+customResponse);
		return customResponse;
	}

}
