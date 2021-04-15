package org.toolup.secu.oauth;

import java.io.IOException;
import java.util.Base64;
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
import org.toolup.secu.oauth.jwt.parse.JWTParserFactory;

public final class OAuthBearerFilter implements Filter {
	private static Logger logger = LoggerFactory.getLogger(OAuthBearerFilter.class);

	private static final String BEARER_PREFIX = "Bearer ";

	private JWTParserFactory jwtBuilder;

	public OAuthBearerFilter() throws OAuthException {
		initJWTBuilder();
	}

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

			JWT jwt = jwtBuilder.parse(header.substring(BEARER_PREFIX.length()));
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
			logger.error("{}", e.getMessage());
			((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden.");
			return;
		} catch (Exception e) {
			if(logger.isDebugEnabled())  
				logger.error("{}", e);
			else
				logger.error("{}", e.getMessage());
			((HttpServletResponse) resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
			return;
		}
		chain.doFilter(req, resp);
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		try {
			initJWTBuilder();
		} catch (OAuthException e) {
			throw new ServletException("", e);
		}
	}

	private void initJWTBuilder() throws OAuthException {
		if(jwtBuilder == null) {
			try {
				jwtBuilder = JWTParserFactory.newInstance();
			} catch (OAuthException e) {
				throw new OAuthException("Error initializing JWT Builder : newInstance bug.", e, 500);
			}	
		}

		try {
			if(jwtBuilder.getPublicKeys().isEmpty() && jwtBuilder.getDefaultPublicKey() == null)
				throw new OAuthException("Error initializing JWTBuilderFactory : keys were  empty", 500);
		} catch (OAuthException e) {
			throw new OAuthException("Error initializing JWT Builder : keys could not be retrieved", e, 500);
		}		
	}

	public String getDefaultPublicKey() throws OAuthException {
		return Base64.getEncoder().encodeToString(jwtBuilder.getDefaultPublicKey().getEncoded());
	}

}
