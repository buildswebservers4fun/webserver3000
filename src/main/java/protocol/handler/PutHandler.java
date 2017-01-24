package protocol.handler;

import protocol.HttpRequest;
import protocol.HttpResponse;

public class PutHandler implements IRequestHandler {
	
	private String rootDirectory;

	public PutHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public HttpResponse handle(HttpRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
