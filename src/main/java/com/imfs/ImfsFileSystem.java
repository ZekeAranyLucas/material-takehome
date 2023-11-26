package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.TreeMap;

import java.util.Arrays;
import java.util.List;

public class ImfsFileSystem extends FileSystem {
    private static final List<ImfsRecord> TEST_DIRS = Arrays.asList(
            ImfsRecord.ofDir("math"),
            ImfsRecord.ofDir("history"),
            ImfsRecord.ofDir("Spanish"));

    private ImfsProvider provider;
    private String key;
    private final ImfsRecord ROOT = ImfsRecord.ofDir("");

    TreeMap<String, ImfsRecord> records = new TreeMap<>();

    public ImfsFileSystem(ImfsProvider imfsProvider, String key) {
        this.provider = imfsProvider;
        this.key = key;
        this.records = initEntries(key);
    }

    private static TreeMap<String, ImfsRecord> initEntries(String key) {
        TreeMap<String, ImfsRecord> result = new TreeMap<>();
        if (key.contains("Test")) {
            TEST_DIRS.forEach(record -> result.put(record.getMaterializedPath(), record));
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

    public ImfsChildren streamAllPaths() {
        System.out.println("--- streamAllPaths: records.size() = " + records.size());
        return ImfsChildren.builder()
                .size(records.size())
                .version(0)
                .stream(records.keySet().stream()
                        .map(entry -> {
                            return new ImfsPath(this, URI.create("imfs://" + key + "/" + entry));
                        }))
                .build();

    }

    public ImfsChildren streamChildren(String materializedPath) {
        if (materializedPath.length() == 0) {
            return streamAllPaths();
        }

        var from = materializedPath + "/";
        var to = materializedPath + "0";
        var sub = records.subMap(from, to);
        System.out.println("--- streamChildren: sub.size() = " + sub.size());

        return ImfsChildren.builder()
                .size(sub.size())
                .version(0)
                .stream(sub.keySet().stream()
                        .map(entry -> {
                            return new ImfsPath(this, URI.create("imfs://" + key + "/" + entry));
                        }))
                .build();
    }

    public boolean contains(String materializedPath) {
        return records.containsKey(materializedPath);
    }

    public void reset() {
        records = initEntries(key);
    }

    public void removeEntry(String materializedPath) {
        records.remove(materializedPath);
    }

    public void putBlob(String materializedPath, byte[] bytes) {
        records.put(materializedPath,
                ImfsRecord.builder().materializedPath(materializedPath).bytes(bytes).build());
    }

    public byte[] getBlob(String materializedPath) {
        return records.get(materializedPath).getBytes();
    }

    public ImfsRecord getRecord(String materializedPath) {
        if (materializedPath.length() == 0) {
            return ROOT;
        }
        return records.get(materializedPath);
    }

    public void putRecord(ImfsRecord record) {
        records.put(record.getMaterializedPath(), record);
    }
}
