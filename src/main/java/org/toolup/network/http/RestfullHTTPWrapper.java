package org.toolup.network.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.http.HTTPWrapper;
import org.toolup.network.http.HTTPWrapperException;
import org.toolup.network.http.HTTPWrapperException.HTTPVERB;

import com.jayway.jsonpath.JsonPath;

public class RestfullHTTPWrapper {
	
	private static final  ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private final Logger logger = LoggerFactory.getLogger(RestfullHTTPWrapper.class);
	private final HTTPWrapper httpWrapper = new HTTPWrapper();
	
	private final List<Header> defaultHeaders;
	
	public RestfullHTTPWrapper() {
		defaultHeaders = new ArrayList<Header>();
	}
	
	public RestfullHTTPWrapper defaultHeaders(Header... defaultHeaders) {
		this.defaultHeaders.clear();
		if(defaultHeaders != null) this.defaultHeaders.addAll(Arrays.asList(defaultHeaders));
		return this;
	}
	
	public RestfullHTTPWrapper defaultHeaders(List<Header> defaultHeaders) {
		this.defaultHeaders.clear();
		if(defaultHeaders != null) this.defaultHeaders.addAll(defaultHeaders);
		return this;
	}
	
	public <T> T postSingle(CloseableHttpClient httpClient, String url, Object body, Class<T> clazz, Header...headers) throws HTTPWrapperException {
		try {
			Object obj = httpPOST(url, httpClient, objectMapper.writeValueAsString(body), headers);
			if(logger.isDebugEnabled())
				logger.debug("readSingle {} %n  -> {}", url, objectMapper.writeValueAsString(obj));
			if(obj == null) return null;
			return objectMapper.readValue(objectMapper.writeValueAsString(obj), clazz);
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.POST, url, ex);
		}
	}
	
	public <T> T readSingle(CloseableHttpClient httpClient, String url, Class<T> clazz) throws HTTPWrapperException {
		try {
			Object obj = httpGETParsedJsonDocument(url, httpClient);
			if(logger.isDebugEnabled())
				logger.debug("readSingle {} %n  -> {}", url, objectMapper.writeValueAsString(obj));
			if(obj == null) return null;
			return objectMapper.readValue(objectMapper.writeValueAsString(obj), clazz);
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, ex);
		}
	}
	
	public <T> List<T> readList(CloseableHttpClient httpClient, String url, Class<T> clazz) throws HTTPWrapperException{
		try {
			Object obj = httpGETParsedJsonDocument(url, httpClient);
			List<T> result = new ArrayList<>();
			if(obj == null) return result;
			List<Object> res = JsonPath.read(obj, "$.items[*]");
			if(logger.isDebugEnabled()) logger.debug("readList {} %n", url);
			for (Object o : res) {
				if(logger.isDebugEnabled()) logger.debug("-> {}", url, objectMapper.writeValueAsString(o));
				result.add(objectMapper.readValue(objectMapper.writeValueAsString(o), clazz));
			}
			return result;
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, ex);
		}
	}
	
	public <T> T patchSingle(CloseableHttpClient httpClient, String url, String body, Class<T> clazz) throws HTTPWrapperException {
		try {
			Object obj = httpPATCH(url, httpClient, body);
			if(logger.isDebugEnabled())
				logger.debug("patchSingle {} %n  -> {}", url, objectMapper.writeValueAsString(obj));
			if(obj == null) return null;
			return objectMapper.readValue(objectMapper.writeValueAsString(obj), clazz);
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.PATCH, url, ex);
		}
	}
	
	public CloseableHttpResponse httpDELETE(String url, CloseableHttpClient httpClient, Header...headers) throws HTTPWrapperException {
		try {
			
			return httpWrapper.httpDelete(url, httpClient, getHeaders(headers));
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
		}
		return null;
	}
	
	private List<Header> getHeaders(Header...headers){
		List<Header> result = new ArrayList<Header>();
		result.addAll(defaultHeaders);
		if(headers != null)
			result.addAll(Arrays.asList(headers));
		return result;
	}

	public Object httpPOST(String url, CloseableHttpClient httpClient, String body, Header... headers) throws HTTPWrapperException {
		try {
			return httpWrapper.httpPOSTParsedJson(url, body == null ? null : IOUtils.toInputStream(body, "utf-8"), httpClient, getHeaders(headers), null, null);
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
		} catch (IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.POST, url, ex);
		}
		return null;
	}
	
	public Object httpPATCH(String url, CloseableHttpClient httpClient, String body, Header...headers) throws HTTPWrapperException {
		try {
			return httpWrapper.httpPATCHParsedJson(url, body == null ? null : IOUtils.toInputStream(body, "utf-8") ,httpClient, getHeaders(headers), null);
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
		} catch (IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.PATCH, url, ex);
		}
		return null;
	}

	public Object httpGETParsedJsonDocument(String url, CloseableHttpClient httpClient) throws HTTPWrapperException {
		try {
			return httpWrapper.httpGETParsedJson(url, httpClient, defaultHeaders);
		} catch(HTTPWrapperException e) {
			if(e.getStatusCode() == 404) return null;
			handleSecurityException(e);
		}
		return null;
	}

	public void handleSecurityException(HTTPWrapperException e) throws HTTPWrapperException {
		if(e.getStatusCode() == 401) {
			throw new HTTPWrapperException(e.getVerb(), e.getUrl(), e, "Unauthorized : make sure your API credentials are valid.");
		}else if(e.getStatusCode() == 403) {
			throw new HTTPWrapperException(e.getVerb(), e.getUrl(), e, "Forbidden : make sure your user has admin priviledges..");
		}else {
			throw e;
		}
	}
}
