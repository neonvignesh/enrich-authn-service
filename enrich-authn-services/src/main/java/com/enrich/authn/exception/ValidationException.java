package com.enrich.authn.exception;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1750956588519882941L;

	public ValidationException() {
		super();
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
