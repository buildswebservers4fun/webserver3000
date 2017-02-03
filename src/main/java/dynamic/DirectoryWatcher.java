package dynamic;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.time.chrono.IsoChronology;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcher {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Path basePath;
    private boolean trace = false;
    private static final String CLASS_SUFFIX = ".class";

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     *
     * @throws ClassNotFoundException
     */
    private void register(Path dir) throws IOException, ClassNotFoundException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

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
        this.keys = new HashMap<WatchKey, Path>();
        register(basePath);

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
        for (; ; ) {
            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path path = basePath.resolve(ev.context()).toAbsolutePath();
                // print out event
                System.out.format("%s: %s\n", event.kind().name(), path);
                if (path.toFile().getName().endsWith(".jar") && kind == ENTRY_CREATE)
                    loadJar(path);
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void loadJar(Path jar) {
        try {
            System.out.println("In jar class loading stuff");
            JarFile jf = new JarFile(jar.toFile());

            URL[] urls = {new URL("jar:file:" + jar.toAbsolutePath() + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

//            Enumeration<JarEntry> entries = jf.entries();
//            while (entries.hasMoreElements()) {
//                JarEntry element = entries.nextElement();
//                String filename = element.getName();
////							System.out.println(filename);
//                if (filename.endsWith(".MF")) {
//                    // Manifest
//                    Manifest manifest = jf.getManifest();
//                    Attributes attr = manifest.getMainAttributes();
//                    Set<Object> keys = attr.keySet();
//                    for (Object o : keys) {
//                        System.out.println("Key: " + o + " -- Value: " + attr.get(o));
//                    }
//                }
//                if (filename.endsWith(CLASS_SUFFIX)) {
//                    filename = element.getName().substring(0, element.getName().length() - 6);
//                    filename = filename.replace('/', '.');
//                    try {
//                        Class c = cl.loadClass(filename);
////									System.out.println("Class object: " + c);
//                    } catch (NoClassDefFoundError e) {
//                        System.out.println(e.getMessage());
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
            String mainClass = jf.getManifest().getMainAttributes().getValue("Main-Class");
            String contextRoot = jf.getManifest().getMainAttributes().getValue("contextRoot");

            System.out.println(mainClass);
            System.out.println(contextRoot);
            // TODO: Use the following Methods to init servlet
//            Class<? extends IServlet> mainClazz = (Class<? extends IServlet>) cl.loadClass(mainClass);
//            IServlet servlet = mainClazz.getConstructor(String.class).newInstance("ROOTDIRECTORY");

            jf.close();
            cl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
