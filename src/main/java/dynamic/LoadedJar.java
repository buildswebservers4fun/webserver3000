package dynamic;

import java.net.URLClassLoader;

/**
 * Created by CJ on 2/8/2017.
 */
public class LoadedJar {

    private URLClassLoader classLoader;
    private IPluginLoader loader;

    public LoadedJar(URLClassLoader classLoader, IPluginLoader loader) {
        this.classLoader = classLoader;
        this.loader = loader;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public IPluginLoader getPluginLoader() {
        return loader;
    }

}