package webserver3000;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import protocol.HttpRequest;
import protocol.handler.PutHandler;
import protocol.response.GenericResponse;
import protocol.response.PutResponse;

public class PutHandlerUnitTest {

	private String rootDirectory = "./test";
	private HttpRequest request;
	private PutResponse response;
	private PutHandler handler;
	private Field uri;
	private File file;
	private File root;
	private Field body;

	@Before
	public void setUp() throws Exception {
		root = new File(rootDirectory);
		root.mkdir();
		handler = new PutHandler(rootDirectory);

		Constructor<HttpRequest> constructor;
		constructor = HttpRequest.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		request = constructor.newInstance();

		Field method = HttpRequest.class.getDeclaredField("method");
		method.setAccessible(true);
		method.set(request, "PUT");

		uri = HttpRequest.class.getDeclaredField("uri");
		uri.setAccessible(true);

		body = HttpRequest.class.getDeclaredField("body");
		body.setAccessible(true);

		file = new File(rootDirectory, "test.txt");
		file.createNewFile();

	}

	@After
	public void tearDown() throws Exception {
		file.delete();
		root.delete();
	}

	@Test
	public void testPutGives400OnPutToDirectory() throws IllegalArgumentException, IllegalAccessException {
		File newDir = new File(root, "test");
		newDir.mkdir();

		assertEquals(true, newDir.exists());
		assertEquals(true, newDir.isDirectory());

		uri.set(request, "/test");
		GenericResponse response = (GenericResponse) handler.handlePut(request);

		assertEquals(400, response.getStatus());
		newDir.delete();

	}

	@Test
	public void testPutCreatesNewFile() throws IllegalArgumentException, IllegalAccessException, IOException {
		uri.set(request, "newFile.txt");
		body.set(request, "new body".toCharArray());

		File newFile = new File(root, "newFile.txt");
		assertEquals(false, newFile.exists());

		response = (PutResponse) handler.handlePut(request);
		assertEquals(true, newFile.exists());
		assertEquals(201, response.getStatus());
		String fileName = "./test/newFile.txt";
		String content = new String(Files.readAllBytes(Paths.get(fileName)));

		assertEquals("new body", content);
		newFile.delete();

	}

	@Test
	public void testPutOverwritesExistingFile() throws IOException, IllegalArgumentException, IllegalAccessException {
		uri.set(request, "/test.txt");
		body.set(request, "overwritten".toCharArray());

		FileWriter fw = new FileWriter(file);
		fw.write("overwrite me!");
		fw.close();

		String fileName = "./test/test.txt";
		String content = new String(Files.readAllBytes(Paths.get(fileName)));
		assertEquals("overwrite me!", content);

		response = (PutResponse) handler.handlePut(request);
		assertEquals(200, response.getStatus());

		content = new String(Files.readAllBytes(Paths.get(fileName)));
		assertEquals("overwritten", content);

	}

}
