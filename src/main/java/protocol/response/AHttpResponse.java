/*
 * HttpResponse.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 */
 
package protocol.response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import protocol.Protocol;

/**
 * Represents a response object for HTTP.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
abstract class AHttpResponse implements IHttpResponse {
	private String version;
	private int status;
	private String phrase;
	private Map<String, String> header;
	private File file;
	
	/**
	 * Constructs a HttpResponse object using supplied parameter
	 * 
	 * @param version The http version.
	 * @param status The response status.
	 * @param phrase The response status phrase.
	 * @param header The header field map.
	 * @param file The file to be sent.
	 */
	public AHttpResponse(String version, int status, String phrase, Map<String, String> header, File file, String connection) {
		this.version = version;
		this.status = status;
		this.phrase = phrase;
		this.header = header;
		this.file = file;
		
		this.header.put(Protocol.CONNECTION, connection);
		
		// Lets add current date
		Date date = Calendar.getInstance().getTime();
		this.header.put(Protocol.DATE, date.toString());
		
		// Lets add server info
		this.header.put(Protocol.Server, Protocol.getServerInfo());
	
		// Lets add extra header with provider info
		this.header.put(Protocol.PROVIDER, Protocol.AUTHOR);
	}

	/**
	 * Gets the version of the HTTP.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the status code of the response object.
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Gets the status phrase of the response object.
	 * 
	 * @return the phrase
	 */
	public String getPhrase() {
		return phrase;
	}
	
	/**
	 * The file to be sent.
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the header fields associated with the response object.
	 * @return the header
	 */
	public Map<String, String> getHeader() {
		// Lets return the unmodifable view of the header map
		return Collections.unmodifiableMap(header);
	}

	/**
	 * Maps a key to value in the header map.
	 * @param key A key, e.g. "Host"
	 * @param value A value, e.g. "www.rose-hulman.edu"
	 */
	public void put(String key, String value) {
		this.header.put(key, value);
	}
	
	/**
	 * Writes the data of the http response object to the output stream.
	 * 
	 * @param outStream The output stream
	 * @throws Exception
	 */
	public void write(OutputStream outStream) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outStream, Protocol.CHUNK_LENGTH);

		// First status line
		String line = this.version + Protocol.SPACE + this.status + Protocol.SPACE + this.phrase + Protocol.CRLF;
		out.write(line.getBytes());
		
		// Write header fields if there is something to write in header field
		if(header != null) {
			for(Map.Entry<String, String> entry : header.entrySet()) {
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
	
	public abstract void writeBody(OutputStream out) throws IOException;		
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("----------------------------------\n");
		buffer.append(this.version);
		buffer.append(Protocol.SPACE);
		buffer.append(this.status);
		buffer.append(Protocol.SPACE);
		buffer.append(this.phrase);
		buffer.append(Protocol.LF);
		
		for(Map.Entry<String, String> entry : this.header.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append(Protocol.SEPERATOR);
			buffer.append(Protocol.SPACE);
			buffer.append(entry.getValue());
			buffer.append(Protocol.LF);
		}
		
		buffer.append(Protocol.LF);
		if(file != null) {
			buffer.append("Data: ");
			buffer.append(this.file.getAbsolutePath());
		}
		buffer.append("\n----------------------------------\n");
		return buffer.toString();
	}
	
}
