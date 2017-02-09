/*
 * Server.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 */

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import app.ApplicationSettings;
import dynamic.PluginRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dynamic.IPluginRouter;
import utils.ErrorLogger;

/**
 * This represents a welcoming server for the incoming TCP request from a HTTP
 * client such as a web browser.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class Server implements Observer {
	private final PluginRouter router;
	private final boolean isCacheEnabled;
	private final long cacheTimeLimit;
	private String rootDirectory;
	private int port;
	private ServerSocket welcomeSocket;
	private ConnectionHandler handler;
	private HashMap<String, Class<? extends IPluginRouter>> contextRootToPlugin;

	Logger logger = LogManager.getLogger(this.getClass());

	public Server(ApplicationSettings settings, PluginRouter router) {
		this(settings.getRootDirectory(), settings.getPort(), router, settings.isCacheEnabled(), settings.getCacheTimeLimit());
	}

	public Server(String rootDirectory, int port, PluginRouter router, boolean isCacheEnabled, long cacheTimeLimit) {
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.router = router;
		this.isCacheEnabled = isCacheEnabled;
		this.cacheTimeLimit = cacheTimeLimit;
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
			this.welcomeSocket = new ServerSocket(port);
		} catch (IOException e1) {
			RuntimeException e = new RuntimeException("Server unable to start", e1);
			ErrorLogger.getInstance().error(e);
			throw e;
		}

		try {
			// Now keep welcoming new connections until stop flag is set to true
			logger.info(String.format("Simple Web Server started at port %d and serving the %s directory ...%n", port,
					rootDirectory));
			while (true) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				Socket connectionSocket = welcomeSocket.accept();
				// Create a handler for this incoming connection and start the
				// handler in a new thread
				System.out.println("ConnectionHandler Created");
				handler = new ConnectionHandler(connectionSocket, router,isCacheEnabled, cacheTimeLimit);
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
