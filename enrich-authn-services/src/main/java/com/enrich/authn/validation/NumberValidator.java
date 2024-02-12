package com.enrich.authn.validation;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enrich.authn.constants.Constants;

/**
 * Number validator
 * 
 * @author Sivaraj
 * @since 09-05-2022
 * @category Validator
 */
@Component
public class NumberValidator implements ConstraintValidator<NumberConstraints, String> {

	@Autowired
	private MessageConstants messageConstants;

	private int length;

	private int min;

	private long max;

	private boolean reqRangeValidation;

	private boolean isMandatory;

	private boolean reqLengthValidation;

	private String regexExp;

	@Override
	public void initialize(NumberConstraints numberConstraints) {
		this.min = numberConstraints.min();
		this.max = numberConstraints.max();
		this.length = numberConstraints.length();
		this.reqLengthValidation = numberConstraints.reqLengthValidation();
		this.reqRangeValidation = numberConstraints.reqRangeValidation();
		this.regexExp = numberConstraints.regexExp();
		this.isMandatory = numberConstraints.isMandatory();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		// Clear the default constraint validation
		context.disableDefaultConstraintViolation();

		// Added Null Check here
		if (StringUtils.isBlank(value)) {
			// If null validation is not required then return true(i.e. further no more
			// validations are required)
			if (!this.isMandatory) {
				return true;
			}
			// Send required error message
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_ID_REQ);
			return false;
		}

		// Required length validation failed if it is exceeded the length
		if (this.reqLengthValidation && value.length() != this.length) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_LENGTH_EXCEEDED);
			return false;
		}

		// Number validation
		if (!value.matches(Constants.NUMBER_REGEX)) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_NOT_VALID);
			return false;
		}

		// Regex validation
		if (StringUtils.isNotBlank(regexExp) && !value.matches(regexExp)) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_NOT_VALID);
			return false;
		}

		try {
			if (StringUtils.isNotBlank(value)
					&& (new BigDecimal(value).longValue() < this.min || new BigDecimal(value).longValue() > this.max)
					&& this.reqRangeValidation) {
				this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_RANGE_FAILED);
				return false;
			}
		} catch (Exception e) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_NOT_VALID);
			return false;
		}
		return true;
	}

	private void buildConstraintsError(ConstraintValidatorContext context, String errorMessage) {
		context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
	}

}
