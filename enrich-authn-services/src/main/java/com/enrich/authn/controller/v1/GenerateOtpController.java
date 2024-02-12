package com.enrich.authn.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.v1.authentication.GenerateOtpService;
import com.enrich.authn.util.HttpStatusUtil;
import com.enrich.authn.util.StandardResponseUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GenerateOtpController {
@Autowired
private final GenerateOtpService generateOtpService;

@CrossOrigin()
@RequestMapping(value = "otp/generate", method = RequestMethod.POST, produces = "application/json")
public ResponseEntity<StandardMessageResponse> forgotPassword(
		@RequestBody AuthnEncryptRequest authnEncryptRequest) {
	try {
		final var result = generateOtpService.generateOtp(authnEncryptRequest);
		return ResponseEntity.status(HttpStatusUtil.getHttpStatusFromSystemMessage(result.getSystemMessage())).body(result);

	} catch (Exception e) {
		log.error("Error occurred in GenerateOtpController:" + e.getMessage());
		final var error  = StandardResponseUtil.prepareInternalServerErrorResponse();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}



}
