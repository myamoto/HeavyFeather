package org.toolup.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Load parameters according to a defined hierarchy of scopes.
 * 1) try to load from parameters set from program (meant to be used from java code)
 * 2) if empty, try to load from program arguments (needs to be registered at application startup)
 * 3) if empty, try to load from System properties
 * 4) if empty, try to load from Env variables
 * 
 * TODO : AbstractApplication : auto-register program arguments
 * TODO : easy system to get all the available "Parameter keys" in a program.
 * Ideally in a declarative and detectable manner (annotation ...).
 * TODO : plug spring-cloud-config
 *
 */
public class ParamLoader {
	
	private static Logger logger = LoggerFactory.getLogger(ParamLoader.class);
	
	private final static Hashtable<String, String> paramRegister = new Hashtable<>();
	
	public static String getParam(String key) {
		return paramRegister.get(key);
	}
	
	public static void setParam(String key, String value) {
		paramRegister.put(key, value);
	}
	
	public static List<String> getParams(){
		List<String> result = new ArrayList<String>(paramRegister.keySet());
		Collections.sort(result);
		return result;
	}
	
	
	public static String load(String key, String defaultVal) {
		String result = load(key);
		if(result == null) return defaultVal;
		return result;
	}
	
	public static String load(String key) {
		String result = paramRegister.get(key);
		logger.debug("load {} from toolup's parameters register => {}", key, result);
		if(result == null) result = ArgumentRegister.get(key);
		logger.debug("load {} from toolup's arguments register => {}", key, result);
//		if(result == null) result = getSpringCloudConfig("paramName");
		if(result == null) result = System.getProperty(key);
		logger.debug("load {} from System property => {}", key, result);
		if(result == null) result = System.getenv(key);
		logger.debug("load {} from Env => {}", key, result);
		return result;
	}
}
