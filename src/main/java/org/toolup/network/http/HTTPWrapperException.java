package org.toolup.network.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.springframework.http.HttpStatus;


public class HTTPWrapperException extends AbstractHttpException{

	private static final long serialVersionUID = -3000391496445588070L;

	public enum HTTPVERB{GET, POST, DELETE, PATCH, PUT;

		public static  HTTPVERB from(String method) {
		return HTTPVERB.valueOf(method.toUpperCase());
	}
	}
	
	private final HTTPVERB verb;
	private final String url;
	private final String responseContent;
	private final String xError;
	private final List<Header> headerList = new ArrayList<>();
	
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Throwable t, Header... headerList) {
		this(verb, xError, statusCode, url, responseContent, t, null, headerList);
	}
		
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Throwable t, String msg, Header... headerList) {
		super(msg, t, statusCode);
		this.verb = verb;
		this.url = url;
		this.responseContent = responseContent;
		this.xError = xError;
		this.headerList.addAll(Arrays.asList(headerList));
	}
	
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Header... headerList) {
		this(verb, xError, statusCode, url, responseContent, null, headerList);
	}
	
	public HTTPWrapperException(HTTPVERB verb, String url, Throwable t, String msg) {
		this(verb, null, HttpStatus.INTERNAL_SERVER_ERROR.value(), url, null, t, msg);
	}
	
	public HTTPWrapperException(HTTPVERB verb, String url, Throwable t) {
		this(verb, url, t, null);
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		
		return String.format("%sHTTP status %d using %s on url [%s], probable cause [%s]. response headers : %s"
				, (msg != null ? msg + " : " : "")
				, getHttpStatus()
				, verb
				, url
				, xError != null ? String.format("X-Error = %s", xError) : String.format("response content = %s", responseContent)
				, getHeadersString());
	}
	
	public String getxError() {
		return xError;
	}

	public HTTPVERB getVerb() {
		return verb;
	}

	public String getUrl() {
		return url;
	}

	private String getHeadersString() {
		return headerList != null ? headerList.toString() : "";
	}
	
	public List<Header> getHeaderList() {
		return headerList;
	}

	public String getResponseContent() {
		return responseContent;
	}
	
	public int getStatusCode() {
		return getHttpStatus();
	}
	
}
