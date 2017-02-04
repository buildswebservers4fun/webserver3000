package dynamic;

import utils.ErrorLogger;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcher {

    private final WatchService watcher;
    private final Path basePath;

    /**
     * Creates a WatchService and registers the given directory
     *
     * @throws ClassNotFoundException
     */
    public DirectoryWatcher(String dir) throws IOException, ClassNotFoundException {
        File filePath = new File(dir);

        if (!filePath.exists()) {
            filePath.mkdir();
        }

        basePath = filePath.toPath();

        this.watcher = FileSystems.getDefault().newWatchService();
    }

    public void start() {
        new Thread(this::startProcessing).start();
    }

    private void startProcessing() {
        processDirectory();
        processEvents();
    }

    private void processDirectory() {
        File[] files = basePath.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        });

        for(File file: files) {
            loadJar(file.toPath());
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
        try {
            basePath.register(this.watcher, ENTRY_CREATE);
        } catch (IOException e) {
            ErrorLogger.getInstance().error("Unable to register watcher for plugin directory.", e);
            return;
        }
        for (; ; ) {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path path = basePath.resolve(ev.context()).toAbsolutePath();
                // print out event
                if (path.toFile().getName().endsWith(".jar") && kind == ENTRY_CREATE)
                    loadJar(path);
            }

            // reset key and remove from set if directory no longer accessible
            if (!key.reset()) {
                break;
            }
        }
    }

    private void loadJar(Path jar) {
        try {
            JarFile jf = new JarFile(jar.toFile());

            URL[] urls = {new URL("jar:file:" + jar.toAbsolutePath() + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            Manifest manifest = jf.getManifest();

            Closeable[] toClose = {jf,cl};

            if(manifest == null) {
                ErrorLogger.getInstance().error("Plugin has no manifest. File: " + jar);
                close(toClose);
                return;
            }

            String mainClass = manifest.getMainAttributes().getValue("Main-Class");

            if(mainClass == null) {
                ErrorLogger.getInstance().error("Plugin has no Main-Class defined. File: " + jar);
                close(toClose);
                return;
            }

            Class<?> clazz = cl.loadClass(mainClass);
            if (!IPluginLoader.class.isAssignableFrom(clazz)) {
                ErrorLogger.getInstance().error("Plugin has Main-Class of incorrect type. File: " + jar);
                close(toClose);
                return;
            }

            Class<? extends IPluginLoader> mainClazz = (Class<? extends IPluginLoader>) clazz;
            IPluginLoader servlet = mainClazz.newInstance();
            servlet.init();

            close(toClose);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void close(Closeable[] close) throws IOException {
        for (Closeable c: close) {
            c.close();
        }
    }
}
