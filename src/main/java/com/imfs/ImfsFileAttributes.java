package com.imfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class ImfsFileAttributes implements BasicFileAttributes {

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
        return true;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isRegularFile() {
        return !isDirectory();
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

}
