package protocol.handler;

import protocol.HttpRequest;
import protocol.HttpResponse;

public interface IRequestHandler {
	HttpResponse handle(HttpRequest request);
}
