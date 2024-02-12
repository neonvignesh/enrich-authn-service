package com.enrich.authn.exception;

public class ResourceFoundException extends RuntimeException {

	private static final long serialVersionUID = 1750956588519882941L;

	public ResourceFoundException() {
		super();
	}

	public ResourceFoundException(String message) {
		super(message);
	}

	public ResourceFoundException(Throwable cause) {
		super(cause);
	}

	public ResourceFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
