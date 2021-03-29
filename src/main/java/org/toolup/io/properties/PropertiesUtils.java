package org.toolup.io.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtils {
	private static Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);
	
	private PropertiesUtils() {}
	
	public static String getString(Properties props, String key)  {
		if(!props.containsKey(key))
			logger.warn(String.format("warning : String property not found : %s", key));
		return props.getProperty(key);
	}
	
	public static boolean getMandatoryBool(Properties props, String key) throws PropertiesUtilsException {
		if(!props.containsKey(key))throw new PropertiesUtilsException(String.format("mandatory boolean property not found : %s", key));
		return Boolean.parseBoolean(props.getProperty(key));
	}

	public static String getMandatoryString(Properties props, String key) throws PropertiesUtilsException {
		if(!props.containsKey(key))throw new PropertiesUtilsException(String.format("mandatory String property not found : %s", key));
		return props.getProperty(key);
	}
	
	public static int getMandatoryInt(Properties props, String key) throws PropertiesUtilsException {
		if(!props.containsKey(key))throw new PropertiesUtilsException(String.format("mandatory int property not found : %s", key));
		String val = props.getProperty(key);
		try {
			return Integer.parseInt(val);
		}catch(NumberFormatException ex) {
			throw new PropertiesUtilsException(String.format("not a valid int for key %s : %s", key, val));
		}
	}
	
	public static Properties loadProperties(String confFilePath) throws PropertiesUtilsException {
		if(confFilePath == null) throw new PropertiesUtilsException("conf file can't be null.");
		File confFile = new File(confFilePath);
		
		if(!confFile.exists() || !confFile.canRead())throw new PropertiesUtilsException("config file %s must be readable.");

		Properties confProps;
		try (FileInputStream confIs = new FileInputStream(confFile)){
			confProps = new Properties();
			confProps.load(confIs);
			return confProps;
		} catch (IOException e) {
			throw new PropertiesUtilsException(e);
		}
	}

	
}
