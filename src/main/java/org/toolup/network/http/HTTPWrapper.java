package org.toolup.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.IOUtils;
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
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.app.ParamLoader;
import org.toolup.network.http.HTTPWrapperException.HTTPVERB;

import com.jayway.jsonpath.Configuration;

public class HTTPWrapper {

	private final Logger logger = LoggerFactory.getLogger(HTTPWrapper.class);
	
	public static final String PROP_PROXY_HOST = "ProxyHost";
	public static final String PROP_PROXY_PORT = "ProxyPort";
	public static final String PROP_PROXY_USER = "ProxyUser";
	public static final String PROP_PROXY_PASSWORD = "ProxyPassword";

	private boolean useProxy = false;

	private static final List<Integer> DELETE_STATUSCODE_OK = Arrays.asList(200, 202, 204);
	private static final List<Integer> POST_STATUSCODE_OK = Arrays.asList(200, 201, 204);
	private static final List<Integer> GET_STATUSCODE_OK = Arrays.asList(200, 206);

	public HTTPWrapper useProxy(boolean useProxy) {
		this.useProxy = useProxy;
		return this;
	}

	private String parseJson(CloseableHttpResponse resp) throws IOException {
		return IOUtils.toString(resp.getEntity().getContent(), StandardCharsets.UTF_8);
	}

	public CloseableHttpResponse httpDelete(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		HttpDelete httpDelete = new HttpDelete(url);
		handleProxy(httpDelete);
		for (Header header : headers) {
			httpDelete.addHeader(header);
		}
		try(CloseableHttpResponse result = httpClient.execute(httpDelete)){
			if(!DELETE_STATUSCODE_OK.contains(result.getStatusLine().getStatusCode())) {
				throw new HTTPWrapperException(HTTPVERB.DELETE
						, result.containsHeader("X-Error") ? result.getFirstHeader("X-Error").getValue() : null
								, result.getStatusLine().getStatusCode()
								, url
								, IOUtils.toString(result.getEntity().getContent(), StandardCharsets.UTF_8));
			}
			return result;
		} catch (IOException e) {
			throw new HTTPWrapperException(HTTPVERB.DELETE, url, e);
		}
	}

	public CloseableHttpResponse httpPUT(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		return httpPUT(url, null, httpClient, headers, null);
	}

	public CloseableHttpResponse httpPUT(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return http(new HttpPut(url), url, bodyIS, httpClient, headers, parameters);
	}

	public CloseableHttpResponse httpPOST(String url, CloseableHttpClient httpClient, List<? extends Header> headers) throws HTTPWrapperException {
		return httpPOST(url, null, httpClient, headers, null);
	}

	public CloseableHttpResponse httpPOST(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return http(new HttpPost(url), url, bodyIS, httpClient, headers, parameters);
	}
	
	public Object httpPATCHParsedJson(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return Configuration.defaultConfiguration().jsonProvider().parse(httpPatchContent(url, bodyIS, httpClient, headers, parameters));
	}
	
