package dynamic;

import protocol.HttpRequest;

/**
 * Created by CJ on 2/3/2017.
 */
public interface IPluginRouter {

   
    void forwardRequest(HttpRequest request);
}
