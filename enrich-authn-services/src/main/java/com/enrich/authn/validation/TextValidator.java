package com.enrich.authn.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TextValidator - Validate the text
 * 
 * @author Sivaraj
 * @since 09-05-2022
 * @category Validator
 */
@Component
public class TextValidator implements ConstraintValidator<TextConstraints, String> {

	@Autowired
	private MessageConstants messageConstants;

	private int length;

	private int min;

	private int max;

	private boolean reqRangeValidation;

	private boolean isMandatory;

	private boolean reqLengthValidation;

	private boolean reqAlphaNumbericValidation;

	private String regexExp;

	@Override
	public void initialize(TextConstraints textConstraints) {
		this.min = textConstraints.min();
		this.max = textConstraints.max();
		this.length = textConstraints.length();
		this.reqAlphaNumbericValidation = textConstraints.reqAlphaNumericValidation();
		this.reqLengthValidation = textConstraints.reqLengthValidation();
		this.reqRangeValidation = textConstraints.reqRangeValidation();
		this.regexExp = textConstraints.regexExp();
		this.isMandatory = textConstraints.isMandatory();
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
		if (StringUtils.isNotBlank(value) && this.reqLengthValidation && value.length() != this.length) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_LENGTH_EXCEEDED);
			return false;
		}

		// Range validation for property length which should meet minimum length
		if (StringUtils.isNotBlank(value) && this.reqRangeValidation && value.length() < this.min) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_RANGE_MIN);
			return false;
		}

		// Range validation for property length which should not maximum length
		if (StringUtils.isNotBlank(value) && this.reqRangeValidation && value.length() > this.max) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_RANGE_MAX);
			return false;
		}

		// Allowed only alpha numberic value and space is not allowed
		if (this.reqAlphaNumbericValidation && !StringUtils.isNumeric(value)) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_NOT_VALID);
			return false;
		}

		// Regex validation
		if (StringUtils.isNotBlank(regexExp) && !value.matches(regexExp)) {
			this.buildConstraintsError(context, messageConstants.ERROR_VALIDATION_PROPERTY_NOT_VALID);
			return false;
		}
		return true;
	}

	private void buildConstraintsError(ConstraintValidatorContext context, String errorMessage) {
		context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
	}

}
