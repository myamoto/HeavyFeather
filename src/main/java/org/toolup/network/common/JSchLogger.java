package org.toolup.network.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSchLogger implements com.jcraft.jsch.Logger {

	private final static Logger slf4j = LoggerFactory.getLogger(JSchLogger.class);
	
	@Override
	public boolean isEnabled(int level) {
		return true;
	}

	@Override
	public void log(int level, String message) {
		switch (level) {
			case DEBUG:
				slf4j.debug("{}", message);
				break;
			case INFO:
				slf4j.info("{}", message);
				break;
			case WARN:
				slf4j.warn("{}", message);
				break;
			case ERROR:
			case FATAL:
				slf4j.error("{}", message);
				break;
		}
	}

}
