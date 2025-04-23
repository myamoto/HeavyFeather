package org.toolup.network.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;

public class HttpResp {

	private final List<Header> headers = new ArrayList<>();
	private String content;

	public String getContent() {
		return content;
	}

	public HttpResp setContent(String content) {
		this.content = content;
		return this;
	}

	public List<Header> getHeaders() {
		return headers;
	}
	
	public HttpResp setHeaders(List<Header> headers) {
		this.headers.clear();
		if(headers != null) this.headers.addAll(headers);
		return this;
	}
	
	public Header getHeader(String name) {
		Optional<Header> r = headers.stream().filter(h -> name.equals(h.getName())).findFirst();
		return r.isPresent() ? r.get() : null;
	}
}
