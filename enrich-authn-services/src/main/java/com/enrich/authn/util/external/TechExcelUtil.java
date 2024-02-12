package com.enrich.authn.util.external;

import javax.print.attribute.standard.Media;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.util.UnauthorizedUtil;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TechExcelUtil {
	
	
	
	
	
	
	
	
	public String enableTOTP(String userId) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://192.168.1.197:8686/techexcelapi/index.cfm/DynamicHelp/DynamicHelp1")
					.queryParam("TABLE_NAME", "Capsfo.dbo.CLIENT_DETAILS")
					.queryParam("COLUMN_NAME", "CLIENT_ID,PAN_NO")
			.queryParam("FILTER1COL", "CLIENT_ID")
			.queryParam("FILTER1TYPE", "=")
			.queryParam("FILTER1VALUE", userId.toString())
			.queryParam("FILTER2COL", "")
			.queryParam("FILTER2TYPE", "")
			.queryParam("FILTER2VALUE", "")
			.queryParam("SEARCH1COL", "")
			.queryParam("SEARCH1TYPE", "")
			.queryParam("SEARCH1VALUE", "")
			.queryParam("SEARCH2COL", "")
			.queryParam("SEARCH2TYPE", "")
			.queryParam("SEARCH2VALUE", "")
			.queryParam("UrlUserName", "TECHAPI")
			.queryParam("UrlPassword", "TECH@123")
			.queryParam("UrlDatabase", "capsfo")
			.queryParam("UrlDataYear", "2023");

			HttpHeaders headers = new HttpHeaders();
		
		//	headers.set(Constants.JKEY_TOKEN, jKey);
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<?> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
					requestEntity, String.class);
			String customResponse = responseEntity.getBody().toString();
			
			System.out.println("\nhelllo teckexcel"+customResponse);
			return customResponse;
		}
		catch (HttpClientErrorException e) {
			if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				return UnauthorizedUtil.createCustomErrorResponse(Constants.INVALID_TOKEN).toString();
			} else {
				log.error("Error occurred in executeHydraRequest: URL: {} {}", e.getMessage());
				return new Gson().fromJson(e.getResponseBodyAsString(), String.class);
			}
		} catch (Exception e) {
			log.error("Error occurred in executeHydraRequest: {}", e.getMessage());
			return new Gson().fromJson(e.getMessage(), String.class);
		}
	}

}
