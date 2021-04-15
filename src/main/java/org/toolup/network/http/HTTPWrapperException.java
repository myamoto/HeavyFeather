package org.toolup.network.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;


public class HTTPWrapperException extends Exception{

	private static final long serialVersionUID = -3000391496445588070L;

	public enum HTTPVERB{GET, POST, DELETE, PATCH}
	
	private final HTTPVERB verb;
	private final int statusCode;
	private final String url;
	private final String responseContent;
	private final String xError;
	private final List<Header> headerList = new ArrayList<>();
	
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Throwable t, Header... headerList) {
		this(verb, xError, statusCode, url, responseContent, t, null, headerList);
	}
		
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Throwable t, String msg, Header... headerList) {
		super(t);
		this.verb = verb;
		this.statusCode = statusCode;
		this.url = url;
		this.responseContent = responseContent;
		this.xError = xError;
		this.headerList.addAll(Arrays.asList(headerList));
	}
	
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Header... headerList) {
		this(verb, xError, statusCode, url, responseContent, null, headerList);
	}
	
	public HTTPWrapperException(HTTPVERB verb, String url, Throwable t, String msg) {
		this(verb, null, -1, url, msg, t);
	}
	
	public HTTPWrapperException(HTTPVERB verb, String url, Throwable t) {
		this(verb, url, t, null);
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		
		return String.format("%sHTTP status %d using %s on url [%s], probable cause [%s]. response headers : %s"
				, (msg != null ? msg + " : " : "")
				, statusCode
				, verb
				, url
				, xError != null ? String.format("X-Error = %s", xError) : String.format("response content = %s", responseContent)
				, getHeadersString());
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

	public int getStatusCode() {
		return statusCode;
	}

	public String getResponseContent() {
		return responseContent;
	}
	
}
