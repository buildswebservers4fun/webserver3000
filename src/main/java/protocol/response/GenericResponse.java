package protocol.response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpResponse;
import protocol.Protocol;

public class GenericResponse extends HttpResponse {

	public static HttpResponse get400(String connection) {
		HttpResponse response = new GenericResponse(Protocol.VERSION, Protocol.BAD_REQUEST_CODE, 
				Protocol.BAD_REQUEST_TEXT, new HashMap<String, String>(), null, connection);
		
		return response;
	}
	
	
	private GenericResponse(String version, int status, String phrase, Map<String, String> header, File file,
			String connection) {
		super(version, status, phrase, header, file, connection);

	}

	@Override
	public void writeBody(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
