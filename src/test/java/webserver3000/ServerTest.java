package webserver3000;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Server;

public class ServerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRootAndPort() throws InterruptedException {
		String rootDirectory = "testRoot";
		int port = 47097;
		Server server = new Server(rootDirectory, 47097, null, false, 0);

		assertEquals(rootDirectory, server.getRootDirectory());
		assertEquals(port, server.getPort());
	}

}
