package protocol.response;

import protocol.ServerException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface IHttpResponse {
	void write(OutputStream outStream) throws IOException;
	int getStatus();
	Map<String, String> getHeaders();
}
