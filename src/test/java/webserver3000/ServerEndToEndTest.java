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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ServerEndToEndTest {
	final String tempRootDirectory = "./tempWeb";
	final String SERVER_PATH = "http://localhost:";
	final int port = 47097;
	File tempDir;
	File file;
	
	Server server;
	Thread runner;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// make temporary directory and file
		tempDir = new File(tempRootDirectory);
		tempDir.mkdir();
		if(!tempDir.exists()) {
			throw new Exception("temp dir failed to be created");
		}
		
	    file = new File(tempRootDirectory, "testFile.txt");
	    file.createNewFile();
	    
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        writer.print("test");
        writer.close();
        
		if(!file.exists()) {
			throw new Exception("temp file failed to be created");
		}
		
		server = new Server(tempRootDirectory, port);
		runner = new Thread(new Runnable() {
			
			@Override
			public void run() {
				server.start();
			}
		});
		runner.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				serverFailed = true;
			}
		});
		runner.start();
		Thread.sleep(1000);
		if(serverFailed) {
			fail("Server unable to start");
		}
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if(serverFailed) {
			fail("There was an uncaught exception on the server");
		}
		
		// Wait for the server thread to terminate
		server.stop();
		runner.join();
		
		
		file.delete();
		tempDir.delete();
	}

	private boolean serverFailed;
	
	@Test
	public void testGet() throws InterruptedException, IOException {	
		NetHttpTransport transport = new NetHttpTransport();
		
		HttpRequest requestGet = transport.createRequestFactory().buildGetRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile.txt")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent());
		assertEquals(responseGet.getStatusCode(), 200);
//		assertEquals("test\n", responseGet2.getHeaders());
		assertEquals("test", fileContents);
	}
	
	@Test
	public void testHead() throws InterruptedException, IOException {	
		NetHttpTransport transport = new NetHttpTransport();
		
		HttpRequest requestGet = transport.createRequestFactory().buildHeadRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile.txt")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent());
		assertEquals(responseGet.getStatusCode(), 200);
		assertEquals("", fileContents);
	}
	
	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = null;
		try {
			s = new java.util.Scanner(is);
			s.useDelimiter("\\A");
		    return s.hasNext() ? s.next() : "";
		} finally {
			s.close();
		}
	}
}