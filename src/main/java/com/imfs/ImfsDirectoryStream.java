package com.imfs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class ImfsDirectoryStream implements DirectoryStream<Path> {
    private final ImfsPath parent;
    private final ImfsChildren children;
    private final Stream<Path> stream;
    private int total = 0;
    private int kids = 0;
    long startTime = System.nanoTime();

    public ImfsDirectoryStream(ImfsPath parent, ImfsChildren children, Filter<? super Path> filter) {
        this.parent = parent;
        this.children = children;
        this.stream = children.getStream().filter(this::isChild)
                .filter(each -> {
                    try {
                        return filter == null || filter.accept(each);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

    }

    private boolean isChild(Path each) {
        total++;
        var result = each.getParent().equals(parent);
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
                + ": path=" + parent.toUri().toString()
                + ", version=" + children.getVersion()
                + ", size=" + children.getSize()
                + ", total=" + total
                + ", kids=" + kids
                + ", elapsed=" + elapsed);
        stream.close();
    }

}
