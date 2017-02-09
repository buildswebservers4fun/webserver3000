package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dynamic.IPluginRouter;
import dynamic.PluginRouter;
import utils.ErrorLogger;

public class SecureServer implements Observer{
	
	private final PluginRouter router;
	private String rootDirectory;
	private int port;
	private ServerSocket welcomeSocket;
	private ConnectionHandler handler;
	private HashMap<String, Class<? extends IPluginRouter>> contextRootToPlugin;

	Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * @param rootDirectory
	 * @param port
	 * @param router
	 */
	public SecureServer(String rootDirectory, int port, PluginRouter router) {
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.router = router;
	}

	/**
	 * Gets the root directory for this web server.
	 * 
	 * @return the rootDirectory
	 */
	public String getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * Gets the port number for this web server.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * The entry method for the main server thread that accepts incoming TCP
	 * connection request and creates a {@link ConnectionHandler} for the
	 * request.
	 */
	public void start() {
		try {
			this.welcomeSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
		} catch (IOException e1) {
			RuntimeException e = new RuntimeException("Server unable to start", e1);
			ErrorLogger.getInstance().error(e);
			throw e;
		}

		try {
			// Now keep welcoming new connections until stop flag is set to true
			logger.info(String.format("Secure Web Server started at port %d and serving the %s directory ...%n", port,
					rootDirectory));
			while (true) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				SSLSocket connectionSocket = (SSLSocket) welcomeSocket.accept();
				// Create a handler for this incoming connection and start the
				// handler in a new thread
				System.out.println("ConnectionHandler Created");
				handler = new ConnectionHandler(connectionSocket, router);
				new Thread(handler).start();
			}
		} catch (SocketException e) {
			// Ignore these
			System.out.println("socket exception");
		} catch (Exception e) {
			ErrorLogger.getInstance().error(e);
		}

	}

	/**
	 * Stops the server from listening further.
	 */
	public synchronized void stop() {
		if (!welcomeSocket.isClosed())
			try {
				welcomeSocket.close();
			} catch (IOException e) {
			}
	}

	/**
	 * Checks if the server is stopeed or not.
	 * 
	 * @return
	 */
	public boolean isStoped() {
		if (this.welcomeSocket != null)
			return this.welcomeSocket.isClosed();
		return true;
	}

	@Override
	public void update(Observable o, Object obj) {
		System.out.println("server got update");
		System.out.println(obj.toString());
		contextRootToPlugin = (HashMap<String, Class<? extends IPluginRouter>>) obj;
	}

}
