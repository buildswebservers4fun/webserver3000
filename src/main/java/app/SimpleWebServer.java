package app;

import java.io.File;
import java.io.IOException;

import server.Server;

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
		server.start();
	}
}
