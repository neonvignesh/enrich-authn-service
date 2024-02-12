package com.enrich.authn.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, ANNOTATION_TYPE, PARAMETER, TYPE_USE })
@Constraint(validatedBy = NumberValidator.class)
public @interface NumberConstraints {

	// Error Message
	String message() default "";

	// Name of the property
	String propertyName() default "";

	// Regex expression of the property
	String regexExp() default "";

	// Length of the property
	int length() default 0;

	int min() default 0;

	long max() default 1000;

	// Flag to specify the Length validation is required or not
	boolean reqLengthValidation() default false;

	// Flag to specify the mandatory validation is required or not
	boolean isMandatory() default true;

	// Flag to specify the Range validation is required or not
	boolean reqRangeValidation() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
