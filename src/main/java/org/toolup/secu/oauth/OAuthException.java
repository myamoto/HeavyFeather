package org.toolup.secu.oauth;

import org.toolup.network.http.AbstractHttpException;

public class OAuthException extends AbstractHttpException{

	private static final long serialVersionUID = -5450648683916650215L;
	
	private static final int DEFAULT_HTTP_STATUS = 403;
	
	public OAuthException(String msg, Throwable cause, int httpStatus) {
		super(msg, cause, httpStatus);
	}

	public OAuthException(Throwable cause, int httpStatus) {
		super(cause, httpStatus);
	}
	
	public OAuthException(Throwable cause) {
		super(cause, DEFAULT_HTTP_STATUS);
	}
	
	public OAuthException(String msg) {
		super(msg, DEFAULT_HTTP_STATUS);
	}
	
	public OAuthException(String msg, int httpStatus) {
		super(msg, httpStatus);
	}

}
