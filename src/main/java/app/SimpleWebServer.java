package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.util.IOUtils;

import server.Server;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class SimpleWebServer {
	private static Logger logger = LogManager.getLogger(SimpleWebServer.class.getName());
	
	public static void main(String[] args) throws InterruptedException, IOException {
		Properties props = new Properties(); 
	    try { 
	        InputStream configStream = SimpleWebServer.class.getResourceAsStream( "/log4j.properties"); 
	        props.load(configStream); 
	        configStream.close(); 
	    } catch (IOException e) { 
	        System.out.println("Error: Cannot laod configuration file "); 
	    }
		
		// DONE: Server configuration, ideally we want to read these from an application.properties file
		Properties prop = new Properties();
		String propName = "application.properties";
		
		File properties = new File("./application.properties");
		if (!properties.exists()){
			InputStream stream = SimpleWebServer.class.getClassLoader().getResourceAsStream(propName);
			FileOutputStream fo = new FileOutputStream(properties);
			IOUtils.copy(stream, fo);
			stream.close();
			fo.close();
		}
		
		FileReader fr = new FileReader(properties);
		prop.load(fr);
		
		String rootDirectory = prop.getProperty("rootDirectory");
		int port = Integer.parseInt(prop.getProperty("port"));

		// Create a run the server
		Server server = new Server(rootDirectory, port);
		Thread runner = new Thread(server);
		runner.start();

		
		// DONE: Instead of just printing to the console, use proper logging mechanism.
		// SL4J/Log4J are some popular logging framework
		logger.info("Simple Web Server started at port %d and serving the %s directory ...%n", port, rootDirectory);
		
		// Wait for the server thread to terminate
		runner.join();
	}
}
