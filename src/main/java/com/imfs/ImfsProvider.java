package com.imfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
        if (fileSystem.contains(dstKid) || arg2.length > 0 && arg2[0] == StandardCopyOption.REPLACE_EXISTING) {
            throw new FileAlreadyExistsException("File at path:" + dst.toUri() + " already exists");
        }
        ImfsRecord srcRecord = fileSystem.getRecord(srcKid);
        if (srcRecord == null) {
            throw new NoSuchFileException("No such file or directory: " + src.toUri().toString());
        }

        // copy reuses the original file data, but changing the path
        // results in two records sharing the data.
        var dstRecord = srcRecord.toBuilder()
                .materializedPath(dstKid)
                .build();
        fileSystem.putRecord(dstRecord);
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... arg1) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        if (fileSystem.contains(kid)) {
            throw new FileAlreadyExistsException("File at path:" + path.toUri() + " already exists");
        }
        fileSystem.putRecord(ImfsRecord.ofDir(kid));
    }

    @Override
    public void delete(Path path) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        var record = fileSystem.getRecord(kid);
        if (record == null) {
            throw new NoSuchFileException("No such file or directory: " + imfsPath.toUri().toString());
        }
        if (!record.isFile()) {
            try (DirectoryStream<Path> dirStream = newDirectoryStream(path, null)) {
                if (dirStream.iterator().hasNext()) {
                    throw new DirectoryNotEmptyException("Directory not empty: " + imfsPath.toUri().toString());
                }
            }
        }
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
            return new ImfsH2FileSystem(this, key);
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
    }

    @Override
    public SeekableByteChannel newByteChannel(Path arg0, Set<? extends OpenOption> options, FileAttribute<?>... arg2)
            throws IOException {
        var imfsPath = checkPath(arg0);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var record = fileSystem.getRecord(imfsPath.getMaterializedPath());
        if (options.contains(StandardOpenOption.WRITE)) {
            if (record != null && options.contains(StandardOpenOption.CREATE_NEW)) {
                throw new FileAlreadyExistsException("File at path:" + imfsPath.toUri() + " already exists");
            }
            // TODO: check if clobbering existing files is always correct
            fileSystem.putBlob(imfsPath.getMaterializedPath(), new byte[] {});
            var result = new ImfsWritableByteChannel(imfsPath);
            return new ImfsSeekableByteChannel(result);
        } else if (options.contains(StandardOpenOption.READ) || options.isEmpty()) {
            if (record == null) {
                throw new FileNotFoundException("No such file or directory: " + imfsPath.toUri().toString());
            }

            return new ByteArraySeekableByteChannel(fileSystem.getBlob(imfsPath.getMaterializedPath()));
        }
        throw new UnsupportedOperationException("only READ and WRITE are implemented in 'newByteChannel'");
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
        var imfsPath = checkPath(path);
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        return fileSystem.streamChildren(imfsPath.getMaterializedPath(), filter);
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
            var record = fileSystem.getRecord(imfsPath.getMaterializedPath());
            if (record != null) {
                return classz.cast(ImfsFileAttributes.of(record));
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
