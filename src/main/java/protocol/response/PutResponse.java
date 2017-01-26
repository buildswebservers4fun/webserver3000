package protocol.response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpResponse;
import protocol.Protocol;

public class PutResponse extends HttpResponse {

	public static HttpResponse get200(File file, String connection) {
		HttpResponse response = new PutResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	public static HttpResponse get201(File file, String connection) {
		HttpResponse response = new PutResponse(Protocol.VERSION, Protocol.CREATED_CODE, 
				Protocol.CREATED_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	private PutResponse(String version, int status, String phrase, Map<String, String> header, File file, String connection) {
		super(version, status, phrase, header, file, connection);
		
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
