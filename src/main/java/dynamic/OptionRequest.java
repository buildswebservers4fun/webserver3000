package dynamic;

import protocol.HttpRequest;
import protocol.Protocol;
import protocol.response.HttpResponseBuilder;
import protocol.response.IHttpResponse;

import java.util.HashMap;

/**
 * Created by CJ on 2/16/2017.
 */
public class OptionRequest {

    public static IHttpResponse handle(HttpRequest request) {
        HttpResponseBuilder responseBuilder = new HttpResponseBuilder();
        responseBuilder.setStatus(Protocol.OK_CODE);
        responseBuilder.setPhrase(Protocol.OK_TEXT);
        HashMap header = new HashMap<String, String>();
        header.put("Access-Control-Allow-Origin", "*");
        responseBuilder.setHeaders(header);
        responseBuilder.setConnection(Protocol.CLOSE);

        return responseBuilder.build();
    }
}
