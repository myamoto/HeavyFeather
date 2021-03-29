package org.toolup.secu.oauth;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.toolup.secu.oauth.jwt.JWT;
import org.toolup.secu.oauth.jwt.JWTBuilderFactory;

public final class OAuthBearerFilter implements Filter {
	private static Logger logger = LoggerFactory.getLogger(OAuthBearerFilter.class);
	
	private static final String BEARER_PREFIX = "Bearer ";
	@Override
	public void destroy() {}
	
	@Override
	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain)
			throws IOException, ServletException {
		try {
			/*
			 * assert Bearer validity
			 */
			String header = ((HttpServletRequest) req).getHeader("Authorization");
			logger.debug("parsing authorization header {}", header);
			if ((header == null) || (!header.startsWith(BEARER_PREFIX)))
				throw new OAuthException(String.format("missing Bearer token. make sure it is of the form '%s{{bearer}}'", BEARER_PREFIX));
			
			JWT jwt = JWTBuilderFactory.newInstance().build(header.substring(BEARER_PREFIX.length()));
			((HttpServletRequest) req).setAttribute(JWT.REQ_ATTRBT, jwt);

			/*
			 * plug to Spring-security
			 * - inject jwt.roles + jwt.scope in AuthorityList
			 * - inject jwt.subject in securityContext Authentication
			 */
			Collection<String> roles = Stream.concat(jwt.getRoles().stream(), Stream.of(jwt.getScope())).collect(Collectors.toList());
			Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roles.toArray(new String[roles.size()]));
			
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new User(jwt.getSubject(), "", authorities), "", authorities));
		} catch (OAuthException e) {
			logger.error("{}", e);
			((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden.");
		} catch (Exception e) {
			logger.error("{}", e);
			((HttpServletResponse) resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
		}
		chain.doFilter(req, resp);
	}
	
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {}
}
