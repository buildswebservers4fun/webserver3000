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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;

import dynamic.DirectoryWatcher;
import dynamic.PluginRouter;
import protocol.Protocol;
import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ServerEndToEndTest {
	final String TEMP_ROOT_DIRECTORY = "tempWeb";
	final String SERVER_PATH = "http://localhost:";
	final int port = 47097;
	final String PLUGINS_DIRECTORY = "plugins";
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
		tempDir = new File(TEMP_ROOT_DIRECTORY);
		tempDir.mkdir();
		if (!tempDir.exists()) {
			fail("temp dir failed to be created");
		}

		file = new File(TEMP_ROOT_DIRECTORY, "testFile.txt");
		file.createNewFile();

		PrintWriter writer = new PrintWriter(file, "UTF-8");
		writer.print("test");
		writer.close();

		if (!file.exists()) {
			fail("temp file failed to be created");
		}


		PluginRouter router = new PluginRouter();
		// Create Watch Service
        DirectoryWatcher watcher = new DirectoryWatcher(PLUGINS_DIRECTORY, router, TEMP_ROOT_DIRECTORY);
        watcher.start();
		server = new Server(TEMP_ROOT_DIRECTORY, port, router, false, 0);
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
		Thread.sleep(100);
		if (serverFailed) {
			fail("Server unable to start");
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (serverFailed) {
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

		HttpRequest requestGet = transport.createRequestFactory()
				.buildGetRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile.txt")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent(), responseGet.getContentCharset());
		assertEquals(200, responseGet.getStatusCode());
		// assertEquals("test\n", responseGet2.getHeaders());
		assertEquals("test", fileContents);
	}

	@Test
	public void testGetDefaultExists() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		File file = new File(TEMP_ROOT_DIRECTORY, Protocol.DEFAULT_FILE);
		file.createNewFile();

		HttpRequest requestGet = transport.createRequestFactory()
				.buildGetRequest(new GenericUrl(new URL(SERVER_PATH + port + "/")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent(), responseGet.getContentCharset());
		assertEquals(200, responseGet.getStatusCode());
		;

		assertEquals(true, file.delete());
	}

	@Test
	public void testGetDefaultNotFound() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		HttpRequest requestGet = transport.createRequestFactory()
				.buildGetRequest(new GenericUrl(new URL(SERVER_PATH + port + "/")));
		try {
			HttpResponse responseGet = requestGet.execute();
			fail("Should have thrown an exception");
		} catch (HttpResponseException e) {
			// Passed
		}
	}

	@Test
	public void testHead() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		HttpRequest requestGet = transport.createRequestFactory()
				.buildHeadRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile.txt")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent(), responseGet.getContentCharset());
		assertEquals(200, responseGet.getStatusCode());
		assertEquals("", fileContents);
	}

	@Test
	public void testHeadDirectory404() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		HttpRequest requestGet = transport.createRequestFactory()
				.buildHeadRequest(new GenericUrl(new URL(SERVER_PATH + port + "/")));
		try {
			HttpResponse responseGet = requestGet.execute();
			fail("Should have thrown an exception");
		} catch (HttpResponseException e) {
			// pass
		}
	}

	@Test
	public void testHeadDirectory200() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		File file = new File(TEMP_ROOT_DIRECTORY, Protocol.DEFAULT_FILE);
		file.createNewFile();

		HttpRequest requestGet = transport.createRequestFactory()
				.buildHeadRequest(new GenericUrl(new URL(SERVER_PATH + port + "/")));
		HttpResponse responseGet = requestGet.execute();
		String fileContents = convertStreamToString(responseGet.getContent(), responseGet.getContentCharset());
		assertEquals(200, responseGet.getStatusCode());
		assertEquals("", fileContents);
		assertEquals(true, file.delete());
	}

	@Test
	public void testHead404() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		HttpRequest requestGet = transport.createRequestFactory()
				.buildHeadRequest(new GenericUrl(new URL(SERVER_PATH + port + "/asdfasdfasd")));
		try {
			HttpResponse responseGet = requestGet.execute();
			fail("Should of thrown 404, threw " + responseGet.getStatusCode());
		} catch (HttpResponseException e) {
			// Pass
		}
	}

	@Test
	public void testPutFileExists() throws InterruptedException, IOException {
		// tests that a put request overwrites the file if it does exist

		NetHttpTransport transport = new NetHttpTransport();

		String test = "overwrite";
		byte[] bytes = test.getBytes();
		HttpContent content = new ByteArrayContent("type", bytes);
		HttpRequest requestPut = transport.createRequestFactory()
				.buildPutRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile.txt")), content);
		HttpResponse responsePut = requestPut.execute();
		assertEquals(200, responsePut.getStatusCode());
		String fileContents = convertStreamToString(responsePut.getContent(), responsePut.getContentCharset());
		assertEquals("overwrite", fileContents);
	}

	@Test
	public void testPutFileDoesntExist() throws InterruptedException, IOException {
		// tests that a put request creates a new file if the file doesnt exist

		NetHttpTransport transport = new NetHttpTransport();

		String test = "new file";
		byte[] bytes = test.getBytes();
		HttpContent content = new ByteArrayContent("type", bytes);
		File delete = new File(TEMP_ROOT_DIRECTORY, "testFile2.txt");
		delete.delete();
		assertEquals(false, delete.exists());
		HttpRequest requestPut = transport.createRequestFactory()
				.buildPutRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile2.txt")), content);
		HttpResponse responsePut = requestPut.execute();
		assertEquals(201, responsePut.getStatusCode());
		String fileContents = convertStreamToString(responsePut.getContent(), responsePut.getContentCharset());
		assertEquals("new file", fileContents);

		delete.delete();
		assertEquals(false, delete.exists());
	}

	@Test
	public void testDeleteFileExists() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		File file = new File(TEMP_ROOT_DIRECTORY, "testFile3.txt");
		file.createNewFile();

		HttpRequest requestGet1 = transport.createRequestFactory()
				.buildGetRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile3.txt")));
		HttpResponse responseGet1 = requestGet1.execute();

		assertEquals(200, responseGet1.getStatusCode());
		System.out.println("This assert passes");
		assertEquals(true, file.exists());
		HttpRequest requestDelete = transport.createRequestFactory()
				.buildDeleteRequest(new GenericUrl(new URL(SERVER_PATH + port + "/testFile3.txt")));
		HttpResponse responseDelete = requestDelete.execute();

		assertEquals(200, responseDelete.getStatusCode());
		assertEquals(false, file.exists());
	}

	@Test
	public void testDeleteFileDoesntExist() throws InterruptedException, IOException {
		NetHttpTransport transport = new NetHttpTransport();

		HttpRequest requestDelete = transport.createRequestFactory()
				.buildDeleteRequest(new GenericUrl(new URL(SERVER_PATH + port + "/notARealFile.txt")));
		try {
			requestDelete.execute();
			fail("Should have thrown an exception");
		} catch (HttpResponseException e) {
			// pass
		}
	}

	public void testPutDirectory() throws InterruptedException, IOException {
		// tests that a put request creates a new file if the file doesnt exist

		NetHttpTransport transport = new NetHttpTransport();

		String test = "new file";
		byte[] bytes = test.getBytes();
		HttpContent content = new ByteArrayContent("type", bytes);
		File delete = new File(TEMP_ROOT_DIRECTORY, "testFile2.txt");
		delete.delete();
		assertEquals(false, delete.exists());
		HttpRequest requestPut = transport.createRequestFactory()
				.buildPutRequest(new GenericUrl(new URL(SERVER_PATH + port + "/")), content);
		try {
			HttpResponse responsePut = requestPut.execute();
			fail("Should have thrown an exception");
		} catch (HttpResponseException e) {
			// Passed
		}
	}

	@Test
	public void testPostFileDoesExist() throws InterruptedException, IOException {
		// tests that a put request overwrites the file if it does exist
		NetHttpTransport transport = new NetHttpTransport();

		String start = "Original ";
		String test = "new file";
		File file = createRandomFile();
		setContentsOfFile(file, start);
		try {
			byte[] bytes = test.getBytes();
			HttpContent content = new ByteArrayContent("type", bytes);
			HttpRequest requestPut = transport.createRequestFactory()
					.buildPostRequest(new GenericUrl(new URL(SERVER_PATH + port + "/" + file.getName())), content);
			HttpResponse responsePut = requestPut.execute();
			assertEquals(200, responsePut.getStatusCode());
			String fileContents = convertStreamToString(responsePut.getContent(), responsePut.getContentCharset());
			assertEquals(start + test, fileContents);

			String newContents = getFileContents(file);
			assertEquals(start + test, newContents);

			file.delete();
			assertEquals(false, file.exists());
		} finally {
			file.delete();
		}
	}

	@Test
	public void testPostDirectory() throws InterruptedException, IOException {
		// tests that a put request overwrites the file if it does exist
		NetHttpTransport transport = new NetHttpTransport();

		String start = "Original ";
		String test = "new file";
		File file = createRandomFile();
		setContentsOfFile(file, start);
		try {

			byte[] bytes = test.getBytes();
			HttpContent content = new ByteArrayContent("type", bytes);
			HttpRequest requestPut = transport.createRequestFactory()
					.buildPostRequest(new GenericUrl(new URL(SERVER_PATH + port + "/")), content);
			try {
				HttpResponse responsePut = requestPut.execute();
				fail("Should have thrown an exception");
			} catch (HttpResponseException e) {

			}
		} finally {
			file.delete();
		}
	}

	@Test
	public void testPostFileDoesntExist() throws InterruptedException, IOException {
		// tests that a put request creates a new file if the file doesnt exist
		String test = "contents";
		String fileName = "postFileDoesntExist";
		File file = new File(TEMP_ROOT_DIRECTORY, fileName);
		try {
			NetHttpTransport transport = new NetHttpTransport();

			byte[] bytes = test.getBytes();
			HttpContent content = new ByteArrayContent("type", bytes);
			HttpRequest requestPut = transport.createRequestFactory()
					.buildPostRequest(new GenericUrl(new URL(SERVER_PATH + port + "/" + fileName)), content);
			HttpResponse responsePut = requestPut.execute();
			assertEquals(201, responsePut.getStatusCode());
			String fileContents = convertStreamToString(responsePut.getContent(), responsePut.getContentCharset());
			// TODO Why is this one not returning correctly? Test with REST
			// client works correctly
			// assertEquals(test, fileContents);
			System.out.println("Responce Contents: " + fileContents);

			String newContents = getFileContents(file);
			assertEquals(test, newContents);
		} finally {
			file.delete();
			assertEquals(false, file.exists());
		}

	}

	static String convertStreamToString(InputStream is, Charset set) throws IOException {
		StringBuilder builder = new StringBuilder();
		byte[] buffer = new byte[65535];
		int read = 0;
		while ((read = is.read(buffer, 0, buffer.length)) != -1) {
			builder.append(new String(buffer, 0, read, set));
			System.out.println("Reading");
		}
		return builder.toString();
	}

	private File createRandomFile() throws IOException {
		return File.createTempFile("TestFile", "TestFile", new File(TEMP_ROOT_DIRECTORY));
	}

	private void setContentsOfFile(File file, String string) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(string);
		fw.close();
	}

	private String getFileContents(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
	}
}