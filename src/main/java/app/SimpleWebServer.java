package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dynamic.DirectoryWatcher;
import server.Server;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class SimpleWebServer {
	public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
		// DONE: Server configuration, ideally we want to read these from an application.properties file
		File properties = new File("./application.properties");
		ApplicationSettings settings = new ApplicationSettings(properties);
		
		String rootDirectory = settings.getRootDirectory();
		int port = settings.getPort();
		String dir = settings.getPluginsDirectory();
		
		// Create Watch Service
        DirectoryWatcher watcher = new DirectoryWatcher(dir);
        

		// Create a run the server
		Server server = new Server(rootDirectory, port);
		watcher.addObserver(server);
		
		System.out.println("watcher observer count: " + watcher.countObservers());

		watcher.start();
		System.out.println(("watcher started"));
		server.start();
		System.out.println("server started");

	}
}
