package protocol.response;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import protocol.Protocol;

public class HeadResponse extends AFileResponse {

	public static AHttpResponse get200(File file, String connection) {
		AHttpResponse response = new HeadResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	public static AHttpResponse get404(String connection) {
		AHttpResponse response = new HeadResponse(Protocol.VERSION, Protocol.NOT_FOUND_CODE, 
				Protocol.NOT_FOUND_TEXT, new HashMap<String, String>(), null, connection);
				
		return response;
	}
	
	private HeadResponse(String version, int status, String phrase, Map<String, String> header, File file, String connection) {
		super(version, status, phrase, header, file, connection);
	}

	@Override
	public void writeBody(OutputStream out) {
		// Head has no body to write
	}
}
