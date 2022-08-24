package org.toolup.secu.oauth.jwt.parse;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JWT;
import org.toolup.secu.oauth.jwt.parse.keys.UrlKeyCache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJWTParserFactory {
	
	private final static ObjectMapper objectMapper = new ObjectMapper();
	static{
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private Logger logger = LoggerFactory.getLogger(TestJWTParserFactory.class);
	
	@Test
	public void test() throws OAuthException {
		System.setProperty(UrlKeyCache.OAUTH_PUBLIC_KEY_URL_PARAM, "http://authgateway-qualif.si.cnaf.info:9999/oauth/keys");
		
		String tkn = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJkZXYiLCJzdWIiOiJtY29ydDc1NSIsIm5iZiI6MTY0MjQwNzQ5MTk0MiwiYWRHcm91cHMiOiJHR0ZfSVNVX0RFTUFOREVVUkROUyIsImRvbWFpbiI6InByaXZhdGUiLCJyb2xlcyI6W10sInNjb3BlIjoicHVibGljIiwiaXNzIjoiaHR0cDovL2F1dGgtcXVhbGlmLnNpLmNuYWYuaW5mbyIsImV4cCI6MTY0MjQ5MzkwMTk0MiwiaWF0IjoxNjQyNDA3NTAxOTQyLCJqdGkiOiI0NGU0NWFiYy05NGE4LTQ4MjUtODk4YS0wZjVhNTY1ZGFmMDkiLCJjaWQiOiJlZjI2MGU5YWEzYzY3M2FmMjQwZDE3YTI2NjA0ODAzNjFhOGUwODFkMWZmZWNhMmE1ZWQwZTMyMTlmYzE4NTY3In0.OCfv4Jh43c2ostip8JhKHy959-Be6vurOuhFOfF_uWuXaeuzBUjT7FZwWfIPs9sVR8CT11HB6BfbWw_9l1vjdNVcQ7OxydVXf1Kgbbd3w1U0R9vKdbP753D1EO6NHI8SlWK_rN31i1AjfXZUQpChMI6n8c4Js87rn_oVRop8Gmj_MAK6-pfmuHv1dw-Ijrn868byUBnD903HPf6ZaQRT0aCZJd2Nlb5MefBPqqmum2GzfgoB6_nwi6PfYAXSnMlZMBbE24ilVrBOr0t9wv2qMZTu55p8QN-umnszJgd_bw64YK24d0DivpYGtGt-mCwN5btylp7uwCj9sS1dfdxIpA";
		JWT jwt = JWTParserFactory.newInstance().parse(tkn);
		
		try {
			logger.info("jwt : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jwt));
		} catch (IOException e) {
			logger.error("{}", e);
			Assert.fail(e.getMessage());
		}
	}
}

