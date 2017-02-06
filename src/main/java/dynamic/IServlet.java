package dynamic;

import java.io.IOException;
import java.io.OutputStream;

import dynamic.handler.IDeleteHandler;
import dynamic.handler.IGetHandler;
import dynamic.handler.IHeadHandler;
import dynamic.handler.IPostHandler;
import dynamic.handler.IPutHandler;
import protocol.HttpRequest;

/**
 * Created by CJ on 1/29/2017.
 */
public interface IServlet extends IGetHandler, IHeadHandler, IPostHandler, IPutHandler, IDeleteHandler {
    default void handle(HttpRequest request, OutputStream outStream) throws IOException {
        switch(request.getMethod().toUpperCase()) {
            case "GET":
                handleGet(request).write(outStream);
                break;
            case "HEAD":
                handleHead(request).write(outStream);
                break;
            case "PUT":
                handlePut(request).write(outStream);
                break;
            case "POST":
                handlePost(request).write(outStream);
                break;
            case "DELETE":
                handleDelete(request).write(outStream);
                break;
        }
    }
}
