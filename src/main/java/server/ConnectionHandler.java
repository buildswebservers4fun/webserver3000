package server;

import dynamic.IServlet;
import dynamic.PluginRouter;
import protocol.HttpRequest;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.ServerException;
import protocol.response.HttpResponseBuilder;
import protocol.response.IHttpResponse;
import utils.AccessLogger;
import utils.ErrorLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class is responsible for handling a incoming request by creating a
 * {@link HttpRequest} object and sending the appropriate response be creating a
 * {@link IHttpResponse} object. It implements {@link Runnable} to be used in
 * multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
    private ResponseWriter responseWriter;
	private long cacheTimeLimit = 10000;
	private final PluginRouter router;
	private final boolean isCacheEnabled;
	private Socket socket;

	private static Map<String, CachedItem<IHttpResponse>> CachedResponses;

	static {
		CachedResponses = new Hashtable<>();
	}

	public ConnectionHandler(Socket socket, PluginRouter router, ResponseWriter responseWriter, boolean cacheEnable, long cacheTimeLimit) {
		this.socket = socket;
		this.router = router;
		this.isCacheEnabled = cacheEnable;
		this.cacheTimeLimit = cacheTimeLimit;
        this.responseWriter = responseWriter;
	}

    /**
     * The entry point for connection handler. It first parses incoming request
     * and creates a {@link HttpRequest} object, then it creates an appropriate
     * {@link IHttpResponse} object and sends the response back to the client
     * (web browser).
     */
    public void run() {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            inStream = this.socket.getInputStream();
            outStream = this.socket.getOutputStream();
        } catch (IOException e) {
            ErrorLogger.getInstance().error(e);
            return;
        }
        HttpRequest request = loadRequest(inStream, outStream, responseWriter);
        IHttpResponse response = processRequestAndGenerateResponse(request);

        responseWriter.addToQueue(response, socket);
    }

    private HttpRequest loadRequest(InputStream inStream, OutputStream outStream, ResponseWriter responseWriter) {
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

        if (!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
            response = build400Response();
        }

        // Means there was an error, now write the response object to the
        if (response != null) {
            responseWriter.addToQueue(response, socket);
            return null;
        }

        return request;
    }


    private IHttpResponse processRequestAndGenerateResponse(HttpRequest request) {
        IHttpResponse response = null;

        boolean cacheHit = false;
        if(isCacheEnabled && request.getMethod().toUpperCase().equals("GET") && CachedResponses.containsKey(request.getUri())) {
            CachedItem<IHttpResponse> cachedItem = CachedResponses.get(request.getUri());

            if(cachedItem.getCreatedTime() + cacheTimeLimit > System.currentTimeMillis()) {
                response = cachedItem.getItem();
                cacheHit = true;
                AccessLogger.getInstance().info("Cache Hit on: " + request.getUri());
            }
            else {
                CachedResponses.remove(request.getUri());
            }
        }
        if (!cacheHit){
            IServlet servlet = router.getRoute(Paths.get(request.getUri()));
            if(servlet != null) {
                response = servlet.handle(request);
            }
            if(isCacheEnabled && request.getMethod().toUpperCase().equals("GET") && response != null) {
                CachedResponses.put(request.getUri(), new CachedItem<>(System.currentTimeMillis(), response));
                AccessLogger.getInstance().info("Cache Miss on: " + request.getUri());
            }
        }


        if (response == null)
            response = build400Response();
        else if(isCacheEnabled) {
            switch(request.getMethod().toUpperCase()) {
                case "DELETE":
                case "PUT":
                case "POST":
                    CachedResponses.remove(request.getUri());
                    break;
            }
        }

        return response;
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
