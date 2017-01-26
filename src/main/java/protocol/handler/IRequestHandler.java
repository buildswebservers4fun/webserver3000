package protocol.handler;

import protocol.HttpRequest;
import protocol.response.IHttpResponse;

public interface IRequestHandler {
	IHttpResponse handle(HttpRequest request);
}
