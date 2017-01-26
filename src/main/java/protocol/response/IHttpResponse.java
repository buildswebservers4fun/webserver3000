package protocol.response;

import java.io.OutputStream;

public interface IHttpResponse {
	public void write(OutputStream outStream) throws Exception;
}
