package me.fy2ne.muto.engine;

import me.fy2ne.muto.MutoLog;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;

public final class MutoClassLoader extends URLClassLoader {
    private final List<Path> jarPaths;
    private final Set<String> modIds;
    private volatile boolean closed;

    public MutoClassLoader(List<Path> jars, Set<String> modIds, ClassLoader parent) {
        super(toUrls(jars), parent);
        this.jarPaths = new ArrayList<>(jars);
        this.modIds = new HashSet<>(modIds);
        this.closed = false;
        MutoLog.info("muto child loader spawned with {} jars", jars.size());
    }

    private static URL[] toUrls(List<Path> paths) {
        URL[] urls = new URL[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            try {
                urls[i] = paths.get(i).toUri().toURL();
            } catch (Exception e) {
                MutoLog.error("bad jar path: {}", paths.get(i), e);
            }
        }
        return urls;
    }

    public Set<String> modIds() {
        return Collections.unmodifiableSet(modIds);
    }

    public List<Path> jarPaths() {
        return Collections.unmodifiableList(jarPaths);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            super.close();
            MutoLog.info("muto child loader closed ({} jars freed)", jarPaths.size());
        }
    }
}
