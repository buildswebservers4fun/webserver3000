package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpRequest;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.ServerException;
import protocol.handler.DeleteHandler;
import protocol.handler.GetHandler;
import protocol.handler.HeadHandler;
import protocol.handler.IRequestHandler;
import protocol.handler.PostHandler;
import protocol.handler.PutHandler;
import protocol.response.GenericResponse;
import protocol.response.IHttpResponse;
import utils.AccessLogger;
import utils.ErrorLogger;

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
	private Map<String, IRequestHandler> handlers;
	
	
	public ConnectionHandler(String rootDirectory, Socket socket) {
		this.socket = socket;
		handlers = new HashMap<String, IRequestHandler>();
		
		handlers.put("DELETE", new DeleteHandler(rootDirectory));
		handlers.put("GET", new GetHandler(rootDirectory));
		handlers.put("HEAD", new HeadHandler(rootDirectory));
		handlers.put("PUT", new PutHandler(rootDirectory));
		handlers.put("POST", new PostHandler(rootDirectory));
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
				response = GenericResponse.get400(Protocol.CLOSE);
			} else if (status == Protocol.NOT_SUPPORTED_CODE) {
                response = GenericResponse.get400(Protocol.CLOSE);
            }
		} catch (ServerException e) {
			ErrorLogger.getInstance().error(e);
			response = GenericResponse.get400(Protocol.CLOSE);
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
            response = GenericResponse.get400(Protocol.CLOSE);
        } else {
            IRequestHandler handler = handlers.get(request.getMethod().toUpperCase());
            if(handler != null)
                response = handler.handle(request);
            else {
                response = GenericResponse.get400(Protocol.CLOSE);
            }
        }

		try {
			response.write(outStream);
			socket.close();
		} catch (IOException e) {
			// We will ignore this exception
			ErrorLogger.getInstance().error(e);
		}
	}
}
