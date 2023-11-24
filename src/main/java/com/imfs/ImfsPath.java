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

    public ImfsPath(FileSystem fileSystem, URI uri) {
        this.fileSystem = fileSystem;
        this.uri = uri;
    }

    @Override
    public Path resolve(String relative) {
        URI result = this.uri.resolve(relative);
        // TODO: what if relative is not actually relative?
        return new ImfsPath(fileSystem, result);
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
}
