package org.toolup.network.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
	private List<? extends Header> reqHeaders = new ArrayList<>();
	private final String reqUrl;
	
	private final String respContent;
	private final String respXError;
	private final List<Header> responseHeaderList = new ArrayList<>();
	
	
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String reqUrl, String respContent, Throwable t, String msg, Header... respHeaderList) {
		this(null, verb, xError, statusCode, reqUrl, respContent, t, msg, respHeaderList);
	}
		
		
	public HTTPWrapperException(HTTPVERB verb, String xError, int statusCode, String url, String responseContent, Throwable t, Header... respHeaderList) {
		this(null, verb, xError, statusCode, url, responseContent, t, null, respHeaderList);
	}
		
	public HTTPWrapperException(List<? extends Header> reqHeaders, HTTPVERB verb, String xError, int statusCode, String reqUrl, String respContent, Throwable t, String msg, Header... responseHeaderList) {
		super(msg, t, statusCode);
		this.reqHeaders = reqHeaders;
		this.verb = verb;
		this.reqUrl = reqUrl;
		this.respContent = respContent;
		this.respXError = xError;
		this.responseHeaderList.addAll(Arrays.asList(responseHeaderList));
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
		
//		return String.format("%sHTTP status %d using %s on url [%s], probable cause [%s]. response headers : %s"
//				, (msg != null ? msg + " : " : "")
//				, getHttpStatus()
//				, verb
//				, url
//				, responseXError != null ? String.format("X-Error = %s", responseXError) : String.format("response content = %s", responseContent)
//				, getHeadersString());
		
		
		return String.format("%s%s Exception 'invalid HTTP Status' : %s -> %d%n"
		+ "req.hdrs :%s%n"
		+ "resp.content :%s%n"
		+ "resp.xError :%s%n"
		+ "resp.hdrs :%s"
		, (msg != null ? msg + " : " : "")
		, getVerb()
		, reqUrl
		, getHttpStatus()
		, prettyHeaders(reqHeaders)
		, respContent == null || respContent.isBlank() ? "" : "\n  " + respContent
		, respXError == null || respXError.isBlank() ? "" : "\n  " + respXError
		, prettyHeaders(responseHeaderList));
	}
	
	private static String prettyHeaders(List<? extends Header> hdrs) {
		return hdrs == null || hdrs.isEmpty() ? "" : 
			"\n  -" + StringUtils.join(
					hdrs
					.stream()
					.map(h -> String.format("%s:%s",h.getName(), h.getValue()))
					.collect(Collectors.toList()), "\n  -");
	}
	
	public String getxError() {
		return respXError;
	}

	public HTTPVERB getVerb() {
		return verb;
	}

	public String getUrl() {
		return reqUrl;
	}

	public List<Header> getHeaderList() {
		return responseHeaderList;
	}

	public String getResponseContent() {
		return respContent;
	}
	
	public int getStatusCode() {
		return getHttpStatus();
	}
	
}
