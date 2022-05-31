package org.toolup.secu.oauth.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;


public final class JSONObject {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private Map<String, Object> map;

	public static JSONObject create() {
		return new JSONObject();
	}

	public static JSONObject create(final String json) throws IOException {
		return mapper.readValue(json, JSONObject.class);
	}

	private JSONObject() {
		map = new HashMap<String, Object>();
	}

	@JsonCreator
	private JSONObject(final Map<String, Object> map) {
		this();
		putAll(map);
	}

	@JsonAnyGetter
	public Map<String, Object> get() {
		return map;
	}

	public Object get(final String key) {
		return map.get(key);
	}
	
	public int getInt(final String key) {
		return (Integer) get(key);
	}
	
	public long getLong(final String key) {
		Object value = get(key);
		return (value instanceof Long) ? (Long) value : new Long((Integer) value);
	}

	public Object put(final String key, final Object value) {
		return map.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public void putAll(final Map<String, ? extends Object> map) {
		
		for (final String key : map.keySet()) {
			Object value = map.get(key);
			
			if (value instanceof List) {
				List<Object> l = (List<Object>) value;
				
				for (int i = 0, size = l.size(); i < size; ++i) {
					Object o = l.get(i);
					if (!Map.class.isAssignableFrom(o.getClass())) continue;
					l.set(i, new JSONObject((Map<String, Object>) o));
				}
			}
			
			put(key, (value instanceof Map) ? new JSONObject((Map<String, Object>) value) : value);
		}
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			return "";
		}
	}
	
}