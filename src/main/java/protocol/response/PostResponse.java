package protocol.response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpResponse;
import protocol.Protocol;

public class PostResponse extends HttpResponse {
	
	public static HttpResponse get200(File file, String connection) {
		HttpResponse response = new PostResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file, connection);
		
		return response;
	}
	
	public static HttpResponse get404(String connection) {
		HttpResponse response = new PostResponse(Protocol.VERSION, Protocol.NOT_FOUND_CODE, 
				Protocol.NOT_FOUND_TEXT, new HashMap<String, String>(), null, connection);
				
		return response;
	}

	private PostResponse(String version, int status, String phrase, Map<String, String> header, File file,
			String connection) {
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
		// TODO Auto-generated method stub
	}

}
