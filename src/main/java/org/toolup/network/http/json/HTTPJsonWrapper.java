package org.toolup.network.http.json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.http.HTTPWrapper;
import org.toolup.network.http.HTTPWrapperException;
import org.toolup.network.http.HTTPWrapperException.HTTPVERB;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public class HTTPJsonWrapper {

	/*
	 * 20230905 : Deserialization of @JsonTypeInfo annotated type fails with missing type id even for explicit concrete subtypes 
	 * 
	 * https://github.com/FasterXML/jackson-databind/issues/2968
	 * https://github.com/FasterXML/jackson-databind/issues/3853
	 * => cowtowncoder commented on Apr 18
	 * Correct: while the fix is in, it will only work when that MapperFeature ^^^ is explicitly DISABLED (is enabled by default).
	 * 
	 */
	public static final  ObjectMapper objectMapper = createOM();

	protected final Logger logger = LoggerFactory.getLogger(HTTPJsonWrapper.class);
	protected final HTTPWrapper httpWrapper = new HTTPWrapper();

	protected final List<Header> defaultHeaders;

	private String limitParamName = "limit";
	private String offsetParamName = "offset";

	public static final int DEFAULT_LIMIT_MAX_VALUE = 50;
	private int limitMaxVal = DEFAULT_LIMIT_MAX_VALUE;
	
	public HTTPJsonWrapper() {
		defaultHeaders = new ArrayList<Header>();
	}
	
	/**
     * Bug Spring / Jackson + Jersey : 
     * - https://github.com/eclipse-ee4j/jersey/issues/4130 
     *   "Using SecurityEntityFilteringFeature with Jackson Databind results in "Cannot resolve PropertyFilter with id..." exceptions"
     * - https://github.com/FasterXML/jackson-databind/issues/2293 
     * "SerializerProvider has no FilterProvider but retrieves a BeanSerializer that has a non null _propertyFilterId"
     * => using this workaround : https://stackoverflow.com/a/27512905
     */
	public static Builder createOMBldr() {
		return JsonMapper
			    .builder()
			    .filterProvider(new SimpleFilterProvider().setFailOnUnknownId(false))
			    .disable(MapperFeature.REQUIRE_TYPE_ID_FOR_SUBTYPES)
			    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public static ObjectMapper createOM() {
		return createOMBldr().build();
	}
	
	public HTTPJsonWrapper limitMaxVal(int limitMaxVal) {
		this.limitMaxVal = limitMaxVal;
		this.httpWrapper.limitMaxVal(limitMaxVal);
		return this;
	}

	public HTTPJsonWrapper limitParamName(String limitParamName) {
		this.limitParamName = limitParamName;
		return this;
	}

	public HTTPJsonWrapper offsetParamName(String offsetParamName) {
		this.offsetParamName = offsetParamName;
		return this;
	}
	
	public String getLimitParamName() {
		return limitParamName;
	}


	public String getOffsetParamName() {
		return offsetParamName;
	}


	public List<Header> getDefaultHeaders() {
		return defaultHeaders;
	}

	public HTTPJsonWrapper addDefaultHeader(Header... defaultHeaders) {
		if(defaultHeaders != null) this.defaultHeaders.addAll(Arrays.asList(defaultHeaders));
		return this;
	}

	public HTTPJsonWrapper defaultHeaders(Header... defaultHeaders) {
		this.defaultHeaders.clear();
		if(defaultHeaders != null) this.defaultHeaders.addAll(Arrays.asList(defaultHeaders));
		return this;
	}

	public HTTPJsonWrapper defaultHeaders(List<Header> defaultHeaders) {
		this.defaultHeaders.clear();
		if(defaultHeaders != null) this.defaultHeaders.addAll(defaultHeaders);
		return this;
	}

	public String httpGetListParams(int limit, int offset, NameValuePair[] params) throws HTTPWrapperException {
		return httpWrapper.httpGetListParams(limit, offset
				, getLimitParamName()
				, getOffsetParamName()
				, params);
	}
	
	// GET

	public <T> T readSingle(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		if(param == null) return null;
		String url = param.getReqParams() == null ? param.getUrl() : HTTPWrapper.fullUrl(param.getUrl(), HTTPWrapper.queryParams(param.getReqParamsArr()));
		try {
			if(logger.isDebugEnabled())
				logger.debug("readSingle {}   -> ...", url);
			Object obj = httpGETParsedJsonDocument(url, httpClient);
			if(logger.isDebugEnabled())
				logger.debug("readSingle {}   -> {}", url, objectMapper.writeValueAsString(obj));
			if(obj == null) return null;
			return objectMapper.readValue(objectMapper.writeValueAsString(obj), param.getClazz());
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, ex);
		}
	}

	
	public <T> HTTPJsonListResponse<T> readListResp(CloseableHttpClient httpClient, HttpReqParam<T> param, String jsonPath) throws HTTPWrapperException{
		if(param == null) return null;
		String url = param.getUrl();
		if(logger.isDebugEnabled()) logger.debug("readList {}...", url);
		try (CloseableHttpResponse resp = httpWrapper.httpget(url, httpClient, defaultHeaders)){
			Object obj = Configuration.defaultConfiguration().jsonProvider().parse(httpWrapper.getContentAsString(resp));
			HTTPJsonListResponse<T> result = new HTTPJsonListResponse<T>()
					.setHeaders(Arrays.asList(resp.getAllHeaders()));
			if(obj == null) return result;
			List<Object> res = JsonPath.read(obj, jsonPath);
			for (Object o : res) {
				if(logger.isDebugEnabled()) logger.debug("  -> {}", objectMapper.writeValueAsString(o));
				result.add(objectMapper.readValue(objectMapper.writeValueAsString(o), param.getClazz()));
			}
			return result;
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, ex);
		}
	}

	
	public <T> List<T> readList(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException{
		return readList(httpClient, param, "$.items[*]");
	}
	
	public <T> List<T> readList(CloseableHttpClient httpClient, HttpReqParam<T> param, String jsonPath) throws HTTPWrapperException{
		HTTPJsonListResponse<T> r = readListResp(httpClient, param, jsonPath);
		return r == null ? null : r.getList();
	}
	
	public <T> List<T> readAll(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException{
		return readAll(httpClient, param, null);
	}
	
	public <T> List<T> readAll(CloseableHttpClient httpClient, HttpReqParam<T> param, String jsonPath) throws HTTPWrapperException{
		if(param == null) return null;
		String urlBase = param.getUrl();
		try {
			List<T> result = new ArrayList<>();
			if(urlBase.contains("?")) 
				throw new HTTPWrapperException(HTTPVERB.GET, urlBase, null, "baseUrl param must not contain '?' : please pass any GET param via the 'param' method parameter.");
			int limit = limitMaxVal;
			for(int i= 0 ; ; ++i) {
				int offset = limit * i;
				
				HttpReqParam<T> newP = param.clone()
						.setUrl(HTTPWrapper.fullUrl(urlBase, httpWrapper.httpGetListParams(limit, offset, getLimitParamName(), offsetParamName, param.getReqParamsArr())));
				List<T> subResLst = jsonPath == null ? readList(httpClient, newP) : readList(httpClient, newP, jsonPath);
				if(subResLst.isEmpty()) break;
				result.addAll(subResLst);
			}
			return result;
		} catch (HTTPWrapperException ex) {
			throw new HTTPWrapperException(HTTPVERB.GET, ex.getxError(), ex.getHttpStatus(), urlBase, ex.getResponseContent(), ex, ex.getMessage(), ex.getHeaderList().toArray(new Header[ex.getHeaderList().size()]));
		}
	}
	
	
	
	public byte[] httpGetRaw(CloseableHttpClient httpClient, HttpReqParam<?> param) throws HTTPWrapperException {
		return httpWrapper.httpGetRaw(param.getUrl(), httpClient, defaultHeaders, null);
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

	// POST
	public <T> List<T> postList(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException{
		return postList(httpClient, param, "$");
	}

	public <T> List<T> postList(CloseableHttpClient httpClient, HttpReqParam<T> param, String listJsonPath) throws HTTPWrapperException{
		String url = param.getUrl();
		try {
			if(logger.isDebugEnabled())
				logger.debug("postList {}   -> req-body : {}", param.getUrl(), objectMapper.writeValueAsString(param.getBody()));
			String resp = httpPOST(httpClient, param);
			if(logger.isDebugEnabled())
				logger.debug("postList {} -> resp : {}", url, resp);
			List<T> result = new ArrayList<>();
			if(resp == null) return result;

			List<Object> res = JsonPath.read(resp, listJsonPath);
			for (Object o : res) {
				if(logger.isTraceEnabled())
					logger.trace("  -> {} adding result {}", url, objectMapper.writeValueAsString(o));

				result.add(objectMapper.readValue(objectMapper.writeValueAsString(o), param.getClazz()));
			}
			return result;
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.POST, url, ex);
		}
	}

	public <T> T postSingle(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		String url = param.getUrl();
		String objectValue = null;
		try {
			
			InputStream body = param.getBody() == null || param.getBody() instanceof InputStream ? 
					(InputStream)param.getBody() : IOUtils.toInputStream(objectMapper.writeValueAsString(param.getBody()), "utf-8");
			if(logger.isDebugEnabled()) {
				logger.debug("postSingle {}   -> req-body : {}", param.getUrl(), body == null ? null : IOUtils.toString(body, Charset.forName("utf-8")));
				
			}
			objectValue = httpPOST(httpClient, param);
			if(logger.isDebugEnabled()) {
				logger.debug("postSingle {}   -> resp : {}", url, objectValue);
			}
			if(objectValue == null || objectValue.isEmpty() || param.getClazz() == String.class) return (T)objectValue;
			return objectMapper.readValue(objectValue, param.getClazz());
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.POST, url , ex,  String.format("postSingle : val = %s", objectValue));
		}
	}

	public String httpPOST(CloseableHttpClient httpClient, HttpReqParam<?> param) throws HTTPWrapperException {
		if(param == null) return null;
		Object body = param.getBody();
		try {
			return httpWrapper.httpPOSTParsedJson(param.getUrl()
					, body == null ? null : IOUtils.toInputStream(writeValueAsString(body), "utf-8")
							, httpClient
							, getHeaders(param.getHeadersArr())
							, param.getReqParams()
							, param.getHttpClContext()
							, param.getContentType());
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
		} catch (IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.POST, param.getUrl(), ex);
		}
		return null;
	}
	
	public CloseableHttpResponse httpPOSTRaw(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return httpWrapper.httpPOST(url, bodyIS, httpClient, getHeaders(headers.toArray(new Header[headers.size()])), parameters, null);
	}

	//PATCH

	public <T> T putSingle(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {

		try {
			InputStream body = param.getBody() == null || param.getBody() instanceof InputStream ? 
					(InputStream)param.getBody() : IOUtils.toInputStream(objectMapper.writeValueAsString(param.getBody()), "utf-8");
			if(logger.isDebugEnabled()) {
				logger.debug("putSingle {}   -> req-body : {}", param.getUrl(), IOUtils.toString(body, Charset.forName("utf-8")));
			}
			String obj = httpPUT(httpClient, param);
			if(logger.isDebugEnabled())
				logger.debug("putSingle {}   -> resp : {}", param.getUrl(), objectMapper.writeValueAsString(obj));
			if(obj == null || obj.isEmpty()) return null;
			return objectMapper.readValue(obj, param.getClazz());
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.PUT, param.getUrl(), ex);
		}
	}
	
	public CloseableHttpResponse httpPUTRaw(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return httpWrapper.httpPUT(url, bodyIS, httpClient, getHeaders(headers.toArray(new Header[headers.size()])), parameters, null);
	}
	

	public <T> String httpPUT(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		if(param == null) return null;
		String url = param.getUrl();
		try {
			InputStream body = param.getBody() == null || param.getBody() instanceof InputStream ? 
					(InputStream)param.getBody() : IOUtils.toInputStream(objectMapper.writeValueAsString(param.getBody()), "utf-8");
			
			if(logger.isDebugEnabled()) {
				logger.debug("httpPUT {}   -> req-body : {}", param.getUrl(), IOUtils.toString(body, Charset.forName("utf-8")));
			}
			String result = httpWrapper.httpPUTContent(url, body , httpClient, getHeaders(param.getHeadersArr()), null);
			if(logger.isDebugEnabled())
				logger.debug("httpPUT {}   -> resp : {}", param.getUrl(), result);
			return result;
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
		} catch (IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.PATCH, url, ex);
		}
		return null;
	}

	//PATCH

	public <T> T patchSingle(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		try {
			if(logger.isDebugEnabled())
				logger.debug("patchSingle {}   -> req-body : {}", param.getUrl(), objectMapper.writeValueAsString(param.getBody()));
			String obj = httpPATCH(httpClient, param);
			if(logger.isDebugEnabled())
				logger.debug("patchSingle {}   -> resp : {}", param.getUrl(), objectMapper.writeValueAsString(obj));
			if(obj == null || obj.isEmpty()) return null;
			return objectMapper.readValue(obj, param.getClazz());
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.PATCH, param.getUrl(), ex);
		}
	}


	public <T> String httpPATCH(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		if(param == null) return null;
		Object body = param.getBody();
		String url = param.getUrl();
		try {
			return httpWrapper.httpPatchContent(url, body == null ? null : IOUtils.toInputStream(objectMapper.writeValueAsString(body), "utf-8") , httpClient, getHeaders(param.getHeadersArr()), null);
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
			return null;
		} catch (IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.PATCH, url, ex);
		}
	}

	//DELETE
	
	public <T> T deleteSingle(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		try {
			if(logger.isDebugEnabled())
				logger.debug("deleteSingle {}   -> req-body : {}", param.getUrl(), objectMapper.writeValueAsString(param.getBody()));
			String obj = httpWrapper.httpDeleteContent(param.getUrl(), httpClient, getHeaders(param.getHeadersArr()));
			if(logger.isDebugEnabled())
				logger.debug("deleteSingle {}   -> resp : {}", param.getUrl(), objectMapper.writeValueAsString(obj));
			if(obj == null) return null;
			return objectMapper.readValue(obj, param.getClazz());
		}catch(IOException ex) {
			throw new HTTPWrapperException(HTTPVERB.DELETE, param.getUrl(), ex);
		}
	}

	public <T> CloseableHttpResponse httpDELETE(CloseableHttpClient httpClient, HttpReqParam<T> param) throws HTTPWrapperException {
		return httpDELETE(param.getUrl(), httpClient, param.getHeadersArr());
	}

	public CloseableHttpResponse httpDELETE(String url, CloseableHttpClient httpClient, Header...headers) throws HTTPWrapperException {
		try {
			if(logger.isDebugEnabled())
				logger.debug("httpDELETE {}   -> ...", url);
			CloseableHttpResponse r = httpWrapper.httpDelete(url, httpClient, getHeaders(headers));
			if(logger.isDebugEnabled())
				logger.debug("httpDELETE {}   -> {}", url, r.getStatusLine());
			return r;
		} catch(HTTPWrapperException e) {
			handleSecurityException(e);
		}
		return null;
	}

	// MISC

	private List<Header> getHeaders(Header... headers){
		List<Header> result = new ArrayList<Header>();
		result.addAll(defaultHeaders);
		if(headers != null)
			result.addAll(Arrays.asList(headers));
		return result;
	}

	public void handleSecurityException(HTTPWrapperException e) throws HTTPWrapperException {
		if(e.getStatusCode() == 401) {
			throw new HTTPWrapperException(e.getVerb(), null, e.getStatusCode(), e.getUrl(), null, e, "Unauthorized : make sure your API credentials are valid.");
		}else if(e.getStatusCode() == 403) {
			throw new HTTPWrapperException(e.getVerb(), null, e.getStatusCode(), e.getUrl(), null, e, "Forbidden : make sure your user has admin priviledges..");
		}else {
			throw e;
		}
	}

	private String writeValueAsString(Object body) throws JsonGenerationException, JsonMappingException, IOException {
		return body instanceof String ? (String)body : objectMapper.writeValueAsString(body);
	}

	public static String fullUrl(String baseUrl, String params) {
		if(params == null || params.isEmpty()) return baseUrl;
		return String.format("%s?%s", baseUrl, params);
	}


}
