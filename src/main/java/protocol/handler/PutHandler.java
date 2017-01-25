package protocol.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

public class PutHandler implements IRequestHandler {

	private String rootDirectory;

	public PutHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public HttpResponse handle(HttpRequest request) {
		HttpResponse response;

		String uri = request.getUri();
		boolean exists = false;

		File file = new File(rootDirectory, uri);

		if (file.exists()) {
			if(file.isDirectory()){
				return HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
			}
			exists = true;
		}
		
		try {
			char[] information = request.getBody();
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(information);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (exists){
			return HttpResponseFactory.create200OK(file, Protocol.CLOSE);
		}
		return HttpResponseFactory.create201Created(file, Protocol.CLOSE);
	}

}
