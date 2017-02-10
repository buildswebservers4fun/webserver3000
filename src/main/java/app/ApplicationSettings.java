package app;

import com.google.api.client.util.IOUtils;

import java.io.*;
import java.util.Properties;

public class ApplicationSettings {

	private String rootDirectory;
	private int port;
	private String plugins;
	private boolean cacheEnabled;
	private long cacheTimeLimit;
	private int heartbeatInteveral;
	private int heartbeatErrorCount;

	public ApplicationSettings(File properties) throws IOException {
		Properties prop = new Properties();
		String propName = "application.properties";
		
		if (!properties.exists()){
			InputStream stream = SimpleWebServer.class.getClassLoader().getResourceAsStream(propName);
			FileOutputStream fo = new FileOutputStream(properties);
			IOUtils.copy(stream, fo);
			stream.close();
			fo.close();
		}
		
		FileReader fr = new FileReader(properties);
		prop.load(fr);
		
		rootDirectory = prop.getProperty("rootDirectory", "web");
		port = Integer.parseInt(prop.getProperty("port", "8080"));
		plugins = prop.getProperty("plugins", "plugins");
		cacheEnabled = Boolean.parseBoolean(prop.getProperty("cache-enabled", Boolean.toString(true)));
		cacheTimeLimit = Long.parseLong(prop.getProperty("cache-time-limit", Long.toString(10000)));
		heartbeatInteveral = Integer.parseInt(prop.getProperty("heartbeatInterval", "10"));
		heartbeatErrorCount = Integer.parseInt(prop.getProperty("heartbeatErrorCount", "5"));
	}
	
	public String getRootDirectory() {
		return this.rootDirectory;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getPluginsDirectory() {
		return this.plugins;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public long getCacheTimeLimit() {
		return cacheTimeLimit;
	}

	public int getErrorCount() {
		return heartbeatErrorCount;
	}

	public int getInterval() {
		return heartbeatInteveral;
	}
}
