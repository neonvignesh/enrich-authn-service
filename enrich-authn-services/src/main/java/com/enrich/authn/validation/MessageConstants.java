package com.enrich.authn.validation;

import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * MessageConstants - Generic constants and error messages
 * 
 * @author Sivaraj
 * @since 09-05-2022
 * @category Validation Error Constant
 */
@Component
public final class MessageConstants implements InitializingBean {

	// Property validation error key
	public String ERROR_VALIDATION_PROPERTY_ID_REQ = "fundmanager.validation.error.property.required";
	public String ERROR_VALIDATION_PROPERTY_NOT_VALID = "fundmanager.validation.error.property.notvalid";
	public String ERROR_VALIDATION_PROPERTY_RANGE_FAILED = "fundmanager.validation.error.property.range.failed";
	public String ERROR_VALIDATION_PROPERTY_RANGE_MIN = "fundmanager.validation.error.property.range.min.failed";
	public String ERROR_VALIDATION_PROPERTY_RANGE_MAX = "fundmanager.validation.error.property.range.max.failed";
	public String ERROR_VALIDATION_PROPERTY_LENGTH_EXCEEDED = "fundmanager.validation.error.property.length.exceeded";

	// Common Error Message
	public String COMMON_ERROR = "fundmanager.common.error";

	@Autowired
	private MessageSource messageSource;

	@Override
	public void afterPropertiesSet() {
		ERROR_VALIDATION_PROPERTY_ID_REQ = messageSource.getMessage(ERROR_VALIDATION_PROPERTY_ID_REQ, null,
				ERROR_VALIDATION_PROPERTY_ID_REQ, Locale.getDefault());
		ERROR_VALIDATION_PROPERTY_NOT_VALID = messageSource.getMessage(ERROR_VALIDATION_PROPERTY_NOT_VALID, null,
				ERROR_VALIDATION_PROPERTY_NOT_VALID, Locale.getDefault());
		ERROR_VALIDATION_PROPERTY_RANGE_FAILED = messageSource.getMessage(ERROR_VALIDATION_PROPERTY_RANGE_FAILED, null,
				ERROR_VALIDATION_PROPERTY_RANGE_FAILED, Locale.getDefault());
		ERROR_VALIDATION_PROPERTY_RANGE_MIN = messageSource.getMessage(ERROR_VALIDATION_PROPERTY_RANGE_MIN, null,
				ERROR_VALIDATION_PROPERTY_RANGE_MIN, Locale.getDefault());
		ERROR_VALIDATION_PROPERTY_RANGE_MAX = messageSource.getMessage(ERROR_VALIDATION_PROPERTY_RANGE_MAX, null,
				ERROR_VALIDATION_PROPERTY_RANGE_MAX, Locale.getDefault());
		ERROR_VALIDATION_PROPERTY_LENGTH_EXCEEDED = messageSource.getMessage(ERROR_VALIDATION_PROPERTY_LENGTH_EXCEEDED,
				null, ERROR_VALIDATION_PROPERTY_LENGTH_EXCEEDED, Locale.getDefault());
		COMMON_ERROR = messageSource.getMessage(COMMON_ERROR, null, COMMON_ERROR, Locale.getDefault());
	}

	public String buildParamMessage(String meesageKey, String... params) {
		return messageSource.getMessage(meesageKey, params, meesageKey, Locale.getDefault());
	}
}
