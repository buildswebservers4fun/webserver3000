package dynamic.handler;

import protocol.HttpRequest;
import protocol.response.IHttpResponse;

/**
 * Created by CJ on 1/29/2017.
 */
public interface IPutHandler {
    IHttpResponse handlePut(HttpRequest request);
}
