package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

public class ImfsProvider extends FileSystemProvider {
    static final String IMFS_SCHEME = "imfs";
    private static final Map<String, ImfsFileSystem> cache = new HashMap<>();

    @Override
    public void checkAccess(Path arg0, AccessMode... arg1) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkAccess'");
    }

    @Override
    public void copy(Path arg0, Path arg1, CopyOption... arg2) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... arg1) throws IOException {
        var fileSystem = (ImfsFileSystem) path.getFileSystem();
        var kid = path.toUri().getPath();
        fileSystem.addEntry(kid);
    }

    @Override
    public void delete(Path arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path arg0, Class<V> arg1, LinkOption... arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFileAttributeView'");
    }

    @Override
    public FileStore getFileStore(Path arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFileStore'");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return cache.computeIfAbsent(uri.getHost(), (key) -> {
            return new ImfsFileSystem(this, key);
        });
    }

    @Override
    public Path getPath(URI uri) {
        return new ImfsPath(getFileSystem(uri), uri);
    }

    @Override
    public String getScheme() {
        return IMFS_SCHEME;
    }

    @Override
    public boolean isHidden(Path arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isHidden'");
    }

    @Override
    public boolean isSameFile(Path arg0, Path arg1) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSameFile'");
    }

    @Override
    public void move(Path arg0, Path arg1, CopyOption... arg2) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    @Override
    public SeekableByteChannel newByteChannel(Path arg0, Set<? extends OpenOption> arg1, FileAttribute<?>... arg2)
            throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newByteChannel'");
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> arg1) throws IOException {
        var fileSystem = (ImfsFileSystem) path.getFileSystem();
        var stream = fileSystem.streamAllPaths()
                .filter(each -> each.getParent().equals(path))
                .filter(arg0 -> {
                    try {
                        return arg1.accept(arg0);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return false;
                    }
                });
        return new DirectoryStream<Path>() {
            @Override
            public Iterator<Path> iterator() {
                return stream.iterator();
            }

            @Override
            public void close() throws IOException {
                stream.close();
            }
        };
    }

    @Override
    public FileSystem newFileSystem(URI arg0, Map<String, ?> arg1) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newFileSystem'");
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path arg0, Class<A> arg1, LinkOption... arg2)
            throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAttributes'");
    }

    @Override
    public Map<String, Object> readAttributes(Path arg0, String arg1, LinkOption... arg2) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAttributes'");
    }

    @Override
    public void setAttribute(Path arg0, String arg1, Object arg2, LinkOption... arg3) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setAttribute'");
    }

}
