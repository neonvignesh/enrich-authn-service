package com.enrich.authn.pojo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponsePojo {

	private String status;

	@JsonProperty("request_date_time")
	private String requestDateTime;

	@JsonProperty("error_message")
	private String message;
}
