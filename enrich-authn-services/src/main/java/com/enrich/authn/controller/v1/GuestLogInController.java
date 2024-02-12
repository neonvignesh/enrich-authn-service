package com.enrich.authn.controller.v1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.pojo.GuestOtpRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.v1.authentication.GuestLogInService;
import com.enrich.authn.util.StandardResponseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
public class GuestLogInController {

	@Autowired
	GuestLogInService service;

	@PostMapping(value = "/guest/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StandardMessageResponse> guestLogIn(HttpServletRequest request, HttpServletResponse response,
			@RequestBody GuestOtpRequest logInRequest) {
		StandardMessageResponse result;
		try {
			result = service.guestLogin(logInRequest);
			return StandardResponseUtil.generateResponseEntity(result);
		} catch (Exception e) {
			log.error("Error occurred in Guest Login Controller :" + e.getLocalizedMessage());
			result = StandardResponseUtil.prepareInternalServerErrorResponse(e.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}

}
