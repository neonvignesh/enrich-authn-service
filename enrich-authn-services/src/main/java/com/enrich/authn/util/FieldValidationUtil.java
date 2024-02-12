package com.enrich.authn.util;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.stereotype.Component;

import com.enrich.authn.pojo.StandardMessageResponse;

@Component
public class FieldValidationUtil {
	
	  public static <T> Set<ConstraintViolation<T>> validate(T object) {
	        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	        return validator.validate(object);
	    }

	    public static <T> StandardMessageResponse handleValidationErrors(Set<ConstraintViolation<T>> violations) {
	        return StandardResponseUtil.prepareBadRequestResponseList(
	                MessageUtil.getErrorMessage(
	                        violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "))));
	    }

}
