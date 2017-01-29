package protocol.response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;

import protocol.Protocol;

public abstract class AFileResponce extends AHttpResponse {

	public AFileResponce(String version, int status, String phrase, Map<String, String> header, File file,
			String connection) {
		super(version, status, phrase, header, file, connection);

		if(file != null && file.exists()) {
			// Lets add last modified date for the file
			long timeSinceEpoch = file.lastModified();
			Date modifiedTime = new Date(timeSinceEpoch);
			this.put(Protocol.LAST_MODIFIED, modifiedTime.toString());

			// Lets get content length in bytes
			long length = file.length();
			this.put(Protocol.CONTENT_LENGTH, length + "");

			// Lets get MIME type for the file
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mime = fileNameMap.getContentTypeFor(file.getName());
			// The fileNameMap cannot find mime type for all of the documents, e.g. doc, odt, etc.
			// So we will not add this field if we cannot figure out what a mime type is for the file.
			// Let browser do this job by itself.
			if(mime != null) {
				this.put(Protocol.CONTENT_TYPE, mime);
			}
		}
	}
	
	protected void writeFile(OutputStream out) throws IOException {
		// Process text documents
		FileInputStream fileInStream = new FileInputStream(this.getFile());
		BufferedInputStream inStream = new BufferedInputStream(fileInStream, Protocol.CHUNK_LENGTH);
		
		byte[] buffer = new byte[Protocol.CHUNK_LENGTH];
		int bytesRead = 0;
		// While there is some bytes to read from file, read each chunk and send to the socket out stream
		while((bytesRead = inStream.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		// Close the file input stream, we are done reading
		inStream.close();
	}
}
