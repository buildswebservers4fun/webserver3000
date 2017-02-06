package dynamic;

import protocol.HttpRequest;

/**
 * Created by CJ on 2/3/2017.
 */
public interface IPluginRouter {

    /**
     * Registers all servlets for this plugin with the server.
     * TODO: Add arugments to allow fo registering
     */
    void init();
    
    void forwardRequest(HttpRequest request);
}
