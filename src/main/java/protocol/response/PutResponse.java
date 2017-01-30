package protocol.response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import protocol.Protocol;

public class PutResponse extends AFileResponse {

	public static AHttpResponse get200(File file, String connection) {
		AHttpResponse response = new PutResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	public static AHttpResponse get201(File file, String connection) {
		AHttpResponse response = new PutResponse(Protocol.VERSION, Protocol.CREATED_CODE, 
				Protocol.CREATED_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	private PutResponse(String version, int status, String phrase, Map<String, String> header, File file, String connection) {
		super(version, status, phrase, header, file, connection);
	}

	@Override
	public void writeBody(OutputStream out) throws IOException {
		// We are reading a file
		if ((this.getStatus() == Protocol.OK_CODE || this.getStatus() == Protocol.CREATED_CODE) && getFile() != null) {
			writeFile(out);
		}
	}

}
