package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class ImfsDirectoryStream implements DirectoryStream<Path> {
    private FileSystem fileSystem;
    private String parent;
    private int version = 0;
    private int inputSize;
    private Stream<Path> stream;
    private int total = 0;
    private int kids = 0;
    long startTime = System.nanoTime();
    private final int offset;
    private String fileSystemKey;

    public ImfsDirectoryStream(FileSystem fileSystem, String fileSystemKey, String materializedPath,
            Stream<String> input, int inputSize, Filter<? super Path> filter) {
        this.fileSystem = fileSystem;
        this.fileSystemKey = fileSystemKey;
        this.parent = materializedPath;
        this.inputSize = inputSize;
        this.offset = parent.length() + 1;

        this.stream = input.filter(this::isChild)
                .map(this::toPath)
                .filter(each -> {
                    try {
                        return filter == null || filter.accept(each);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    private Path toPath(String entry) {
        return new ImfsPath(fileSystem, URI.create("imfs://" + fileSystemKey + "/" + entry));
    }

    private boolean isChild(String each) {
        total++;
        // paths with no extra slashes are children
        var result = each.indexOf("/", offset) == -1;
        kids += result ? 1 : 0;
        return result;
    }

    @Override
    public Iterator<Path> iterator() {
        return stream.iterator();
    }

    @Override
    public void close() throws IOException {
        // Print the count when the stream is closed
        // ... the code being measured ...
        long endTime = System.nanoTime();

        long elapsed = (endTime - startTime) / 1_000_000;

        System.out.println("ImfsDirectoryStream"
                + ": path=" + parent
                + ", version=" + version
                + ", size=" + inputSize
                + ", total=" + total
                + ", kids=" + kids
                + ", elapsed=" + elapsed);
        stream.close();
    }

}
