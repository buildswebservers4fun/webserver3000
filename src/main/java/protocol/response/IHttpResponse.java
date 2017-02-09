package protocol.response;

import protocol.ServerException;

import java.io.IOException;
import java.io.OutputStream;

public interface IHttpResponse extends Comparable<IHttpResponse> {
	void write(OutputStream outStream) throws IOException;
	int getStatus();
    int getSize();
}
