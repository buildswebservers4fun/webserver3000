package protocol.handler;

import java.io.File;

import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.DeleteResponse;
import protocol.response.IHttpResponse;

public class DeleteHandler implements IRequestHandler {

	private String rootDirectory;
	
	public DeleteHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	@Override
	public IHttpResponse handle(HttpRequest request) {
		IHttpResponse response;
		
		String uri = request.getUri();
		
		File file = new File(rootDirectory + uri);
		
		//File to delete exists
		if(file.exists()) {
			file.delete();
			return DeleteResponse.get200(file, Protocol.CLOSE);
			
			
		} else {
			return DeleteResponse.get404(Protocol.CLOSE);
		}
		
	}

}
