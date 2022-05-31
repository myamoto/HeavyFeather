package org.toolup.io.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperHolder {
	private static final  ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
	}
	
	private ObjectMapperHolder() {}
	
	public static ObjectMapper getObjectmapper() {
		return objectMapper;
	}
	
	
}
