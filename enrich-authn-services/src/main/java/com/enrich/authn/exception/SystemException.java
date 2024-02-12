package com.enrich.authn.exception;

public class SystemException extends Throwable {
	
	private static final long serialVersionUID = 1750956588519882941L;

	public SystemException() {
		super();
	}

	public SystemException(String message) {
		super(message);
	}

	public SystemException(Throwable cause) {
		super(cause);
	}

	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}
}
