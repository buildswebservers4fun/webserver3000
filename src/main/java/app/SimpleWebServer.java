package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dynamic.DirectoryWatcher;
import dynamic.PluginRouter;
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

		PluginRouter router = new PluginRouter();
		// Create Watch Service
        DirectoryWatcher watcher = new DirectoryWatcher(settings.getPluginsDirectory(), router, settings.getRootDirectory());

		// Create a run the server
		Server server = new Server(settings, router);

    	watcher.start();
		System.out.println(("Plugin Watcher Started"));
		server.start();
	}
}
