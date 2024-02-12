package com.enrich.authn.controller.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.pojo.TokenRequest;
import com.enrich.authn.service.v1.authentication.AccessTokenService;
import com.enrich.authn.util.StandardResponseUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
@Validated
@Slf4j
public class AccessTokenController {


	private final AccessTokenService service ;

	@CrossOrigin()
	@PostMapping(value = "/access-token")
	public ResponseEntity<StandardMessageResponse> accessToken(@RequestBody TokenRequest tokenrequest,
			@RequestHeader(value = "Authorization", defaultValue = "") String jwtToken,
			@RequestHeader(value = "user-Id", defaultValue = "") @NotNull(message = "user-Id is require") String userId) {
		try {
			final var result = service.generateAccessToken(jwtToken, tokenrequest);
			return ResponseEntity.status(HttpStatus.OK).body(result);
		} catch (Exception e) {
			log.error("Error occurred in AuthenticationController: {}", e.getMessage());
			StandardMessageResponse error = StandardResponseUtil.prepareInternalServerErrorResponse();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}
	
	@CrossOrigin()
	@GetMapping(value = "/session/access-token")
	public ResponseEntity<StandardMessageResponse> getAccessToken(
			@RequestHeader(value = "Authorization", defaultValue = "") String jwtToken,
			@RequestHeader(value = "user-Id", defaultValue = "") @NotNull(message = "user-Id is require") String userId) {
		try {
			StandardMessageResponse result = service.getAccessToken(jwtToken,userId);
			return ResponseEntity.status(HttpStatus.OK).body(result);
		} catch (Exception e) {
			log.error("Error occurred in getAccessTokenController: {}", e.getMessage());
			StandardMessageResponse error = StandardResponseUtil.prepareInternalServerErrorResponse();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

}
