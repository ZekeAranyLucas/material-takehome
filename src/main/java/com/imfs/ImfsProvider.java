package com.imfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Iterator;

public class ImfsProvider extends FileSystemProvider {
    static final String IMFS_SCHEME = "imfs";
    private static final Map<String, ImfsFileSystem> cache = new HashMap<>();

    private static ImfsPath checkPath(Path obj) {
        Objects.requireNonNull(obj);
        if (!(obj instanceof ImfsPath)) {
            throw new ProviderMismatchException();
        }
        return (ImfsPath) obj;
    }

    @Override
    public void checkAccess(Path path, AccessMode... arg1) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        if (!kid.isEmpty() && !fileSystem.contains(kid)) {
            throw new NoSuchFileException("No such file or directory: " + imfsPath.toUri().toString());
        }
    }

    @Override
    public void copy(Path arg0, Path arg1, CopyOption... arg2) throws IOException {
        var src = checkPath(arg0);
        var dst = checkPath(arg1);
        checkAccess(src);
        checkAccess(dst.getParent());
        var fileSystem = (ImfsFileSystem) src.getFileSystem();
        var srcKid = src.getMaterializedPath();
        var dstKid = dst.getMaterializedPath();
        if (fileSystem.contains(dstKid)) {
            throw new FileAlreadyExistsException("File at path:" + dst.toUri() + " already exists");
        }
        fileSystem.addEntry(dstKid);
        var blob = fileSystem.getBlob(srcKid);
        if (blob != null) {
            fileSystem.putBlob(dstKid, blob);
        }
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... arg1) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        fileSystem.addEntry(kid);
    }

    @Override
    public void delete(Path path) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        fileSystem.removeEntry(kid);
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
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        this.copy(source, target, options);
        this.delete(source);

        // var src = checkPath(arg0);
        // var dst = checkPath(arg1);
        // var fileSystem = (ImfsFileSystem) src.getFileSystem();
        // var srcKid = src.getMaterializedPath();
        // var dstKid = dst.getMaterializedPath();
        // fileSystem.addEntry(dstKid);
        // fileSystem.putBlob(dstKid, fileSystem.getBlob(srcKid));
        // fileSystem.removeEntry(srcKid);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path arg0, Set<? extends OpenOption> options, FileAttribute<?>... arg2)
            throws IOException {
        var imfsPath = checkPath(arg0);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        var exists = fileSystem.contains(kid);
        if (options.contains(StandardOpenOption.WRITE)) {
            if (exists && options.contains(StandardOpenOption.CREATE_NEW)) {
                throw new FileAlreadyExistsException("File at path:" + imfsPath.toUri() + " already exists");
            }
            fileSystem.addEntry(kid);
            fileSystem.putBlob(kid, new byte[] {});
            var result = new ImfsWritableByteChannel(imfsPath);
            return new ImfsSeekableByteChannel(result);
        } else if (options.contains(StandardOpenOption.READ) || options.isEmpty()) {
            return new ByteArraySeekableByteChannel(fileSystem.getBlob(kid));
        }
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("only READ and WRITE are implemented in 'newByteChannel'");
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> arg1) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
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
    public <A extends BasicFileAttributes> A readAttributes(Path arg0, Class<A> classz, LinkOption... arg2)
            throws IOException {
        var imfsPath = checkPath(arg0);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        if (classz.equals(BasicFileAttributes.class)) {
            if (fileSystem.contains(imfsPath.getMaterializedPath())) {
                return (A) new ImfsFileAttributes(imfsPath);
            }
            throw new FileNotFoundException("No such file or directory: " + imfsPath.toUri().toString());
        } else {
            throw new UnsupportedOperationException("cannot read attributes of type: " + classz);
        }

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
