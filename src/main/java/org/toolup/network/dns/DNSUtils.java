package org.toolup.network.dns;

import java.util.regex.Pattern;

public class DNSUtils {
	private static final  String VALID_HOST_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
	private static final  Pattern VALID_HOST_PTRN = Pattern.compile(VALID_HOST_REGEX);
	
	private DNSUtils() {/*static class*/}
	
	
	/**
	 * thanks to https://stackoverflow.com/a/106223
	 * based on https://tools.ietf.org/html/rfc1123
	 * @param h
	 * @return
	 */
	public static boolean isValidHost(String h) {
		return VALID_HOST_PTRN.matcher(h).matches();
	}
	
	public static void assertValidHost(String h) throws DNSException {
		if(!VALID_HOST_PTRN.matcher(h).matches())
			throw new DNSException(String.format("%s : not a valid host according to RFC1123 (see https://tools.ietf.org/html/rfc1123)", h), 400);
	}
	
}
