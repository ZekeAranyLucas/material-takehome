package com.imfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class ImfsFileAttributes implements BasicFileAttributes {

    private boolean isDirectory;
    private long size;

    public ImfsFileAttributes(ImfsPath imfsPath) {
        var fileSystem = (ImfsFileSystem) imfsPath.getFileSystem();
        var kid = imfsPath.getMaterializedPath();
        this.isDirectory = fileSystem.isDirectory(kid);
        this.size = this.isDirectory ? 0 : fileSystem.getSize(kid);
    }

    @Override
    public FileTime creationTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creationTime'");
    }

    @Override
    public Object fileKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fileKey'");
    }

    @Override
    public boolean isDirectory() {
        // TODO: only supports directories for now!
        return this.isDirectory;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isRegularFile() {
        return !this.isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public FileTime lastAccessTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lastAccessTime'");
    }

    @Override
    public FileTime lastModifiedTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lastModifiedTime'");
    }

    @Override
    public long size() {
        return this.size;
    }

}
