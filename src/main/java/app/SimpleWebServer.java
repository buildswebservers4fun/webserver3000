package app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import dynamic.DirectoryWatcher;
import dynamic.PluginRouter;
import server.SecureServer;
import server.Server;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class SimpleWebServer {
	public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
		// DONE: Server configuration, ideally we want to read these from an
		// application.properties file
		File properties = new File("./application.properties");
		ApplicationSettings settings = new ApplicationSettings(properties);

		System.setProperty("javax.net.ssl.keyStore", "keystore/keystore.jks");
		System.setProperty("javax.net.ssl.trustStore", "keystore/cacerts.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "webserver");

		String rootDirectory = settings.getRootDirectory();
		int port = settings.getPort();
		String dir = settings.getPluginsDirectory();

		//
		PluginRouter router = new PluginRouter();
		// Create Watch Service
		DirectoryWatcher watcher = new DirectoryWatcher(dir, router, rootDirectory);

		// Create a run the server

		Server server = new Server(rootDirectory, port, router);

		new Thread(new Runnable() {
			@Override
			public void run() {
				SecureServer secure = new SecureServer(rootDirectory, 443, router);
				secure.start();
			}
		}).start();

		watcher.start();
		System.out.println(("Plugin Watcher Started"));
		server.start();
	}
}
