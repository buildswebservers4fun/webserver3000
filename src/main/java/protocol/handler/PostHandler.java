package protocol.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

public class PostHandler implements IRequestHandler {
	
	private String rootDirectory;

	public PostHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public HttpResponse handle(HttpRequest request) {
		String uri = request.getUri();
		File file = new File(rootDirectory, uri);

		if (file.exists()) {
			if(file.isDirectory()){
				return HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
			} else {
				// Append data to existing file
				try {
					char[] information = request.getBody();
					FileWriter writer = new FileWriter(file, true);
					writer.write(information);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return HttpResponseFactory.create200OK(file, Protocol.CLOSE);
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
				e.printStackTrace();
			}
			return HttpResponseFactory.create201Created(file, Protocol.CLOSE);
		}
	}
}