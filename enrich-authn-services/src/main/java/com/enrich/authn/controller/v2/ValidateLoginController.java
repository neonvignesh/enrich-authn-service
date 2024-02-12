package com.enrich.authn.controller.v2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.v2.authentication.ValidateLoginService;
import com.enrich.authn.util.HttpStatusUtil;
import com.enrich.authn.util.StandardResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2")
@Validated
@Slf4j
public class ValidateLoginController {
	
	private final ValidateLoginService service;

    @CrossOrigin()
    @PostMapping(value = "/login")
    public ResponseEntity<StandardMessageResponse> authenticate(@RequestBody AuthnEncryptRequest authnEncryptRequest) {
        try {
            final var result = service.authenticate(authnEncryptRequest);
            return ResponseEntity.status(HttpStatusUtil.getHttpStatusFromSystemMessage(result.getSystemMessage())).body(result);
        } catch (Exception e) {
            log.error("Error occurred in ValidateLoginController: {}", e.getMessage());
            final var error = StandardResponseUtil.prepareInternalServerErrorResponse();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
