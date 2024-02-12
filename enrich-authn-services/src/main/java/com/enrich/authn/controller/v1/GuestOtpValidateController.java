package com.enrich.authn.controller.v1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.pojo.GuestOtpValidateRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.v1.authentication.GuestOptValidateService;
import com.enrich.authn.util.StandardResponseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
public class GuestOtpValidateController {

	@Autowired
	GuestOptValidateService service;

	@PutMapping(value = "/guest/validate/otp", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StandardMessageResponse> otpValidate(HttpServletRequest request, HttpServletResponse response,
			@RequestBody GuestOtpValidateRequest otpValidateRequest) {
		StandardMessageResponse result;
		try {
			result = service.guestOtpValidate(otpValidateRequest);
			return StandardResponseUtil.generateResponseEntity(result);
		} catch (Exception e) {
			log.error("Error occurred in Guest Otp Validate Controller :" + e.getLocalizedMessage());
			result = StandardResponseUtil.prepareInternalServerErrorResponse(e.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}
}
