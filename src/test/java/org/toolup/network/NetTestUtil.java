package org.toolup.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.toolup.network.common.ServerParams;

public class NetTestUtil {
	
	private static Properties prop;
	
	static {
        try (InputStream input = NetTestUtil.class.getResourceAsStream("net-config.properties")) {
        	prop = new Properties();
            prop.load(input);
        } catch (IOException e) {
        	throw new Error(e);
        } 
	}

	public static ServerParams env(String name) {
		return new ServerParams(getPropertyOrFail(name + ".host"),
						getPropertyOrFail(name + ".user"), 
						getPropertyOrFail(name + ".password"));
	}

	private static String getPropertyOrFail(String key) {
		if (prop.containsKey(key)) {
			return prop.getProperty(key);
		} else {
			throw new IllegalArgumentException(key);
		}
	}

}
