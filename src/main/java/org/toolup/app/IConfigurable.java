package org.toolup.app;

import java.util.Properties;

import org.toolup.io.properties.PropertiesUtilsException;

public interface IConfigurable<T> {

	public T configure(Properties props) throws ConfigurationException, PropertiesUtilsException;
}
