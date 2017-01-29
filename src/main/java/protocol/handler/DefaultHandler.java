package protocol.handler;

import dynamic.IServlet;
import dynamic.ServletBuilder;

/**
 * Created by CJ on 1/29/2017.
 */
public class DefaultHandler {

   public static IServlet createDefaultHandler(String rootDirectory) {
       return new ServletBuilder()
               .setGetHandler(new GetHandler(rootDirectory))
               .setHeadHandler(new HeadHandler(rootDirectory))
               .setPostHandler(new PostHandler(rootDirectory))
               .setPutHandler(new PutHandler(rootDirectory))
               .setDeleteHandler(new DeleteHandler(rootDirectory))
               .build();
   }
}
