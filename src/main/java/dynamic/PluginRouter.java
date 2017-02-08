package dynamic;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by CJ on 2/6/2017.
 */
public class PluginRouter {

    Map<Path, IServlet> routes;

    public PluginRouter() {
        routes = new Hashtable<Path, IServlet>();
    }

    public void addRoute(Path directory, IServlet servlet) {
        routes.put(directory,servlet);
    }

    public IServlet getRoute(Path directory) {
        do {
            IServlet servlet = routes.get(directory);
            if(servlet == null) {
                directory = directory.getParent();
            } else {
                return servlet;
            }
        } while(directory != null);

        return null;
    }

    public void removeRoute(Path directory) {
        routes.remove(directory);
    }
}
