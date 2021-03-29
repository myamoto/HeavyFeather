package org.toolup.io.json;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONIO {

	private static Logger logger = LoggerFactory.getLogger(JSONIO.class);
	private static boolean debug;

	private JSONIO() {
		//static class
	}

	public static Map<String, Object> readString(String json) throws JSonException{
		try {
			return json != null ? readStream(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8")))) : null;
		} catch (ConfigurationException e) {
			throw new JSonException(e);
		} catch (IOException e) {
			throw new JSonException(e);
		}
	}

	public static Map<String, Object> readFile(String jsonFilePath) throws ConfigurationException, IOException, JSonException{
		if(jsonFilePath != null){
			File jsonFile = new File(jsonFilePath);
			if(!jsonFile.exists() || !jsonFile.canRead()){
				throw new ConfigurationException(String.format("can't read json file %s.", jsonFile.getAbsolutePath()));
			}
			try{
				return readStream(new FileInputStream(jsonFile));
			}catch(JSonException ex){
				throw new JSonException(String.format("error parsing file %s.", jsonFilePath), ex);
			}
		}
		return null;
	}

	public static Object readFileObj(String jsonFilePath) throws ConfigurationException, IOException, JSonException{
		if(jsonFilePath != null){
			File jsonFile = new File(jsonFilePath);
			if(!jsonFile.exists() || !jsonFile.canRead()){
				throw new ConfigurationException(String.format("can't read json file %s", jsonFile.getAbsolutePath()));
			}
			try{
				return readStreamObj(new FileInputStream(jsonFile));
			}catch(JSonException ex){
				throw new JSonException(String.format("error parsing file %s.", jsonFilePath), ex);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> readStream(InputStream is) throws ConfigurationException, IOException, JSonException{
		Object result = readStreamObj(is);
		return result != null && result instanceof Map ? (Map<String, Object>)result : null;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object readStreamObj(InputStream is) throws ConfigurationException, IOException, JSonException{
		Object result = null;

		Reader reader = null;
		StringBuilder keySb = new StringBuilder(), valueSb = new StringBuilder();
		try{

			reader = new InputStreamReader(is, Charset.forName("UTF-8"));

			int prevChar = -1;
			int currentChar = -2;
			boolean isValue = true, inValueQuote = false;

			Stack<Object> valueStack = new Stack<>();

			while ((currentChar = reader.read()) != -1) {
				try {
					char c = (char)currentChar;

					/**
					 * isValue == false
					 */
					if(!isValue && Character.isWhitespace(c)) continue;
					if(!isValue) debug(c + " isKey (key = " + keySb +")");


					if(!isValue && c == ':'){
						isValue = true;
						continue;
					}

					if(!isValue && c != ':'){
						keySb.append(c);
						continue;
					}

					/**
					 * isValue == true
					 */
					if(Character.isWhitespace(c) && !inValueQuote) {
						debug(" > escaped space");
						continue;
					}


					if(inValueQuote) debug(c + " isValue quoted (key : " + keySb + ", value : " + valueSb + ")");
					else debug(c + " isValue not quoted (key : " + keySb + ", value : " + valueSb + ")");


					if(inValueQuote && c == '"' && prevChar != '\\'){
						debug(" > closing value quote");
						inValueQuote = false;
						valueSb.append(c);
						continue;
					}

					if(!inValueQuote && c == '"' && prevChar != '\\'){
						debug(" > opening value quote");
						inValueQuote = true;
						valueSb.append(c);
						continue;
					}

					if(inValueQuote) {
						valueSb.append(c);
						continue;
					}

					if(c == '{'){
						Object newElem = new HashMap<String, Object>();
						if(!valueStack.isEmpty()){
							Object currentElem = valueStack.lastElement();
							if(currentElem instanceof Map){
								((Map)currentElem).put(cleanJsonString(keySb.toString()), newElem);
							}else if(currentElem instanceof List){
								((List)currentElem).add(newElem);
							}
						}
						keySb = new StringBuilder();

						valueStack.add(newElem);
						isValue = false;
						continue;
					}
					if(c == '}'){
						result = valueStack.pop();
						debug("result = valueStack.pop() " + result);
						if(keySb.length() > 0 && valueSb.length() > 0){
							if(result instanceof Map){
								((Map)result).put(cleanJsonString(keySb.toString()), parseValue(valueSb.toString()));
							}else if(result instanceof List){
								((List)result).add(parseValue(valueSb.toString()));
							}
							keySb = new StringBuilder();
							valueSb = new StringBuilder();
						}
						continue;
					}
					if(c == '['){
						Object newElem = new ArrayList<Object>();
						if(!valueStack.isEmpty()){
							Object currentElem = valueStack.lastElement();
							if(currentElem instanceof Map){
								((Map)currentElem).put(cleanJsonString(keySb.toString()), newElem);
							}else if(currentElem instanceof List){
								((List)currentElem).add(newElem);
							}
						}
						keySb = new StringBuilder();
						valueStack.add(newElem);
						continue;
					}
					if(c == ']' ){
						result = valueStack.pop();
						debug("result = valueStack.pop() " + result);
						continue;
					}

					if(c == ','){
						Object currentElem = valueStack.lastElement();
						if(currentElem instanceof Map){
							isValue = false;
							((Map)currentElem).put(cleanJsonString(keySb.toString()), parseValue(valueSb.toString()));
							keySb = new StringBuilder();
							valueSb = new StringBuilder();
						}else if(currentElem instanceof List && valueSb.length() > 0){
							((List)currentElem).add(parseValue(valueSb.toString()));
							valueSb = new StringBuilder();
						}
						continue;
					}

					valueSb.append(c);
				}finally {
					prevChar = currentChar;
				}
			}

		}catch(NumberFormatException ex){
			throw new JSonException(String.format("invalid json format. (%s)", keySb.toString()), ex);
		}finally{
			if(reader != null){
				reader.close();
			}
		}
		return result;
	}

	public static void setDebug(boolean debug) {
		JSONIO.debug = debug;
	}

	private static void debug(String msg) {
		if(debug) logger.debug(msg);
	}

	private static Object parseValue(String valueStr) {
		valueStr = valueStr.trim();
		if("".equals(valueStr)){
			return null;
		}
		if("null".equals(valueStr)) return null;

		if("true".equals(valueStr) || "false".equals(valueStr))return Boolean.parseBoolean(valueStr);
		if(isJsonString(valueStr))return cleanJsonString(valueStr);

		return Long.parseLong(valueStr);
	}

	private static boolean isJsonString(String str) {
		return str.startsWith("\"") && str.endsWith("\"");
	}

	private static String cleanJsonString(String str) {
		return  isJsonString(str) ? str.substring(1, str.length() - 1) : str;
	}



}

