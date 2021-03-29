package org.toolup.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class ArgumentRegister {
	
	private final static Hashtable<String, String> args = new Hashtable<>();

	public static String get(String key) {
		return args.get(key);
	}
	
	public static void add(String key, String value) {
		args.put(key, value);
	}
	
	public static List<String> getArgs(){
		List<String> result = new ArrayList<String>(args.keySet());
		Collections.sort(result);
		return result;
	}

}
