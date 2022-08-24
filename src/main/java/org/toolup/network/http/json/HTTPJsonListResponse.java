package org.toolup.network.http.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPJsonListResponse<T> {

	private final Logger logger = LoggerFactory.getLogger(HTTPJsonListResponse.class);
	
	private final List<T> list;
	private final List<Header> headers;

	public HTTPJsonListResponse() {
		headers = new ArrayList<>();
		list = new ArrayList<>();
	}

	public void add(T v) {
		list.add(v);
	}

	public List<T> getList() {
		return list;
	}

	public HTTPJsonListResponse<T> setList(List<T> list) {
		this.list.clear();
		if(list != null) this.list.addAll(list);
		return this;
	}

	public HTTPJsonListResponse<T> setHeaders(List<Header> headers) {
		this.headers.clear();
		if(headers != null) this.headers.addAll(headers);
		return this;
	}
	
	public String getHeaderValue(String name) {
		Optional<Header> r = this.headers
			.stream()
			.filter(h -> name.equals(h.getName()))
			.findFirst();
		return r.isPresent() ? r.get().getValue() : null;
	}
	
	public int getHeaderValueInteger(String name) {
		String result = getHeaderValue(name);
		if(result == null) {
			logger.warn("HTTP Header <{}> : header not set", name);
			return -1;
		}
		try {
			return Integer.parseInt(result);
		}catch(NumberFormatException ex) {
			logger.warn("HTTP Header <{}> : invalid integer : '{}'", name, result);
			return -1;
		}
	}

	public List<Header> getHeaders() {
		return headers;
	}

	@Override
	public String toString() {
		return "HTTPJsonListResponse [list=" + list + ", headers=" + headers + "]";
	}
	

}
