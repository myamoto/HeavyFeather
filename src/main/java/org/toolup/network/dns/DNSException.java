package org.toolup.network.dns;

import org.toolup.network.http.AbstractHttpException;

public class DNSException extends AbstractHttpException{

	private static final long serialVersionUID = 7606500524046359944L;

	public DNSException(String msg, int httpStatus) {
		super(msg, httpStatus);
	}
}
