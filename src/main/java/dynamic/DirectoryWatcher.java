package dynamic;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Observable;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import utils.ErrorLogger;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcher {

	private final WatchService watcher;
	private final Path basePath;
    private final String rootDirectory;
    private PluginRouter router;

	/**
	 * Creates a WatchService and registers the given directory
	 *
	 * @throws ClassNotFoundException
	 */
	public DirectoryWatcher(String watchDirectory, PluginRouter router, String rootDirectory) throws IOException, ClassNotFoundException {
		File filePath = new File(watchDirectory);

		if (!filePath.exists()) {
			filePath.mkdir();
		}

		basePath = filePath.toPath();
		this.watcher = FileSystems.getDefault().newWatchService();
		this.router = router;
		this.rootDirectory = rootDirectory;
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

		for (File file : files) {
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
		for (;;) {
			// wait for key to be signaled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path path = basePath.resolve(ev.context()).toAbsolutePath();
				// print out event
				if (path.toFile().getName().endsWith(".jar") && (kind == ENTRY_CREATE || kind == ENTRY_MODIFY)) {
					loadJar(path);
				}

				// TODO: put in a case for delete
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

			URL[] urls = { new URL("jar:file:" + jar.toAbsolutePath() + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());

			Manifest manifest = jf.getManifest();
			Closeable[] toClose = { jf, cl };

			if (manifest == null) {
				ErrorLogger.getInstance().error("Plugin has no manifest. File: " + jar);
				close(toClose);
				return;
			}

			// Get context root out of the manifest
			String contextRoot = manifest.getMainAttributes().getValue("Context-Root");
			
			if (contextRoot == null) {
				ErrorLogger.getInstance().error("Plugin has no Context-Root defined. File: " + jar);
				close(toClose);
				return;
			}

			// Get mainClass out of the manifest
			String mainClass = manifest.getMainAttributes().getValue("Main-Class");

			if (mainClass == null) {
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
			
			if(clazz == null) {
				ErrorLogger.getInstance().error("Main Class is null. File: " + jar);
				close(toClose);
				return;
			}

			Class<? extends IPluginLoader> mainClazz = (Class<? extends IPluginLoader>) clazz;
			System.out.println("new plugin: " + contextRoot);

            mainClazz.newInstance().init(router, rootDirectory);

			close(toClose);
		} catch (IOException e) {
			ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, e.toString());
		} catch (ClassNotFoundException e) {
            ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, e.toString());
		} catch (IllegalAccessException e) {
            ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, e.toString());
        } catch (InstantiationException e) {
            ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, e.toString());
        }
    }

	private void close(Closeable[] close) throws IOException {
		for (Closeable c : close) {
			c.close();
		}
	}

}
