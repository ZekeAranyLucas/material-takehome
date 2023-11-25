package com.imfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ImfsContext class represents a context for interacting with the Imfs file
 * system.
 * It provides methods for navigating the file system, creating directories and
 * files,
 * reading and writing files, and performing file operations such as moving and
 * copying.
 * It works primarily through Java's NIO.2 API, which Imfs implements.
 */
public class ImfsContext {

    private ImfsPath path;

    public ImfsContext(String string) {
        var uri = URI.create(string);
        this.path = (ImfsPath) Paths.get(uri);
    }

    public ImfsContext(Path path) {
        this.path = (ImfsPath) path;
    }

    /**
     * Returns the path associated with this ImfsContext.
     *
     * @return the path associated with this ImfsContext
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Returns the current working directory path as a string.
     *
     * @return the current working directory path as a string
     */
    public String pwd() {
        return this.path.toUri().getPath();
    }

    /**
     * Returns a list of strings representing the names of the files and directories
     * in the current directory.
     *
     * @return a list of strings representing the names of the files and directories
     *         in the current directory
     * @throws IOException if an I/O error occurs while listing the files and
     *                     directories
     */
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

    /**
     * Changes the current directory to the specified directory.
     * 
     * @param string the name of the directory to change to
     * @return the new ImfsContext representing the changed directory, or null if
     *         the directory does not exist
     */
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

    /**
     * Removes the specified directory.
     *
     * @param directoryName the name of the directory to be removed
     * @return true if the directory is successfully removed, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public void rmdir(String string) throws IOException {
        Files.delete(this.path.resolve(string));
    }

    /**
     * Removes a directory and all its contents recursively.
     *
     * @param string the name of the directory to be removed
     * @throws IOException if an I/O error occurs during the directory removal
     */
    public void rmdirs(String string) throws IOException {
        var target = this.path.resolve(string);
        Files.walkFileTree(target,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                            Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });

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

    public void cp(String src, String dst) throws IOException {
        Path srcPath = this.path.resolve(src);
        Path dstPath = this.path.resolve(dst);
        Files.copy(srcPath, dstPath);
    }

    public URI find(String string) {
        Path target = this.path.resolve(string);
        return Files.exists(target) ? target.toUri() : null;
    }

}
