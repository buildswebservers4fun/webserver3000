package protocol.response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import protocol.Protocol;

public class HttpResponseBuilder {
	private String version = Protocol.VERSION;
	private int status;
	private String phrase;
	private Map<String, String> headers;
	private File file;
	private String connection;
	private boolean doWriteFile = false;
	private boolean writeBody;
	private String bodyText;

	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public void setConnection(String connection) {
		this.connection = connection;
	}
	
	public void setFileBody(File file) {
		this.doWriteFile = true;
		this.file = file;
		
		if(file != null && file.exists()) {
			// Lets add last modified date for the file
			long timeSinceEpoch = file.lastModified();
			Date modifiedTime = new Date(timeSinceEpoch);
			this.headers.put(Protocol.LAST_MODIFIED, modifiedTime.toString());

			// Lets get content length in bytes
			long length = file.length();
			this.headers.put(Protocol.CONTENT_LENGTH, length + "");

			// Lets get MIME type for the file
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mime = fileNameMap.getContentTypeFor(file.getName());
			// The fileNameMap cannot find mime type for all of the documents, e.g. doc, odt, etc.
			// So we will not add this field if we cannot figure out what a mime type is for the file.
			// Let browser do this job by itself.
			if(mime != null) {
				this.headers.put(Protocol.CONTENT_TYPE, mime);
			}
		}
	}
	
	public void setFileName(File file) {
		this.doWriteFile = false;
		this.file = file;
	}
	
	public void setBody(String contents) {
		this.bodyText = contents;
		this.writeBody = true;
	}
	
	public IHttpResponse build() {
		headers.put(Protocol.CONNECTION, connection);
		
		// Lets add current date
		Date date = Calendar.getInstance().getTime();
		headers.put(Protocol.DATE, date.toString());
		
		// Lets add server info
		headers.put(Protocol.Server, Protocol.getServerInfo());
	
		// Lets add extra header with provider info
		headers.put(Protocol.PROVIDER, Protocol.AUTHOR);
		
		headers.put(Protocol.TIME_RECEIVED, Long.toString(System.currentTimeMillis()));
		
		return new IHttpResponse() {

			@Override
			public void write(OutputStream outStream) throws IOException {
				// TODO Auto-generated method stub
				BufferedOutputStream out = new BufferedOutputStream(outStream, Protocol.CHUNK_LENGTH);

				// First status line
				String line = version + Protocol.SPACE + status + Protocol.SPACE + phrase + Protocol.CRLF;
				out.write(line.getBytes());
				
				// Write header fields if there is something to write in header field
				if(headers != null) {
					for(Map.Entry<String, String> entry : headers.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						
						// Write each header field line
						line = key + Protocol.SEPERATOR + Protocol.SPACE + value + Protocol.CRLF;
						out.write(line.getBytes());
					}
				}

				// Write a blank line
				out.write(Protocol.CRLF.getBytes());

				writeBody(out);
				
				// Flush the data so that outStream sends everything through the socket 
				out.flush();
			}

			@Override
			public int getStatus() {
				return status;
			}
			
			@Override
			public Map<String, String> getHeaders() {
				return headers;
			}

			private void writeBody(OutputStream out) throws IOException {
				// We are reading a file
				if(doWriteFile && (status == Protocol.OK_CODE || status == Protocol.CREATED_CODE) && file != null) {
					writeFile(out);
				}
				if(writeBody) {
					out.write(bodyText.getBytes());
				}
			}
			
			private void writeFile(OutputStream out) throws IOException {
				// Process text documents
				FileInputStream fileInStream = new FileInputStream(file);
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
			
			@Override
			public String toString() {
				StringBuffer buffer = new StringBuffer();
				buffer.append("----------------------------------\n");
				buffer.append(version);
				buffer.append(Protocol.SPACE);
				buffer.append(status);
				buffer.append(Protocol.SPACE);
				buffer.append(phrase);
				buffer.append(Protocol.LF);
				
				for(Map.Entry<String, String> entry : headers.entrySet()) {
					buffer.append(entry.getKey());
					buffer.append(Protocol.SEPERATOR);
					buffer.append(Protocol.SPACE);
					buffer.append(entry.getValue());
					buffer.append(Protocol.LF);
				}
				
				buffer.append(Protocol.LF);
				if(file != null) {
					buffer.append("Data: ");
					buffer.append(file.getAbsolutePath());
				}
				buffer.append("\n----------------------------------\n");
				return buffer.toString();
			}

            @Override
            public int compareTo(IHttpResponse o) {
                if(this.getSize() > o.getSize()) {
                    return 1;
                } else if(this.getSize() > o.getSize()) {
                    return -1;
                } else {
                    return 0;
                }
            }
            
            public int getSize() {
                Serializable ser = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos;
                try {
                    oos = new ObjectOutputStream(baos);
                    oos.writeObject(ser);
                    oos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return baos.size();
            }
		};
	}
}