package webserver3000;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import protocol.HttpRequest;
import protocol.handler.DeleteHandler;
import protocol.handler.GetHandler;
import protocol.response.DeleteResponse;
import protocol.response.GetResponse;

public class DeleteHandlerUnitTest {

	private String rootDirectory = "./test";
	private HttpRequest request;
	private DeleteResponse response;
	private DeleteHandler handler;
	private Field uri;
	private File file;
	private File root;

	@Before
	public void setUp() throws Exception {
		root = new File(rootDirectory);
		root.mkdir();
		handler = new DeleteHandler(rootDirectory);

		Constructor<HttpRequest> constructor;
		constructor = HttpRequest.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		request = constructor.newInstance();

		Field method = HttpRequest.class.getDeclaredField("method");
		method.setAccessible(true);
		method.set(request, "DELETE");

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
	public void testDeleteFileReturns200() throws IllegalArgumentException, IllegalAccessException {
		uri.set(request, "/test.txt");

		assertEquals(true, file.exists());
		response = (DeleteResponse) handler.handleDelete(request);
		assertEquals(200, response.getStatus());
		assertEquals(false, file.exists());

	}

	@Test
	public void testDeleteDirectory() throws IllegalArgumentException, IllegalAccessException {
		File testDir = new File(root, "testDir");
		testDir.mkdir();

		assertEquals(true, testDir.exists());
		assertEquals(true, testDir.isDirectory());

		uri.set(request, "/testDir");
		response = (DeleteResponse) handler.handleDelete(request);

		assertEquals(200, response.getStatus());
		assertEquals(false, testDir.exists());

	}

	@Test
	public void testDeletingNonExistentFileReturns404() throws IllegalArgumentException, IllegalAccessException {
		uri.set(request, "/nonexistent.txt");
		assertEquals(false, new File(root, "nonexistent.txt").exists());
		response = (DeleteResponse) handler.handleDelete(request);
		assertEquals(404, response.getStatus());
	}

}
