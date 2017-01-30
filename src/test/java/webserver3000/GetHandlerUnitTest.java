package webserver3000;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import protocol.HttpRequest;
import protocol.handler.GetHandler;
import protocol.response.GetResponse;
import protocol.response.IHttpResponse;
import server.Server;

public class GetHandlerUnitTest {

	private String rootDirectory = "./test";
	private HttpRequest request;
	private GetResponse response;
	private GetHandler handler;
	private Field uri;
	private File file;
	private File root;

	@Before
	public void setUp() throws Exception {
		root = new File(rootDirectory);
		root.mkdir();
		handler = new GetHandler(rootDirectory);

		Constructor<HttpRequest> constructor;
		constructor = HttpRequest.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		request = constructor.newInstance();

		Field method = HttpRequest.class.getDeclaredField("method");
		method.setAccessible(true);
		method.set(request, "GET");

		uri = HttpRequest.class.getDeclaredField("uri");
		uri.setAccessible(true);

		file = new File(rootDirectory, "test.txt");
		file.createNewFile();

	}

	@After
	public void tearDown() throws Exception {
		file.delete();
		root.delete();
	}

	@Test
	public void testHandleGetDirectoryNoDefault() throws InterruptedException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, IOException {
		File newDir = new File(root, "test/");
		newDir.mkdir();

		uri.set(request, "test/");
		response = (GetResponse) handler.handleGet(request);

		assertEquals(404, response.getStatus());

		Field field = HttpRequest.class.getDeclaredField("method");
		field.setAccessible(true);
		assertEquals("GET", field.get(request));
		newDir.delete();
	}

	@Test
	public void testHandleGetDirectoryWithDefault() throws InterruptedException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, IOException {
		File newDir = new File(root, "test");
		newDir.mkdir();

		File index = new File(newDir, "index.html");
		index.createNewFile();

		uri.set(request, "/test");
		response = (GetResponse) handler.handleGet(request);

		assertEquals(200, response.getStatus());

		newDir.delete();
		index.delete();
	}

	@Test
	public void testHandleGetFile() throws InterruptedException, IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException, SecurityException {
		uri.set(request, "test.txt");
		response = (GetResponse) handler.handleGet(request);
		assertEquals("test.txt", uri.get(request));

		assertEquals(200, response.getStatus());
	}

	@Test
	public void testHandleGet404() throws InterruptedException, IllegalArgumentException, IllegalAccessException {
		uri.set(request, "nonexistent.txt");
		response = (GetResponse) handler.handleGet(request);
		assertEquals(404, response.getStatus());

	}

}
