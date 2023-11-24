package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
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

    public void rmdir(String string) throws IOException {
        Files.delete(this.path.resolve(string));
    }

    public void mkfile(String string) throws IOException {
        Files.createFile(this.path.resolve(string));
    }

    public void write(String string, String[] lines) throws IOException {
        Path file = this.path.resolve(string);
        Files.write(file, Arrays.asList(lines), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public List<String> readLines(String string) throws IOException {
        Path file = this.path.resolve(string);
        return Files.readAllLines(file);
    }

    public void mv(String src, String dst) throws IOException {
        Path srcPath = this.path.resolve(src);
        Path dstPath = this.path.resolve(dst);
        Files.move(srcPath, dstPath);
    }

}
