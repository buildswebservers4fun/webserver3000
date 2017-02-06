package server;

import dynamic.IPluginRouter;
import dynamic.IServlet;
import protocol.HttpRequest;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.ServerException;
import protocol.handler.DefaultHandler;
import protocol.response.HttpResponseBuilder;
import protocol.response.IHttpResponse;
import utils.AccessLogger;
import utils.ErrorLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * This class is responsible for handling a incoming request by creating a
 * {@link HttpRequest} object and sending the appropriate response be creating a
 * {@link IHttpResponse} object. It implements {@link Runnable} to be used in
 * multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
	private Socket socket;
	private IServlet defaultServlet;
	private HashMap<String, Class<? extends IPluginRouter>> contextRootToPlugin;

	// TODO pass in plugins
	public ConnectionHandler(String rootDirectory, Socket socket, HashMap<String, Class<? extends IPluginRouter>> contextRootToPlugin) {
		this.socket = socket;

		this.contextRootToPlugin = contextRootToPlugin;
		System.out.println("connection handler's plugin map: " + contextRootToPlugin.size());
		defaultServlet = DefaultHandler.createDefaultHandler(rootDirectory);
	}

	public void SetPluginMap(HashMap<String, Class<? extends IPluginRouter>> map) {
		System.out.println("in connection handler");
		this.contextRootToPlugin = map;
	}

	/**
	 * The entry point for connection handler. It first parses incoming request
	 * and creates a {@link HttpRequest} object, then it creates an appropriate
	 * {@link IHttpResponse} object and sends the response back to the client
	 * (web browser).
	 */
	public void run() {
		// Refactor this code to make it
		// cohesive and extensible
		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			inStream = this.socket.getInputStream();
			outStream = this.socket.getOutputStream();
		} catch (IOException e) {
			ErrorLogger.getInstance().error(e);
			return;
		}

		HttpRequest request = null;
		IHttpResponse response = null;
		try {
			request = HttpRequest.read(inStream);
			AccessLogger.getInstance().info(request);
		} catch (ProtocolException pe) {
			int status = pe.getStatus();
			if (status == Protocol.BAD_REQUEST_CODE) {
				response = build400Response();
			} else if (status == Protocol.NOT_SUPPORTED_CODE) {
				response = build400Response();
			}
		} catch (ServerException e) {
			ErrorLogger.getInstance().error(e);
			response = build400Response();
		}

		// Means there was an error, now write the response object to the
		if (response != null) {
			try {
				response.write(outStream);
			} catch (IOException e) {
				// We will ignore this exception
				ErrorLogger.getInstance().error(e);
			}
			return;
		}

		// We reached here means no error so far, so lets process further
		// Fill in the code to create a response for version mismatch.
		// You may want to use constants such as Protocol.VERSION,
		// Protocol.NOT_SUPPORTED_CODE, and more.
		// You can check if the version matches as follows
		if (!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
			response = build400Response();
		} else {
			System.out.println(contextRootToPlugin.size());
			System.out.println(contextRootToPlugin.keySet().toString());
			// TODO keep map of already instantiated classes for efficiency
			Class<? extends IPluginRouter> router = contextRootToPlugin.get(request.getContextRoot());
			System.out.println("got context root: " + request.getContextRoot());
			System.out.println("URI: "+request.getUri());
			
			
			// Send a 404 if there is no plugin for the context root.
			if(router == null) {
				response = build404Response();
			} else {
				// Else search for a plugin that uses the context root
				
				IPluginRouter pluginRouter;
				try {
					pluginRouter = router.newInstance();
					pluginRouter.forwardRequest(request, outStream);
					return;
				} catch (InstantiationException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				response = defaultServlet.handle(request);
			}

		}

		if (response == null)
			response = build400Response();

		try {
			response.write(outStream);
			socket.close();
		} catch (IOException e) {
			// We will ignore this exception
			ErrorLogger.getInstance().error(e);
		}
	}

	private IHttpResponse build404Response() {
		HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
		responseBuilder.setStatus(Protocol.NOT_FOUND_CODE);
		responseBuilder.setPhrase(Protocol.NOT_FOUND_TEXT);
		responseBuilder.setHeaders(new HashMap<String, String>());
		responseBuilder.setConnection(Protocol.CLOSE);

		return responseBuilder.build();
	}

	private IHttpResponse build400Response() {
		HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
		responseBuilder.setStatus(Protocol.BAD_REQUEST_CODE);
		responseBuilder.setPhrase(Protocol.BAD_REQUEST_TEXT);
		responseBuilder.setHeaders(new HashMap<String, String>());
		responseBuilder.setConnection(Protocol.CLOSE);

		return responseBuilder.build();
	}
}
