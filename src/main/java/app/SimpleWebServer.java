package app;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.Server;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class SimpleWebServer {
	
	private static Logger logger = LogManager.getLogger(SimpleWebServer.class);

	
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
		logger.info(String.format("Simple Web Server started at port %d and serving the %s directory ...%n", port, rootDirectory));
	}
}
