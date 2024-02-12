package com.enrich.authn.util.external;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.enrich.authn.constants.Constants;
import com.enrich.authn.provider.AuthnServiceProvider;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailSenderUtil {

	@Autowired
	AuthnServiceProvider provider;

	public ResponseEntity<String> sendSms(String emailID, int otp) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(provider.getEmailSenderUrl());
			URI smsSenderUri = builder.build().toUri();
			RestTemplate restTemplate = new RestTemplate();
			Map<String, Object> requestBody = new HashMap<>();
			List<String> toEmailList = Collections.singletonList(emailID);
			requestBody.put("to_email", toEmailList);
			requestBody.put("cc_email", Collections.emptyList());
			requestBody.put("subject", "One Time Password");
			requestBody.put("body",
					"One time password: " + otp + ". Valid for 5 Minutes, Exclusive for Enrich Terms. - Team Enrich.");
			requestBody.put("source", "email");
			requestBody.put("mime_type", "text/plain");
			requestBody.put("attachments", Collections.emptyList());
			requestBody.put("attachmentFileNameWithExtension", Collections.emptyList());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(Constants.X_API_KEY, provider.getEmailSenderApiKEy());
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(smsSenderUri, requestEntity, String.class);
			return response;
		} catch (HttpClientErrorException e) {
			return handleHttpClientError(e, "Email Sender");
		} catch (Exception e) {
			return handleGenericError(e, "Email Sender");
		}
	}

	private ResponseEntity<String> handleHttpClientError(HttpClientErrorException e, String service) {
		if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Constants.MESSAGE_TYPE_401_UNAUTHORIZED_ERROR);
		}
		if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getResponseBodyAsString());
		} else {
			log.error("Error Occurred on " + service + ": " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Constants.MESSAGE_TYPE_500_INTERNAL_SERVER_ERROR);
		}
	}

	private ResponseEntity<String> handleGenericError(Exception e, String service) {
		log.error("Error Occurred on " + service + ": " + e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Constants.MESSAGE_TYPE_500_INTERNAL_SERVER_ERROR);
	}

}
