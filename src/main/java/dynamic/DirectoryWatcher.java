package dynamic;

import utils.ErrorLogger;
import utils.ExceptionUtil;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcher {

	private final WatchService watcher;
	private final Path basePath;
	private final String runtimeFolder = "runtime";
	private final Path runtimePath;
    private final String rootDirectory;
    private PluginRouter router;

    private Map<Path, LoadedJar> loadedJars;

	/**
	 * Creates a WatchService and registers the given directory
	 *
	 * @throws ClassNotFoundException
	 */
	public DirectoryWatcher(String watchDirectory, PluginRouter router, String rootDirectory) throws IOException, ClassNotFoundException {
        loadedJars = new Hashtable<>();

		File filePath = new File(watchDirectory);

		if (!filePath.exists()) {
			filePath.mkdir();
		}

		this.basePath = filePath.toPath();
		this.runtimePath = Paths.get(basePath.toString(),runtimeFolder);

		if(this.runtimePath.toFile().exists()) {
		    for(File f: this.runtimePath.toFile().listFiles()) {
		        f.delete();
            }
        } else {
            this.runtimePath.toFile().mkdirs();
        }


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
			loadJar(file.toPath().toAbsolutePath());
		}
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public void processEvents() {
		try {
			basePath.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
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
				if (path.toFile().getName().endsWith(".jar")) {
				    if(kind == ENTRY_CREATE) {
                        loadJar(path);
                    }else if (kind == ENTRY_MODIFY) {
                        LoadedJar loadedJar = loadedJars.get(path);
                        URLClassLoader cl = loadedJar.getClassLoader();
                        if(cl != null) {
                            try {
                                cl.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        loadedJar.getPluginLoader().unload(router);
                        loadJar(path);
                    } else if (kind == ENTRY_DELETE) {

				        LoadedJar loadedJar = loadedJars.get(path);
                        URLClassLoader cl = loadedJar.getClassLoader();
                        if(cl != null) {
                            try {
                                cl.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        loadedJar.getPluginLoader().unload(router);
                    }
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
        Closeable[] toClose = null;
		try {
		    Path temp = runtimePath.resolve(jar.getFileName());
            Files.copy(jar , temp, StandardCopyOption.REPLACE_EXISTING);
            Path newJar = temp;

            JarFile jf = new JarFile(newJar.toFile());

			URL[] urls = { new URL("jar:file:" + newJar.toAbsolutePath() + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());

			Manifest manifest = jf.getManifest();
            toClose= new Closeable[]{ jf };

			if (manifest == null) {
				ErrorLogger.getInstance().error("Plugin has no manifest. File: " + newJar);
                cl.close();
				return;
			}

			// Get mainClass out of the manifest
			String mainClass = manifest.getMainAttributes().getValue("Main-Class");

			if (mainClass == null) {
				ErrorLogger.getInstance().error("Plugin has no Main-Class defined. File: " + newJar);
                cl.close();
				return;
			}

			Class<?> clazz = cl.loadClass(mainClass);
			if (!IPluginLoader.class.isAssignableFrom(clazz)) {
				ErrorLogger.getInstance().error("Plugin has Main-Class of incorrect type. File: " + newJar);
                cl.close();
				return;
			}
			
			if(clazz == null) {
				ErrorLogger.getInstance().error("Main Class is null. File: " + newJar);
				cl.close();
				return;
			}

			Class<? extends IPluginLoader> mainClazz = (Class<? extends IPluginLoader>) clazz;

			IPluginLoader loader = mainClazz.newInstance();
            loader.init(router, rootDirectory);
            loadedJars.put(jar, new LoadedJar(cl, loader));
		} catch (IOException e) {
			ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, ExceptionUtil.exceptionToString(e));
			System.out.println(e.toString());
            System.out.println(ExceptionUtil.exceptionToString(e));
		} catch (ClassNotFoundException e) {
            System.out.println(e.toString());
		    ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, ExceptionUtil.exceptionToString(e));
            System.out.println(ExceptionUtil.exceptionToString(e));
		} catch (IllegalAccessException e) {
            ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, ExceptionUtil.exceptionToString(e));
            System.out.println(e.toString());
            System.out.println(ExceptionUtil.exceptionToString(e));
        } catch (InstantiationException e) {
            ErrorLogger.getInstance().error("Error while trying to load plugin: " + jar, ExceptionUtil.exceptionToString(e));
            System.out.println(e.toString());
            System.out.println(ExceptionUtil.exceptionToString(e));
        } finally {
		    if(toClose != null)
                try {
                    close(toClose);
                } catch (IOException e) {
                    ErrorLogger.getInstance().error("Error while closing jar reader: " + jar, e.toString());
                }
        }
    }

	private void close(Closeable[] close) throws IOException {
		for (Closeable c : close) {
			c.close();
		}
	}

}
