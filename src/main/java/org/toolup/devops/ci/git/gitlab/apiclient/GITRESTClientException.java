package org.toolup.devops.ci.git.gitlab.apiclient;

public class GITRESTClientException extends Exception {

	private static final long serialVersionUID = -2357832945523200250L;

	public GITRESTClientException() {
		super();
	}

	public GITRESTClientException(String message) {
		super(message);
	}
	public GITRESTClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public GITRESTClientException(Throwable cause) {
		super(cause);
	}
}
