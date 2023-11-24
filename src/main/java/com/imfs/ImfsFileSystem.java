package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.HashMap;

public class ImfsFileSystem extends FileSystem {

    private ImfsProvider provider;
    private String key;

    private ArrayList<String> entries;
    private HashMap<String, byte[]> blobs;

    public ImfsFileSystem(ImfsProvider imfsProvider, String key) {
        this.provider = imfsProvider;
        this.key = key;
        this.entries = initEntries(key);
        this.blobs = new HashMap<>();
    }

    private static ArrayList<String> initEntries(String key) {
        var result = new ArrayList<String>();
        if (key.contains("Test")) {
            result.addAll(Arrays.asList("math", "history", "spanish"));
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFileStores'");
    }

    @Override
    public Path getPath(String arg0, String... arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPath'");
    }

    @Override
    public PathMatcher getPathMatcher(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPathMatcher'");
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRootDirectories'");
    }

    @Override
    public String getSeparator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSeparator'");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserPrincipalLookupService'");
    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isOpen'");
    }

    @Override
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isReadOnly'");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newWatchService'");
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supportedFileAttributeViews'");
    }

    public String getKey() {
        return key;
    }

    public Stream<Path> streamAllPaths() {
        return entries.stream()
                .map(entry -> new ImfsPath(this, URI.create("imfs://" + key + "/" + entry)));
    }

    public void addEntry(String kid) {
        entries.add(kid);
    }

    public boolean contains(String kid) {
        return entries.contains(kid);
    }

    public void reset() {
        entries = initEntries(key);
        this.blobs = new HashMap<>();
    }

    public void removeEntry(String kid) {
        entries.remove(kid);
        blobs.remove(kid);
    }

    public void putBlob(String kid, byte[] bytes) {
        blobs.put(kid, bytes);
    }

    public boolean isDirectory(String kid) {
        return !blobs.containsKey(kid);
    }

    public long getSize(String kid) {
        return blobs.get(kid).length;
    }
}
