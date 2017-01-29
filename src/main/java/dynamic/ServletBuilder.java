package dynamic;

import dynamic.handler.*;
import protocol.HttpRequest;
import protocol.response.IHttpResponse;

/**
 * Created by CJ on 1/29/2017.
 */
public class ServletBuilder {

    private IGetHandler getHandler;
    private IPutHandler putHandler;
    private IHeadHandler headHandler;
    private IPostHandler postHandler;
    private IDeleteHandler deleteHandler;

    public ServletBuilder setGetHandler(IGetHandler getHandler) {
        this.getHandler = getHandler;
        return this;
    }

    public ServletBuilder setHeadHandler(IHeadHandler getHandler) {
        this.headHandler = getHandler;
        return this;
    }

    public ServletBuilder setPostHandler(IPostHandler getHandler) {
        this.postHandler = getHandler;
        return this;
    }

    public ServletBuilder setPutHandler(IPutHandler getHandler) {
        this.putHandler = getHandler;
        return this;
    }

    public ServletBuilder setDeleteHandler(IDeleteHandler getHandler) {
        this.deleteHandler = getHandler;
        return this;
    }

    public IServlet build() {
        return new IServlet() {
            @Override
            public IHttpResponse handleGet(HttpRequest request) {
                return getHandler.handleGet(request);
            }

            @Override
            public IHttpResponse handleHead(HttpRequest request) {
                return headHandler.handleHead(request);
            }

            @Override
            public IHttpResponse handlePost(HttpRequest request) {
                return postHandler.handlePost(request);
            }

            @Override
            public IHttpResponse handlePut(HttpRequest request) {
                return putHandler.handlePut(request);
            }

            @Override
            public IHttpResponse handleDelete(HttpRequest request) {
                return deleteHandler.handleDelete(request);
            }
        };
    }
}
