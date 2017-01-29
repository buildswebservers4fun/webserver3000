package dynamic.handler;

import protocol.HttpRequest;
import protocol.response.IHttpResponse;

/**
 * Created by CJ on 1/29/2017.
 */
public interface IHeadHandler {
    IHttpResponse handleHead(HttpRequest request);
}
