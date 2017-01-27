package protocol.handler;

import java.io.File;
import java.io.IOException;

import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.DeleteResponse;
import protocol.response.IHttpResponse;
import utils.AccessLogger;

public class DeleteHandler implements IRequestHandler {

	private String rootDirectory;
	
	public DeleteHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	@Override
	public IHttpResponse handle(HttpRequest request) {
		
		String uri = request.getUri();
		
		File file = new File(rootDirectory + uri);
		
		//File to delete exists
		if(file.exists()) {
			file.delete();
			return DeleteResponse.get200(file, Protocol.CLOSE);
		} else {
			AccessLogger.getInstance().info("File does not exists! Delete Handler Invoked!");
			return DeleteResponse.get404(Protocol.CLOSE);
		}
		
	}

}
