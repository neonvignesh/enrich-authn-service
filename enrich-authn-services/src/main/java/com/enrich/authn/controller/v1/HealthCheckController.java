package com.enrich.authn.controller.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enrich.authn.constants.Constants;



@RestController
@RequestMapping("/internal")
public class HealthCheckController {
	    @GetMapping("/health")
	    public String healthCheck() {
	        return Constants.OK;
	    }
}
