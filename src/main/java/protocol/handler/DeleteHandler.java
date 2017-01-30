package protocol.handler;

import java.io.File;
import java.util.HashMap;

import dynamic.handler.IDeleteHandler;
import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.HttpResponseBuilder;
import protocol.response.IHttpResponse;
import utils.AccessLogger;

public class DeleteHandler implements IDeleteHandler {

	private String rootDirectory;
	
	public DeleteHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	@Override
	public IHttpResponse handleDelete(HttpRequest request) {
		
		String uri = request.getUri();
		
		File file = new File(rootDirectory + uri);
		
		//File to delete exists
		if(file.exists()) {
			file.delete();
			return build200Response(file);
		} else {
			AccessLogger.getInstance().info("File does not exists! Delete Handler Invoked!");
			return build404Response();
		}
		
	}
	
	private IHttpResponse build200Response(File file) {
		HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
		responseBuilder.setStatus(Protocol.OK_CODE);
		responseBuilder.setPhrase(Protocol.OK_TEXT);
		responseBuilder.setHeaders(new HashMap<String, String>());
		responseBuilder.setFileName(file);
		responseBuilder.setConnection(Protocol.CLOSE);
		return responseBuilder.build();
	}
	
	private IHttpResponse build404Response() {
		HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
		responseBuilder.setStatus(Protocol.NOT_FOUND_CODE);
		responseBuilder.setPhrase(Protocol.NOT_FOUND_TEXT);
		responseBuilder.setHeaders(new HashMap<String, String>());
		responseBuilder.setConnection(Protocol.CLOSE);
		return responseBuilder.build();
	}

}
