package protocol.response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import protocol.Protocol;

public class DeleteResponse extends AFileResponce {

	public DeleteResponse(String version, int status, String phrase, Map<String, String> header, File file,
			String connection) {
		super(version, status, phrase, header, file, connection);
	}

	public static AHttpResponse get200(File file, String connection) {
		AHttpResponse response = new DeleteResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), null, connection);
		
		return response;
	}
	
	public static AHttpResponse get404(String connection) {
		AHttpResponse response = new DeleteResponse(Protocol.VERSION, Protocol.NOT_FOUND_CODE, 
				Protocol.NOT_FOUND_TEXT, new HashMap<String, String>(), null, connection);
				
		return response;
	}

	
	@Override
	public void writeBody(OutputStream out) throws IOException {
		if(this.getStatus() == Protocol.OK_CODE && this.getFile() != null) {
			writeFile(out);
		}
	}

}
