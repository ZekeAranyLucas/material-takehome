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

    public ImfsContext(Path path) {
        this.path = (ImfsPath) path;
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

    public ImfsContext mkdir(String string) throws IOException {
        var result = Files.createDirectory(this.path.resolve(string));
        return new ImfsContext(result);
    }

    // change directory returns a new context if succesful, null otherwise.
    // this is so that context remains immutable.
    public ImfsContext cd(String string) {
        if (string.equals(".")) {
            return this;
        }
        if (string.equals("..")) {
            var parent = this.path.getParent();
            // won't fail even at the root
            return new ImfsContext(parent);
        }
        var child = this.path.resolve(string);
        if (Files.isDirectory(child)) {
            return new ImfsContext(child);
        }
        return null;
    }

}
