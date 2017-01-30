package webserver3000;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import protocol.HttpRequest;
import protocol.handler.PostHandler;
import protocol.response.GenericResponse;
import protocol.response.PostResponse;

public class PostHandlerUnitTest {
	
	private String rootDirectory = "./test";
	private HttpRequest request;
	private PostResponse response;
	private PostHandler handler;
	private Field uri;
	private File file;
	private File root;
	private Field body;
	
	@Before
	public void setUp() throws Exception {
		root = new File(rootDirectory);
		root.mkdir();
		handler = new PostHandler(rootDirectory);
		
		Constructor<HttpRequest> constructor;
		constructor = HttpRequest.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		request = constructor.newInstance();

		Field method = HttpRequest.class.getDeclaredField("method");
		method.setAccessible(true);
		method.set(request, "POST");
		
		uri = HttpRequest.class.getDeclaredField("uri");
		uri.setAccessible(true);
		
		body = HttpRequest.class.getDeclaredField("body");
		body.setAccessible(true);
		
		file = new File(rootDirectory,"test.txt");
		file.createNewFile();
		
	}

	@After
	public void tearDown() throws Exception {
		file.delete();
		root.delete();
	}
	
	@Test
	public void testPostToDirectoryBreaks() throws IllegalArgumentException, IllegalAccessException{
		File newDir = new File(root, "test/");
		newDir.mkdir();
		
		uri.set(request, "test/");
		GenericResponse response = (GenericResponse) handler.handlePost(request);
		
		assertEquals(400, response.getStatus());
		newDir.delete();
		
	}
	
	@Test
	public void testPostToExistingFileReturns200() throws IllegalArgumentException, IllegalAccessException{
		uri.set(request, "test.txt");
		
		assertEquals(true, file.exists());
		
		response = (PostResponse) handler.handlePost(request);
		
		assertEquals(200, response.getStatus());
		assertEquals(file, response.getFile());
		
	}
	
	@Test
	public void testPostToNewFileReturns201() throws IllegalArgumentException, IllegalAccessException{
		uri.set(request, "newFile.txt");
		
		File newFile = new File(root, "newFile.txt");
		newFile.delete();
		assertEquals(false,newFile.exists());
		
		response = (PostResponse) handler.handlePost(request);
		assertEquals(201, response.getStatus());
		assertEquals(true, newFile.exists());
		newFile.delete();
		
	}

	@Test
	public void testPostToExistingFileAppends() throws IllegalArgumentException, IllegalAccessException, IOException{
		uri.set(request, "test.txt");
		body.set(request, "this is a post body".toCharArray());
		
		assertEquals(true, file.exists());
		
		FileWriter fw = new FileWriter(file);
		fw.write("initial stuff");
		fw.close();
		
		String fileName = "./test/test.txt";
		
		String content = new String(Files.readAllBytes(Paths.get(fileName)));
		
		assertEquals("initial stuff",content);
		
		response = (PostResponse) handler.handlePost(request);
		
		assertEquals(200, response.getStatus());
		assertEquals(file, response.getFile());
		
		content = new String(Files.readAllBytes(Paths.get(fileName)));
		assertEquals("initial stuff"+"this is a post body", content);
		
	}
	
	@Test
	public void testPostToNewFileHasData() throws IllegalArgumentException, IllegalAccessException, IOException{
		uri.set(request, "newFile.txt");
		body.set(request, "this is a post body".toCharArray());
		
		File newFile = new File(root, "newFile.txt");
		newFile.delete();
		assertEquals(false, newFile.exists());
		
		String fileName = "./test/newFile.txt";
		
		response = (PostResponse) handler.handlePost(request);
		
		assertEquals(201, response.getStatus());
		assertEquals(newFile, response.getFile());
		
		String content = new String(Files.readAllBytes(Paths.get(fileName)));
		assertEquals("this is a post body", content);
		newFile.delete();
	}
	
	@Test
	public void testPostCreatesNestedDirectories() throws IllegalArgumentException, IllegalAccessException{
		uri.set(request, "test/test2/test3/test.txt");
		
		File testDir = new File(root, "test");
		File test2Dir = new File(testDir, "test2");
		File test3Dir = new File(test2Dir, "test3");
		File testFile = new File(test3Dir, "test.txt");

		assertEquals(false, testDir.exists());
		assertEquals(false, test2Dir.exists());
		assertEquals(false, test3Dir.exists());
		assertEquals(false, testFile.exists());
		
		response = (PostResponse) handler.handlePost(request);
		
		assertEquals(201, response.getStatus());
		
		assertEquals(true, testDir.exists());
		assertEquals(true, test2Dir.exists());
		assertEquals(true, test3Dir.exists());
		assertEquals(true, testDir.isDirectory());
		assertEquals(true, test2Dir.isDirectory());
		assertEquals(true, test3Dir.isDirectory());
		
		
		assertEquals(true, testFile.exists());
		assertEquals(true, testFile.isFile());

		testFile.delete();
		test3Dir.delete();
		test2Dir.delete();
		testDir.delete();
		
		
	}
	

}
