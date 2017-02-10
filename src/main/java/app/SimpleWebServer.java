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
		// DONE: Server configuration, ideally we want to read these from an application.properties file
		File properties = new File("./application.properties");
		ApplicationSettings settings = new ApplicationSettings(properties);

		System.setProperty("javax.net.ssl.keyStore", "keystore/keystore.jks");
		System.setProperty("javax.net.ssl.trustStore", "keystore/cacerts.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "webserver");

		Thread.currentThread().setName("Server-thread");

		PluginRouter router = new PluginRouter();
		// Create Watch Service
        new Heartbeat(settings.getPort(), settings.getErrorCount(), settings.getInterval());
        DirectoryWatcher watcher = new DirectoryWatcher(settings.getPluginsDirectory(), router, settings.getRootDirectory());

		// Create a run the server
		Server server = new Server(settings, router);

		new Thread(new Runnable() {
			@Override
			public void run() {
				SecureServer secure = new SecureServer(settings.getRootDirectory(), 443, router, settings.isCacheEnabled(), settings.getCacheTimeLimit());
				secure.start();
			}
		}).start();

		watcher.start();
		System.out.println(("Plugin Watcher Started"));
		server.start();
	}
}
