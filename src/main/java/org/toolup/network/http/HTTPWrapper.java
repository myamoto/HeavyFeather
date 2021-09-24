package org.toolup.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.IOUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.app.IConfigurable;
import org.toolup.app.ParamLoader;
import org.toolup.io.properties.PropertiesUtilsException;
import org.toolup.network.http.HTTPWrapperException.HTTPVERB;
import org.toolup.network.http.json.HTTPJsonWrapper;

import com.jayway.jsonpath.Configuration;

public class HTTPWrapper implements IConfigurable{

	private final static HTTPWrapper defaultInstance = new HTTPWrapper();

	public static HTTPWrapper getDefaultInstance() {
		return defaultInstance;
	}

	private final Logger logger = LoggerFactory.getLogger(HTTPWrapper.class);

	private boolean useProxy = false;

	private static final List<Integer> DELETE_STATUSCODE_OK = Arrays.asList(200, 202, 204);
	private static final List<Integer> UPDATE_STATUSCODE_OK = Arrays.asList(200, 201, 204);
	private final static List<String> UPDATE_METHODS = Arrays.asList(HTTPVERB.PATCH, HTTPVERB.PUT, HTTPVERB.POST)
			.stream()
			.map(HTTPVERB::toString)
			.collect(Collectors.toList());

	private static final List<Integer> GET_STATUSCODE_OK = Arrays.asList(200, 206);

	public HTTPWrapper useProxy(boolean useProxy) {
		this.useProxy = useProxy;
		return this;
	}

	// POST

	public CloseableHttpResponse httpPOST(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		return httpPOST(url, null, httpClient, headers, null);
	}

	public CloseableHttpResponse httpPOST(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return httpUpdt(new HttpReqWrapper()
				.setHttpReq(new HttpPost(url))
				.setUrl(url)
				.setBodyIS(bodyIS)
				.setHeaders(headers)
				.setParameters(parameters), httpClient);
	}

	public CloseableHttpResponse httpPOST(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters, HttpClientContext context) throws HTTPWrapperException {
		return httpUpdt(
				new HttpReqWrapper()
				.setHttpReq(new HttpPost(url))
				.setUrl(url)
				.setBodyIS(bodyIS)
				.setHeaders(headers)
				.setParameters(parameters)
				.setContext(context), httpClient);
	}

