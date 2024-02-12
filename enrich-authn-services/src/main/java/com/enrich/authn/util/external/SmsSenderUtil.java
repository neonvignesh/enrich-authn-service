package com.enrich.authn.util.external;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class SmsSenderUtil {

	@Autowired
	AuthnServiceProvider provider;

	public ResponseEntity<String> sendSms(Long mobileNo, int otp) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(provider.getSmsSenderUrl())
					.queryParam(Constants.API_KEY, provider.getSmsSenderApiKEy()).queryParam("method", Constants.SMS)
					.queryParam(Constants.MESSAGE,
							"One time password: " + otp
									+ ". Valid for 5 Minutes, Exclusive for Enrich Terms. - Team Enrich.")
					.queryParam(Constants.TO, "91" + mobileNo).queryParam("sender", Constants.ENRICH);

			URI smsSenderUri = builder.build().toUri();
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.getForEntity(smsSenderUri, String.class);
			return response;
		} catch (HttpClientErrorException e) {
			return handleHttpClientError(e, "Sms Sender");
		} catch (Exception e) {
			return handleGenericError(e, "Sms Sender");
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
