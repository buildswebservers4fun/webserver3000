package dynamic;

import java.io.OutputStream;
import protocol.HttpRequest;

/**
 * Created by CJ on 2/3/2017.
 */
public interface IPluginRouter {

	void forwardRequest(HttpRequest request, OutputStream outStream);
}