	private String httpPatchContent(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		try(CloseableHttpResponse resp = httpPATCH(url, bodyIS, httpClient, headers, parameters)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, e);
		}
	}
	
	public CloseableHttpResponse httpPATCH(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return http(new HttpPatch(url), url, bodyIS, httpClient, headers, parameters);
	}

	public CloseableHttpResponse httpPOST(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters, HttpClientContext context) throws HTTPWrapperException {
		return http(new HttpPost(url), url, bodyIS, httpClient, headers, parameters, context);
	}
	
	public Object httpPOSTParsedJson(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters, HttpClientContext context) throws HTTPWrapperException {
		return Configuration.defaultConfiguration().jsonProvider().parse(httpPostContent(url, bodyIS, httpClient, headers, parameters, context));
	}
	
	private String httpPostContent(String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters, HttpClientContext context) throws HTTPWrapperException {
		try(CloseableHttpResponse resp = httpPOST(url, bodyIS, httpClient, headers, parameters, context)){
			return parseJson(resp);
		} catch (UnsupportedOperationException | IOException e) {
			throw new HTTPWrapperException(HTTPVERB.GET, url, e);
		}
	}

	private CloseableHttpResponse http(HttpEntityEnclosingRequestBase httpPost, String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters) throws HTTPWrapperException {
		return http(httpPost, url, bodyIS, httpClient, headers, parameters, null);
	}

	private CloseableHttpResponse http(HttpEntityEnclosingRequestBase httpReq, String url, InputStream bodyIS, CloseableHttpClient httpClient
			, List<? extends Header> headers, List<NameValuePair> parameters, HttpClientContext context) throws HTTPWrapperException {
		handleProxy(httpReq);
		if(bodyIS != null) {
			try {
				httpReq.setEntity(new StringEntity(IOUtils.toString(bodyIS, StandardCharsets.UTF_8)));
			} catch (IOException e) {
				throw new HTTPWrapperException(HTTPVERB.POST, url, e);
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
				throw new HTTPWrapperException(HTTPVERB.POST, url, e);
			}
		}
		try {
			CloseableHttpResponse result;

			if(context != null) result = httpClient.execute(httpReq, context);
			else result = httpClient.execute(httpReq);
			
			int status = result.getStatusLine().getStatusCode();
			if(!POST_STATUSCODE_OK.contains(status)) {
				String content;
				try {
					content = parseJson(result);
				}catch(IOException e) {
					content = null;
				}
				
				String xError = result.containsHeader("X-Error") ? result.getFirstHeader("X-Error").getValue() : null;
				if(logger.isDebugEnabled())
					logger.debug("http<{}> Exception 'invalid HTTP Status' : {} -> {} {} {}"
							, httpReq.getClass().getSimpleName()
							, url
							, status
							, content
							, xError == null ? "" : ", xError : " + xError);
				
				throw new HTTPWrapperException(
						HTTPVERB.POST
						, xError
								, status
								, url
								, content
								, result.getAllHeaders());
			}
			if(logger.isDebugEnabled())
				logger.debug("httpget OK on {} -> {}", url, status);
			return result;
		} catch (IOException e) {
			if(logger.isDebugEnabled())
				logger.debug("http<> Exception on {} -> {}"
						, httpReq.getClass().getSimpleName()
						, url
						, e.getMessage());
			throw new HTTPWrapperException(HTTPVERB.POST, url, e);
		}
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
				logger.debug("httpget Exception on {} -> {}", url, e.getMessage());
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
			logger.debug("httpget OK on {} -> {}", url, status);
		return result;
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

	public static String slashTerminatedUrl(String url) {
		return url == null || url.endsWith("/") ? url : url + "/";
	}

	public static final String getProxyHost() {
		return ParamLoader.load(PROP_PROXY_HOST);
	}
	public static final String getProxyPort() {
		return ParamLoader.load(PROP_PROXY_PORT);
	}

	public static final String getProxyUser() {
		return ParamLoader.load(PROP_PROXY_USER);
	}

	public static final String getProxyPassword() {
		return ParamLoader.load(PROP_PROXY_PASSWORD);
	}

	public static final void setProxyHost(String host) {
		ParamLoader.setParam(PROP_PROXY_HOST, host);
	}
	public static final void setProxyPort(String port) {
		ParamLoader.setParam(PROP_PROXY_PORT, port);
	}

	public static final void setProxyUser(String usr) {
		ParamLoader.setParam(PROP_PROXY_USER, usr);
	}

	public static final void setProxyPassword(String pwd) {
		ParamLoader.setParam(PROP_PROXY_PASSWORD, pwd);
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
					.build();

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(scktFctryReg);
			return HttpClients.custom().setConnectionManager(cm).build();
		}catch(NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
			throw new HTTPWrapperException(null, null, ex);
		}
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

}
