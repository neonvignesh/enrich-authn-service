package com.enrich.authn.controller.v1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.v1.authentication.TotpService;
import com.enrich.authn.util.StandardResponseUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class TotpController {
	
	private final TotpService totpService;
	
	@PostMapping("/enable/totp")
	public ResponseEntity<?> enableTotp(HttpServletRequest request,@RequestHeader("user-Id") 
    @NotNull(message = "user-Id is required") 
    String userId, @RequestHeader("Authorization") 
    @NotNull(message = "jwtToken is required") 
    String jwtToken)  {
		try {
			final var result = totpService.generateQr(userId,jwtToken);
			return result;
		} catch (Exception e) {
			log.error("Error occured in TotpController controller " + e.getMessage());
			StandardMessageResponse error = StandardResponseUtil.prepareInternalServerErrorResponse();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
		
	}

}
