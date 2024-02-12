package com.enrich.authn.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1750956588519882941L;

	public ResourceNotFoundException() {
		super();
	}

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(Throwable cause) {
		super(cause);
	}

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
