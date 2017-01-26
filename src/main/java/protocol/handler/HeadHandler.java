package protocol.handler;

import java.io.File;

import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.HeadResponse;
import protocol.response.IHttpResponse;

public class HeadHandler implements IRequestHandler {

	private String rootDirectory;
	
	public HeadHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	@Override
	public IHttpResponse handle(HttpRequest request) {
		IHttpResponse response;
		// Map<String, String> header = request.getHeader();
		// String date = header.get("if-modified-since");
		// String hostName = header.get("host");
		//
		// Handling GET request here
		// Get relative URI path from request
		String uri = request.getUri();
		// Get root directory path from server
		// Combine them together to form absolute file path
		File file = new File(rootDirectory + uri);
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				// Look for default index.html file in a directory
				String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
				file = new File(location);
				if (file.exists()) {
					// Lets create 200 OK response
					response = HeadResponse.get200(file, Protocol.CLOSE);
				} else {
					// File does not exist so lets create 404 file not found
					// code
					response = HeadResponse.get404(Protocol.CLOSE);
				}
			} else { // Its a file
						// Lets create 200 OK response
				response = HeadResponse.get200(file, Protocol.CLOSE);
			}
		} else {
			// File does not exist so lets create 404 file not found code
			response = HeadResponse.get404(Protocol.CLOSE);
		}
		return response;
	}

}
