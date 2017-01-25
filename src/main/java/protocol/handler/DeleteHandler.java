package protocol.handler;

import java.io.File;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

public class DeleteHandler implements IRequestHandler {

	private String rootDirectory;
	
	public DeleteHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	@Override
	public HttpResponse handle(HttpRequest request) {
		HttpResponse response;
		
		String uri = request.getUri();
		
		File file = new File(rootDirectory + uri);
		
		//File to delete exists
		if(file.exists()) {
			file.delete();
			return HttpResponseFactory.create200OK(file, Protocol.CLOSE);
			
			
		} else {
			return HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
		}
		
	}

}
