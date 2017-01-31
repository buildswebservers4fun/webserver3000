package webserver3000;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import protocol.HttpRequest;
import protocol.handler.HeadHandler;
import protocol.response.IHttpResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class HeadHandlerUnitTest {

	private String rootDirectory = "./test";
	private HttpRequest request;
	private IHttpResponse response;
	private HeadHandler handler;
	private Field uri;
	private File file;
	private File root;

	@Before
	public void setUp() throws Exception {
		root = new File(rootDirectory);
		root.mkdir();
		handler = new HeadHandler(rootDirectory);

		Constructor<HttpRequest> constructor;
		constructor = HttpRequest.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		request = constructor.newInstance();

		Field method = HttpRequest.class.getDeclaredField("method");
		method.setAccessible(true);
		method.set(request, "HEAD");

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
	public void headRequestGives200OnExistingFile() throws IllegalArgumentException, IllegalAccessException {
		uri.set(request, "/test.txt");
		response = handler.handleHead(request);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void headRequestGives200OnDefaultDirectory()
			throws IOException, IllegalArgumentException, IllegalAccessException {
		File defaultFile = new File(root, "index.html");
		defaultFile.createNewFile();
		uri.set(request, "/");
		response = handler.handleHead(request);
		assertEquals(200, response.getStatus());
		defaultFile.delete();

	}

	@Test
	public void headRequestGives404OnDirIfNoIndex() throws IllegalArgumentException, IllegalAccessException {
		uri.set(request, "/");
		response = handler.handleHead(request);
		assertEquals(404, response.getStatus());
	}

	@Test
	public void headRequestGives404OnFileNotFound() throws IllegalArgumentException, IllegalAccessException {
		uri.set(request, "doesntExist.txt");

		response = handler.handleHead(request);
		assertEquals(404, response.getStatus());
	}

}
