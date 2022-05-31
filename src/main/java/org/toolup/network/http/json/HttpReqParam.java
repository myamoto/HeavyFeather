package org.toolup.network.http.json;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;

public class HttpReqParam <T>{

	private HttpClientContext httpClContext;
	private ContentType contentType;
	private String url;
	private Object body;
	private List<NameValuePair> reqParams;
	private List<NameValuePair> postParams;
	private List<Header> headers;
	private Class<T> clazz;

	public HttpReqParam(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public HttpClientContext getHttpClContext() {
		return httpClContext;
	}

	public HttpReqParam<T> setHttpClContext(HttpClientContext httpClContext) {
		this.httpClContext = httpClContext;
		return this;
	}

	public NameValuePair[] getReqParamsArr() {
		return reqParams == null ? new NameValuePair[0] : reqParams.toArray(new NameValuePair[reqParams.size()]);
	}

	public List<NameValuePair> getReqParams() {
		return reqParams;
	}
	public HttpReqParam<T> setReqParams(List<NameValuePair> reqParams) {
		this.reqParams = reqParams;
		return this;
	}
	
	public List<NameValuePair> getPostParams() {
		return postParams;
	}
	
	public HttpReqParam<T> setPostParams(List<NameValuePair> postParams) {
		this.postParams = postParams;
		return this;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	public String getUrl() {
		return url;
	}
	public HttpReqParam<T> setUrl(String url) {
		this.url = url;
		return this;
	}
	public Object getBody() {
		return body;
	}
	public HttpReqParam<T> setBody(Object body) {
		this.body = body;
		return this;
	}
	public List<Header> getHeaders() {
		return headers;
	}

	public Header[] getHeadersArr() {
		return headers == null ? new Header[0] : headers.toArray(new Header[headers.size()]);
	}

	public HttpReqParam<T> setHeaders(List<Header> headers) {
		this.headers = headers;
		return this;
	}
	
	public ContentType getContentType() {
		return contentType;
	}

	public HttpReqParam<T> setContentType(ContentType contentType) {
		this.contentType = contentType;
		return this;
	}
	
	@Override
	public String toString() {
		return "PostSingleParam [url=" + url + ", body=" + body + ", headers=" + headers + "]";
	}

	public HttpReqParam<T> clone(){
		return new HttpReqParam<T>(clazz)
			.setBody(body)
			.setHeaders(headers)
			.setReqParams(reqParams)
			.setUrl(url);
	}

}
