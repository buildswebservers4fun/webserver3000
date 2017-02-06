package dynamic;

import java.io.IOException;
import java.io.OutputStream;

import dynamic.handler.*;
import protocol.HttpRequest;
import protocol.response.IHttpResponse;

/**
 * Created by CJ on 1/29/2017.
 */
public interface IServlet extends IGetHandler, IHeadHandler, IPostHandler, IPutHandler, IDeleteHandler {
    default IHttpResponse handle(HttpRequest request) {
        switch(request.getMethod().toUpperCase()) {
            case "GET":
                return handleGet(request);
            case "HEAD":
                return handleHead(request);
            case "PUT":
                return handlePut(request);
            case "POST":
                return handlePost(request);
            case "DELETE":
                return handleDelete(request);
            default:
                return null;
        }
    }
    
    default void handle(HttpRequest request, OutputStream outStream) throws IOException {
        switch(request.getMethod().toUpperCase()) {
            case "GET":
                handleGet(request).write(outStream);
            case "HEAD":
            	handleGet(request).write(outStream);
            case "PUT":
            	handleGet(request).write(outStream);
            case "POST":
            	handleGet(request).write(outStream);
            case "DELETE":
            	handleGet(request).write(outStream);
        }
    }
}
