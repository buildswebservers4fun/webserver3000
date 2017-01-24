package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.api.client.util.IOUtils;

public class ApplicationSettings {

	private String rootDirectory;
	private int port;
	
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
		
		rootDirectory = prop.getProperty("rootDirectory", "./");
		port = Integer.parseInt(prop.getProperty("port", "8080"));
	}
	
	public String getRootDirectory() {
		return this.rootDirectory;
	}
	
	public int getPort() {
		return this.port;
	}
}
