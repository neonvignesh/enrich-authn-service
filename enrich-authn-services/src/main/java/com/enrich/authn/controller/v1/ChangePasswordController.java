package com.enrich.authn.controller.v1;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.enrich.authn.pojo.AuthnEncryptRequest;
import com.enrich.authn.pojo.StandardMessageResponse;
import com.enrich.authn.service.v1.authentication.ChangePasswordService;
import com.enrich.authn.util.HttpStatusUtil;
import com.enrich.authn.util.StandardResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ChangePasswordController {
	private final ChangePasswordService changePasswordService;
	@CrossOrigin()
	@RequestMapping(value = "password/change", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<StandardMessageResponse> changePassword(
			@RequestBody AuthnEncryptRequest authnEncryptRequest) {
		try {
			final var result = changePasswordService.changePassword(authnEncryptRequest);
			return ResponseEntity.status(HttpStatusUtil.getHttpStatusFromSystemMessage(result.getSystemMessage())).body(result);

		} catch (Exception e) {
			log.error("Error occurred in changePassword Controller:" + e.getMessage());
			final var error = StandardResponseUtil.prepareInternalServerErrorResponse();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}
}
