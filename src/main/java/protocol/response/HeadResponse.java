package protocol.response;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

import protocol.HttpResponse;

public class HeadResponse extends HttpResponse {

	public HeadResponse(String version, int status, String phrase, Map<String, String> header, File file, String connection) {
		super(version, status, phrase, header, file, connection);
	}

	@Override
	public void writeBody(OutputStream out) {
		// Head has no body to write
	}
}
