package protocol.response;

import protocol.ServerException;

import java.io.IOException;
import java.io.OutputStream;

public interface IHttpResponse {
	void write(OutputStream outStream) throws IOException;
}
