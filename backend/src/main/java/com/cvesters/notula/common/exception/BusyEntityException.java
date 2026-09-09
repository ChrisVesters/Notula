package com.cvesters.notula.common.exception;

public class BusyEntityException extends RuntimeException {

	public BusyEntityException(final String message) {
		super(message);
	}

	public BusyEntityException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
