package protocol.response;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface IHttpResponse extends Comparable<IHttpResponse> {
	void write(OutputStream outStream) throws IOException;
	int getStatus();
    int getSize();
	Map<String, String> getHeaders();
}
