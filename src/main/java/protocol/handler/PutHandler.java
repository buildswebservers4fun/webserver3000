package protocol.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import dynamic.handler.IPutHandler;
import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.GenericResponse;
import protocol.response.IHttpResponse;
import protocol.response.PutResponse;

public class PutHandler implements IPutHandler {

	private String rootDirectory;

	public PutHandler(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public IHttpResponse handlePut(HttpRequest request) {
		String uri = request.getUri();
		boolean exists = false;

		File file = new File(rootDirectory, uri);

		if (file.exists()) {
			if(file.isDirectory()){
				return GenericResponse.get400(Protocol.CLOSE);
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
			// TODO Make this server error
			return GenericResponse.get400(Protocol.CLOSE);
		}

		if (exists){
			return PutResponse.get200(file, Protocol.CLOSE);
		}
		return PutResponse.get201(file, Protocol.CLOSE);
	}

}
