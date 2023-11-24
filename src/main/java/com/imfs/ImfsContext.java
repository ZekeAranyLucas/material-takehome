package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ImfsContext {

    private ImfsPath path;

    public ImfsContext(String string) {
        var uri = URI.create(string);
        this.path = (ImfsPath) Paths.get(uri);
    }

    public Path getPath() {
        return this.path;
    }

    public String pwd() {
        return this.path.toUri().getPath();
    }

    public List<String> ls() throws IOException {
        return Files.list(this.path)
                .map(kid -> path.toUri().relativize(kid.toUri()))
                .map(rel -> rel.toString())
                .collect(Collectors.toList());
    }

    public void mkdir(String string) throws IOException {
        Files.createDirectory(this.path.resolve(string));
    }

}
