package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class ImfsPath implements Path {

    private URI uri;
    private FileSystem fileSystem;
    private String materializedPath;

    public ImfsPath(FileSystem fileSystem, URI uri) {
        if (!uri.isAbsolute()) {
            // TODO: don't support relative URIs yet
            // see branch fix-mkdirs-and-relative-paths
            throw new UnsupportedOperationException("Unimplemented relative URI: " + uri.toString());
        }

        var path = uri.getPath();
        this.materializedPath = path.substring(1);
        this.fileSystem = fileSystem;
        this.uri = uri;
    }

    @Override
    public Path resolve(String relative) {
        if (relative.endsWith("/")) {
            relative = relative.substring(0, relative.length() - 1);
        }
        URI relUri = URI.create(relative);
        URI resolved;
        if (relUri.isAbsolute()) {
            // we don't expect absolute URIs yet
            // they might use a different instance of the fileSystem
            throw new UnsupportedOperationException("Unimplemented absolute URI resolve");
        }
        if (relative.startsWith("/") || this.uri.getPath().equals("/")) {
            // if either are root, then we can just resolve
            resolved = this.uri.resolve(relative);
        } else {
            // otherwise we need to append the relative to the path
            // FS Path is different than URI in this respect.
            // explicitly use resolveSibling to replace the last path component
            resolved = this.uri.resolve(this.uri.getPath() + "/" + relative);
        }
        return new ImfsPath(fileSystem, resolved);
    }

    @Override
    public int compareTo(Path arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }

    @Override
    public boolean endsWith(Path arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endsWith'");
    }

    @Override
    public Path getFileName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFileName'");
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public Path getName(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public int getNameCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNameCount'");
    }

    @Override
    public Path getParent() {
        String path = uri.getPath();
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        URI parentUri = uri.resolve(parentPath);
        return new ImfsPath(fileSystem, parentUri);
    }

    @Override
    public Path getRoot() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRoot'");
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public Path normalize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'normalize'");
    }

    @Override
    public WatchKey register(WatchService arg0, Kind<?>[] arg1, Modifier... arg2) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public Path relativize(Path arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'relativize'");
    }

    @Override
    public Path resolve(Path arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resolve'");
    }

    @Override
    public boolean startsWith(Path arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startsWith'");
    }

    @Override
    public Path subpath(int arg0, int arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subpath'");
    }

    @Override
    public Path toAbsolutePath() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toAbsolutePath'");
    }

    @Override
    public Path toRealPath(LinkOption... arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toRealPath'");
    }

    @Override
    public URI toUri() {
        return this.uri;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof ImfsPath
                && this.uri.equals(((ImfsPath) other).uri);
    }

    @Override
    public String toString() {
        return this.uri.toString();
    }

    public String getMaterializedPath() {
        return materializedPath;
    }
}
