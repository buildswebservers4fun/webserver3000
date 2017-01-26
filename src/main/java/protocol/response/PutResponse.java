package protocol.response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import protocol.HttpResponse;
import protocol.Protocol;

public class PutResponse extends HttpResponse {

	public PutResponse(String version, int status, String phrase, Map<String, String> header, File file, String connection) {
		super(version, status, phrase, header, file, connection);
	}

	@Override
	public void writeBody(OutputStream out) throws IOException {
		// We are reading a file
		if ((this.getStatus() == Protocol.OK_CODE || this.getStatus() == Protocol.CREATED_CODE) && getFile() != null) {
			// Process text documents
			FileInputStream fileInStream = new FileInputStream(getFile());
			BufferedInputStream inStream = new BufferedInputStream(fileInStream, Protocol.CHUNK_LENGTH);

			byte[] buffer = new byte[Protocol.CHUNK_LENGTH];
			int bytesRead = 0;
			// While there is some bytes to read from file, read each chunk and
			// send to the socket out stream
			while ((bytesRead = inStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			// Close the file input stream, we are done reading
			inStream.close();
		}
	}

}
