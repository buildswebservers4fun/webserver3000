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
import utils.AccessLogger;
import utils.ErrorLogger;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class SimpleWebServer {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		// DONE: Server configuration, ideally we want to read these from an application.properties file
		File properties = new File("./application.properties");
		ApplicationSettings settings = new ApplicationSettings(properties);
		
		String rootDirectory = settings.getRootDirectory();
		int port = settings.getPort();
		
		// Create a run the server
		Server server = new Server(rootDirectory, port);
		Thread runner = new Thread(server);
		runner.start();

		
		// DONE: Instead of just printing to the console, use proper logging mechanism.
		// SL4J/Log4J are some popular logging framework
		
		ErrorLogger.getInstance().error(String.format("Simple Web Server started at port %d and serving the %s directory ...%n", port, rootDirectory));
		AccessLogger.getInstance().info("trace");
		
		// Wait for the server thread to terminate
		runner.join();
	}
}
