package com.enrich.authn.controller.intenal;

import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.internal.JkeyVerifyService;
import com.enrich.authn.util.HttpStatusUtil;
import com.enrich.authn.util.StandardResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal")
@Validated
@Slf4j
@RequiredArgsConstructor
public class JsonTokenController {

	private final JkeyVerifyService service;
	
	@CrossOrigin()
	@GetMapping(value = "/session/token")
	public ResponseEntity<StandardMessageResponse> getLoginAuthn(
			@RequestHeader(value = "Authorization", defaultValue = "") String jwtToken) {
		try {
			final var result = service.getJkey(jwtToken);
			return ResponseEntity.status(HttpStatusUtil.getHttpStatusFromSystemMessage(result.getSystemMessage())).body(result);
		} catch (Exception e) {
			log.error("Error occured in JsonTokenController {} ", e.getMessage());
			final var error = StandardResponseUtil.prepareInternalServerErrorResponse();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@CrossOrigin()
	@GetMapping("/verify")
	public ResponseEntity<StandardMessageResponse> verifyJwt(
			@RequestHeader(value = "Authorization", defaultValue = "") String jwtToken,
			@RequestHeader(value = "user-Id", defaultValue = "") @NotNull(message = "user-Id is require") String userId) {
		try {
			final var result = service.verifyJwt(jwtToken, userId);
			return ResponseEntity.status(HttpStatusUtil.getHttpStatusFromSystemMessage(result.getSystemMessage()))
					.body(result);
		} catch (Exception e) {
			log.error("Error occured in JsonTokenController {} ", e.getMessage());
			final var error = StandardResponseUtil.prepareInternalServerErrorResponse();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}

	}
}
