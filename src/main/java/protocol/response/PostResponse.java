package protocol.response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import protocol.Protocol;

public class PostResponse extends AFileResponce {
	
	public static IHttpResponse get200(File file, String connection) {
		IHttpResponse response = new PostResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	public static IHttpResponse get201(File file, String connection) {
		IHttpResponse response = new PostResponse(Protocol.VERSION, Protocol.CREATED_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	public static IHttpResponse get404(String connection) {
		IHttpResponse response = new PostResponse(Protocol.VERSION, Protocol.NOT_FOUND_CODE, 
				Protocol.NOT_FOUND_TEXT, new HashMap<String, String>(), null, connection);
				
		return response;
	}

	private PostResponse(String version, int status, String phrase, Map<String, String> header, File file,
			String connection) {
		super(version, status, phrase, header, file, connection);
	}

	@Override
	public void writeBody(OutputStream out) throws IOException {
		if(this.getStatus() == Protocol.OK_CODE && this.getFile() != null) {
			writeFile(out);
		}
	}
}