	public String httpPOSTParsedJson(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters, HttpClientContext context
			, ContentType contentType ) throws HTTPWrapperException {
		HttpReqWrapper req = new HttpReqWrapper()
				.setUrl(url)
				.setHttpReq(new HttpPost(url))
				.setBodyIS(bodyIS)
				.setHeaders(headers)
				.setParameters(parameters)
				.setContext(context)
				.setContentType(contentType);

		try(CloseableHttpResponse resp = httpUpdt(req, httpClient)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.POST, req.getUrl(), e);
		}
	}


	//PUT

	public CloseableHttpResponse httpPUT(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		return httpPUT(url, null, httpClient, headers, null);
	}

	public CloseableHttpResponse httpPUT(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return httpUpdt(new HttpReqWrapper()
				.setHttpReq(new HttpPut(url))
				.setUrl(url)
				.setBodyIS(bodyIS)
				.setHeaders(headers)
				.setParameters(parameters), httpClient);
	}

	public String httpPUTContent(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		try(CloseableHttpResponse resp = httpPUT(url, bodyIS, httpClient, headers, parameters)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.PUT, url, e);
		}
	}

	//PATCH 

	public Object httpPATCHParsedJson(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return Configuration.defaultConfiguration().jsonProvider().parse(httpPatchContent(url, bodyIS, httpClient, headers, parameters));
	}

	public CloseableHttpResponse httpPATCH(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return httpUpdt(
				new HttpReqWrapper()
				.setHttpReq(new HttpPatch(url))
				.setUrl(url)
				.setBodyIS(bodyIS)
				.setHeaders(headers)
				.setParameters(parameters), httpClient);
	}

	public String httpPatchContent(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		try(CloseableHttpResponse resp = httpPATCH(url, bodyIS, httpClient, headers, parameters)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.PATCH, url, e);
		}
	}

	private CloseableHttpResponse httpUpdt(HttpReqWrapper req, CloseableHttpClient httpClient) throws HTTPWrapperException {

		if(req == null) throw new HTTPWrapperException(null, null, null, "req can't be null.");


		if(req.getHttpReq() == null || req.getHttpReq().getMethod() == null || !UPDATE_METHODS.contains(req.getHttpReq().getMethod().toUpperCase()))
			throw new HTTPWrapperException(null, req.getUrl(), null, 
					String.format("invalid HTTP method <%s>. should be one of : %s"
							, (req.getHttpReq() == null ? null : req.getHttpReq().getMethod().toUpperCase())
							, UPDATE_METHODS));

		HttpEntityEnclosingRequestBase httpReq = req.getHttpReq();
		String url = req.getUrl();
		InputStream bodyIS = req.getBodyIS();
		List<? extends Header> headers = req.getHeaders();
		List<NameValuePair> parameters = req.getParameters();
		HttpClientContext context = req.getContext();
		ContentType contentType = req.getContentType() != null ? req.getContentType() : ContentType.APPLICATION_JSON;
		handleProxy(httpReq);
		if(bodyIS != null) {
			try {
				String bodyContent = IOUtils.toString(bodyIS, StandardCharsets.UTF_8);
				StringEntity body = new StringEntity(bodyContent, contentType);
				httpReq.setEntity(body);
			} catch (IOException e) {
				throw new HTTPWrapperException(HTTPVERB.from(httpReq.getMethod()), url, e);
			}
		}

		if(headers != null) {
			for (Header header : headers) {
				httpReq.addHeader(header);
			}
		}


		if(parameters != null && !parameters.isEmpty()) {
			try {
				httpReq.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new HTTPWrapperException(HTTPVERB.from(httpReq.getMethod()), url, e);
			}
		}
		try {
			CloseableHttpResponse result;

			if(context != null) result = httpClient.execute(httpReq, context);
			else result = httpClient.execute(httpReq);

			int status = result.getStatusLine().getStatusCode();
			if(!UPDATE_STATUSCODE_OK.contains(status)) {
				String content;
				try {
					content = parseJson(result);
				}catch(IOException e) {
					content = null;
				}

				String xError = result.containsHeader("X-Error") ? result.getFirstHeader("X-Error").getValue() : null;
				if(logger.isDebugEnabled())
					logger.debug("{} Exception 'invalid HTTP Status' : {} -> {} {} {}"
							, httpReq.getMethod()
							, url
							, status
							, content
							, xError == null ? "" : ", xError : " + xError);

				throw new HTTPWrapperException(
						HTTPVERB.from(httpReq.getMethod())
						, xError
						, status
						, url
						, content
						, result.getAllHeaders());
			}
			if(logger.isDebugEnabled())
				logger.debug("{} OK on {} -> {}", httpReq.getMethod(), url, status);
			return result;
		} catch (IOException e) {
			if(logger.isDebugEnabled())
				logger.debug("{} Exception on {} -> {}"
						, httpReq.getMethod()
						, httpReq.getClass().getSimpleName()
						, url
						, e.getMessage());
			throw new HTTPWrapperException(HTTPVERB.from(httpReq.getMethod()), url, e);
		}
	}

	// DELETE

	public String httpDeleteContent(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		try(CloseableHttpResponse resp = httpDelete(url, httpClient, headers)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.DELETE, url, e);
		}
	}

	public Object httpDeleteParsedJson(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		String resp = httpDeleteContent(url, httpClient, headers);
		return Configuration.defaultConfiguration().jsonProvider().parse(resp);
	}

	public CloseableHttpResponse httpDelete(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		HttpDelete httpDelete = new HttpDelete(url);
		handleProxy(httpDelete);
		if(headers != null) {
			for (Header header : headers) {
				httpDelete.addHeader(header);
			}
		}
		try(CloseableHttpResponse result = httpClient.execute(httpDelete)){
			if(!DELETE_STATUSCODE_OK.contains(result.getStatusLine().getStatusCode())) {
				throw new HTTPWrapperException(HTTPVERB.DELETE
						, result.containsHeader("X-Error") ? result.getFirstHeader("X-Error").getValue() : null
								, result.getStatusLine().getStatusCode()
								, url
								, IOUtils.toString(result.getEntity().getContent(), StandardCharsets.UTF_8));
			}
			if(logger.isDebugEnabled())
				logger.debug("{} OK on {} -> {}", httpDelete.getMethod(), url, result.getStatusLine().getStatusCode());
			return result;
		} catch (IOException e) {
			if(logger.isDebugEnabled())
				logger.debug("{} Exception on {} -> {}"
						, httpDelete.getMethod()
						, httpDelete.getClass().getSimpleName()
						, url
						, e.getMessage());
			throw new HTTPWrapperException(HTTPVERB.DELETE, url, e);
		}
	}

	//get

	public String httpGetContent(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		return httpGetContent(url, httpClient, headers, null);
	}

	public String httpGetContent(String url, CloseableHttpClient httpClient, List<? extends Header> headers, HttpClientContext context) throws HTTPWrapperException {
		try(CloseableHttpResponse resp = httpget(url, httpClient, headers, context)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, e);
		}
	}

	public Object httpGETParsedJson(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		String resp = httpGetContent(url, httpClient, headers);
		return Configuration.defaultConfiguration().jsonProvider().parse(resp);
	}


	CloseableHttpResponse httpget(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		return httpget(url, httpClient, headers, null);
	}

	CloseableHttpResponse httpget(String url, CloseableHttpClient httpClient, List<? extends Header> headers, HttpClientContext context) throws HTTPWrapperException {
		HttpGet httpGet = new HttpGet(url);

		handleProxy(httpGet);

		if(headers != null) {
			for (Header header : headers) {
				httpGet.addHeader(header);
			}
		}
		CloseableHttpResponse result;
		try {
			if(context != null) result = httpClient.execute(httpGet, context);
			else result = httpClient.execute(httpGet);
		} catch (IOException e) {
			if(logger.isDebugEnabled())
				logger.debug("{} Exception on {} -> {}", httpGet.getMethod(), url, e.getMessage());
			throw new HTTPWrapperException(HTTPVERB.GET, url, e);
		}
		int status = result.getStatusLine().getStatusCode();
		if(!GET_STATUSCODE_OK.contains(status)) {
			String content;
			try {
				content = parseJson(result);
			}catch(IOException e) {
				content = null;
			}
			String xError = result.containsHeader("X-Error") ? result.getFirstHeader("X-Error").getValue() : null;
			if(logger.isDebugEnabled())
				logger.debug("httpget Exception 'invalid HTTP Status' : {} -> {} {} {}"
						, url
						, status
						, content
						, xError == null ? "" : ", xError : " + xError);

			throw new HTTPWrapperException(HTTPVERB.GET, xError, status, url, content);
		}
		if(logger.isDebugEnabled())
			logger.debug("{} OK on {} -> {}", httpGet.getMethod(), url, status);
		return result;
	}

	//misc

	private String parseJson(CloseableHttpResponse resp) throws IOException {
		return IOUtils.toString(resp.getEntity().getContent(), StandardCharsets.UTF_8);
	}

	private void handleProxy(HttpRequestBase httpRequest) {
		if(useProxy) {
			String proxyHost = getProxyHost();
			String proxyPort = getProxyPort();
			if(proxyHost != null) {
				httpRequest.setConfig(RequestConfig.custom()
						.setConnectionRequestTimeout(500)
						.setProxy(new HttpHost(proxyHost, Integer.valueOf(proxyPort), "http")).build());
			}
		}
	}
	
	public static String httpGetListParams(int limit, int offset, String limitParamName, String offsetParamName, NameValuePair... params) throws HTTPWrapperException {
		List<NameValuePair> paramLst = new ArrayList<>();
		if(params != null) 
			paramLst.addAll(Arrays.asList(params));

		if(limit > HTTPJsonWrapper.LIMIT_MAX_VALUE) 
			throw new HTTPWrapperException(HTTPVERB.GET, null, null, String.format( "limit cannot excess %d.", HTTPJsonWrapper.LIMIT_MAX_VALUE));

		paramLst.add(new BasicNameValuePair(limitParamName, Integer.toString(limit)));
		paramLst.add(new BasicNameValuePair(offsetParamName, Integer.toString(offset)));

		return queryParams(paramLst.toArray(new NameValuePair[paramLst.size()]));
	}

	public static String queryParams(NameValuePair... params) throws HTTPWrapperException {
		List<NameValuePair> paramLst = new ArrayList<>();
		if(params != null) paramLst.addAll(Arrays.asList(params));

		StringBuilder res = new StringBuilder();
		if(params == null || params.length == 0) return res.toString();
		for (int i = 0; i < params.length; ++i) {
			NameValuePair param = params[i];
			try {
				res.append(String.format("%s=%s", param.getName(), URLEncoder.encode(param.getValue(), "utf-8")));
			} catch (UnsupportedEncodingException e) {
				throw new HTTPWrapperException(HTTPVERB.GET, null, e);
			}
			if(i < params.length - 1) res.append("&");
		}

		return res.toString();
	}

	public static String fullUrl(String baseUrl, String params) {
		if(params == null || params.isEmpty()) return baseUrl;
		return String.format("%s?%s", baseUrl, params);
	}

	public static String slashTerminatedUrl(String url) {
		return url == null || url.endsWith("/") ? url : url + "/";
	}

	public static final String getProxyHost() {
		return ParamLoader.load(HttpWrapperConf.PROP_PROXY_HOST);
	}
	public static final String getProxyPort() {
		return ParamLoader.load(HttpWrapperConf.PROP_PROXY_PORT);
	}

	public static final String getProxyUser() {
		return ParamLoader.load(HttpWrapperConf.PROP_PROXY_USER);
	}

	public static final String getProxyPassword() {
		return ParamLoader.load(HttpWrapperConf.PROP_PROXY_PASSWORD);
	}

	public HTTPWrapper configure(Properties props) throws PropertiesUtilsException {
		return configure(HttpWrapperConf.from(props));
	}

	public HTTPWrapper configure(HttpWrapperConf conf) {
		ParamLoader.setParam(HttpWrapperConf.PROP_PROXY_HOST, conf.getProxyHost());
		ParamLoader.setParam(HttpWrapperConf.PROP_PROXY_PORT, conf.getProxyPort());
		ParamLoader.setParam(HttpWrapperConf.PROP_PROXY_USER, conf.getProxyUser());
		ParamLoader.setParam(HttpWrapperConf.PROP_PROXY_PASSWORD, conf.getProxyPassword());
		return this;
	}

	
	public static PoolingHttpClientConnectionManager createUnsafeSSLHttpsConMngr() throws HTTPWrapperException {
		try {
			SSLContextBuilder builder = SSLContexts.custom();
			builder.loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
					return true;
				}
			});
			SSLContext sslContext = builder.build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext, new X509HostnameVerifier() {
						@Override
						public void verify(String host, SSLSocket ssl)
								throws IOException {
						}

						@Override
						public void verify(String host, X509Certificate cert)
								throws SSLException {
						}

						@Override
						public void verify(String host, String[] cns,
								String[] subjectAlts) throws SSLException {
						}

						@Override
						public boolean verify(String s, SSLSession sslSession) {
							return true;
						}
					});

			Registry<ConnectionSocketFactory> scktFctryReg = RegistryBuilder
					.<ConnectionSocketFactory>create()
					.register("https", sslsf)
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.build();

			return new PoolingHttpClientConnectionManager(scktFctryReg);
		}catch(NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
			throw new HTTPWrapperException(null, null, ex);
		}
	}
	/**
	 * 
	 * equivalent to "curl -k" in Java. Disable server authenticity check.
	 * Make sure you only use this only on development environment or on unsensitive content.
	 * 
	 * @return
	 * @throws HTTPWrapperException
	 */
	public static CloseableHttpClient createUnsecureSSLHttpsClient() throws HTTPWrapperException {
		return HttpClients.custom().setConnectionManager(createUnsafeSSLHttpsConMngr()).build();
	}

	public static CloseableHttpClient createProxifiedHttpClient() {
		CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(new AuthScope(HTTPWrapper.getProxyHost()
				, Integer.valueOf(HTTPWrapper.getProxyPort()))
				, new UsernamePasswordCredentials(HTTPWrapper.getProxyUser()
						, HTTPWrapper.getProxyPassword()));
		return HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD).build())
				.setDefaultCredentialsProvider(provider).build();
	}

	private final class HttpReqWrapper {

		private String url;
		private HttpEntityEnclosingRequestBase httpReq;
		private InputStream bodyIS;
		private List<? extends Header> headers;
		private List<NameValuePair> parameters;
		private HttpClientContext context;
		private ContentType contentType;

		public ContentType getContentType() {
			return contentType;
		}
		public HttpReqWrapper setContentType(ContentType contentType) {
			this.contentType = contentType;
			return this;
		}
		public String getUrl() {
			return url;
		}
		public HttpReqWrapper setUrl(String url) {
			this.url = url;
			return this;
		}
		public HttpEntityEnclosingRequestBase getHttpReq() {
			return httpReq;
		}
		public HttpReqWrapper setHttpReq(HttpEntityEnclosingRequestBase httpReq) {
			this.httpReq = httpReq;
			return this;
		}
		public InputStream getBodyIS() {
			return bodyIS;
		}
		public HttpReqWrapper setBodyIS(InputStream bodyIS) {
			this.bodyIS = bodyIS;
			return this;
		}
		public List<? extends Header> getHeaders() {
			return headers;
		}
		public HttpReqWrapper setHeaders(List<? extends Header> headers) {
			this.headers = headers;
			return this;
		}
		public List<NameValuePair> getParameters() {
			return parameters;
		}
		public HttpReqWrapper setParameters(List<NameValuePair> parameters) {
			this.parameters = parameters;
			return this;
		}
		public HttpClientContext getContext() {
			return context;
		}
		public HttpReqWrapper setContext(HttpClientContext context) {
			this.context = context;
			return this;
		}
		@Override
		public String toString() {
			return "HttpReqWrapper [url=" + url + ", httpReq=" + httpReq + ", bodyIS=" + bodyIS + ", headers=" + headers
					+ ", parameters=" + parameters + ", context=" + context + "]";
		}
	}

}
