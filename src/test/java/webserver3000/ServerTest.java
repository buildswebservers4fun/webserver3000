/*
 * server.java
 * Jan 18, 2017
 *
 * Copyright (C) 2015 Chandan Raj Rupakheti
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

package webserver3000;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ServerTest {
	static Server server;
	static Thread runner;
	final String tempRootDirectory = "tempWeb";
	final String SERVER_PATH = "http://localhost:";
	final int port = 8080;
	File tempDir;
	File file;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// make temporary directory and file
		tempDir = new File(System.getProperty("user.dir") + "\\" + tempRootDirectory);
		tempDir.mkdir();
		
	    file = new File(System.getProperty("user.dir") + "\\" + tempRootDirectory + "\\testFile.txt");
	    file.createNewFile();
	    
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        writer.print("test");
        writer.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		file.delete();
		tempDir.delete();
	}

	@Test
	public void test() throws InterruptedException, IOException {
		// Create a run the server
		Server server = new Server(tempRootDirectory, port);
		Thread runner = new Thread(server);
		runner.start();

		NetHttpTransport transport = new NetHttpTransport();
		
		HttpRequest requestGet = transport.createRequestFactory().buildGetRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile.txt")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent());
		assertEquals(responseGet.getStatusCode(), 200);
//		assertEquals("test\n", responseGet2.getHeaders());
		assertEquals("test", fileContents);
		
		// Wait for the server thread to terminate
		server.stop();
		runner.join();
	}
	
	static String convertStreamToString(java.io.InputStream is) {
	    @SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}