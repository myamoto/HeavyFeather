package org.toolup.io.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

public class ObjectMapperHolder {
	private static final  ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, true);
	}
	
	private ObjectMapperHolder() {}
	
	public static ObjectMapper getObjectmapper() {
		return objectMapper;
	}
	
	
}
