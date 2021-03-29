package org.toolup.devops.ci.git.gitlab.apiclient;

public class GITRESTClientRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -813905750533157630L;

	public GITRESTClientRuntimeException() {
		super();
	}

	public GITRESTClientRuntimeException(String message) {
		super(message);
	}
	public GITRESTClientRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public GITRESTClientRuntimeException(Throwable cause) {
		super(cause);
	}
}
