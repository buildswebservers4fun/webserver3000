package protocol.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.GenericResponse;
import protocol.response.IHttpResponse;
import protocol.response.PostResponse;

public class PostHandler implements IRequestHandler {
	
	private String rootDirectory;

	public PostHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public IHttpResponse handle(HttpRequest request) {
		String uri = request.getUri();
		File file = new File(rootDirectory, uri);

		if (file.exists()) {
			if(file.isDirectory()){
				return GenericResponse.get400(Protocol.CLOSE);
			} else {
				// Append data to existing file
				try {
					char[] information = request.getBody();
					FileWriter writer = new FileWriter(file, true);
					writer.write(information);
					writer.close();
				} catch (IOException e) {
					return GenericResponse.get400(Protocol.CLOSE);
				}
				return PostResponse.get200(file, Protocol.CLOSE);
			}
		} else {
			// Create parent directories and new file
			try {
				file.getParentFile().mkdirs();
				char[] information = request.getBody();
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write(information);
				writer.close();
			} catch (IOException e) {
				return GenericResponse.get400(Protocol.CLOSE);
			}
			return PostResponse.get201(file, Protocol.CLOSE);
		}
	}
}