package org.toolup.network.http;

public abstract class AbstractHttpException extends Exception {
	
	private static final long serialVersionUID = 2613938799023371661L;
	
	private final int httpStatus;

	public AbstractHttpException(String msg, int httpStatus) {
		super(msg);
		this.httpStatus = httpStatus;
	}

	public AbstractHttpException(Throwable cause, int httpStatus) {
		super(cause);
		this.httpStatus = httpStatus;
	}
	public AbstractHttpException(String msg, Throwable cause, int httpStatus) {
		super(msg, cause);
		this.httpStatus = httpStatus;
	}

	public int getHttpStatus() {
		return httpStatus;
	}
}
