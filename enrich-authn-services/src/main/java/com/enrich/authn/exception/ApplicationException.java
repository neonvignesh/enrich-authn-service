package com.enrich.authn.exception;

public class ApplicationException extends Exception {

	private static final long serialVersionUID = 1750956588519882941L;

	public ApplicationException() {
		super();
	}

	public ApplicationException(String message) {
		super(message);
	}

	public ApplicationException(Throwable cause) {
		super(cause);
	}

	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

}
