package org.toolup.secu.oauth.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JWT {
	
	public enum STD_CLAIMS {jti, scope, sub, roles};
	public static String REQ_ATTRBT = JWT.class.getSimpleName();
	
	public static List<String> RESERVED_CLAIMS = Arrays.asList("iat", "nbf", "exp", "aud", "iss", "jti", "sub", "scope", "roles");
	public static List<String> FILTER_CLAIMS = Arrays.asList("iat", "nbf", "exp", "scope", "roles");
	
	
	private String token;
	private JSONObject header;
	private JSONObject claims;
	
	public JWT setToken(String token) {
		this.token = token;
		return this;
	}
	
	public JWT token(String token) {
		this.token = token;
		return this;
	}
	
	public JWT setHeader(JSONObject header) {
		this.header = header;
		return this;
	}
	
	public JWT header(JSONObject header) {
		this.header = header;
		return this;
	}
	
	public JWT setClaims(JSONObject claims) {
		this.claims = claims;
		return this;
	}
	
	public JWT claims(JSONObject claims) {
		this.claims = claims;
		return this;
	}

	public Map<String, String> getClaims() {
		return claims == null || claims.get() == null ? null : 
			claims.get()
			.entrySet()
			.stream()
			.filter(e -> !FILTER_CLAIMS.contains(e.getKey()))
			.collect(Collectors.toMap(map -> (String)map.getKey(), map -> String.valueOf(map.getValue())));
	}

	public String getID() {
		return getClaim(STD_CLAIMS.jti, String.class);
	}

	private <T> T getClaim(STD_CLAIMS claim, Class<T> clazz) {
		if(claim == null) return null;
		Object res = claims.get(claim.name());
		if(!clazz.isAssignableFrom(res.getClass())) return null;
		return  clazz.cast(res);
	}
	
	private <T> Collection<T> getClaimCollection(STD_CLAIMS claim, Class<T> clazz) {
		if(claim == null) return null;
		Object res = claims.get(claim.name());
		if(!Collection.class.isAssignableFrom(res.getClass())) return null;
		return  ((Collection<?>)res).stream().map(e -> clazz.cast(e)).collect(Collectors.toList());
	}

	public Collection<String> getRoles() {
		return getClaimCollection(STD_CLAIMS.roles, String.class);
	}

	public String getScope() {
		return getClaim(STD_CLAIMS.scope, String.class);
	}

	public String getSubject() {
		return getClaim(STD_CLAIMS.sub, String.class);
	}

	@Override
	public String toString() {
		return token;
	}
}