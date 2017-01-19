package webserver3000;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.ConnectionHandler;
import server.Server;

public class ConnectionHandlerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		String rootDirectory = "testRoot";
		int port = 47097;
		Server server = new Server(rootDirectory, 47097);
		
//		ServerSocket serverSocket = new ServerSocket(port);
//		Socket connectionSocket = serverSocket.accept();
		
		ConnectionHandler connectionHandler = new ConnectionHandler(server, null);
//		new Thread(connectionHandler).start();
//		
//		Socket socket = new Socket(InetAddress.getLocalHost(), port);
//		
//		// We do not have any other job for this socket so just close it
//		socket.close();
//		
//		serverSocket.close();
		
		assertTrue(true);
	}

}
