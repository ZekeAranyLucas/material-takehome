package com.imfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import lombok.Builder;

@Builder
public class ImfsFileAttributes implements BasicFileAttributes {
    private String materializedPath;
    private boolean hasBytes;
    private long size;

    // TODO: this is a hack to get the size of the file
    public static ImfsFileAttributes of(ImfsRecord record) {
        return ImfsFileAttributes.builder()
                .materializedPath(record.getMaterializedPath())
                .hasBytes(record.isFile())
                .size(record.isFile() ? 100 : 0)
                .build();
    }

    @Override
    public FileTime creationTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'creationTime'");
    }

    @Override
    public Object fileKey() {
        return this.materializedPath;
    }

    @Override
    public boolean isDirectory() {
        return !this.hasBytes;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isRegularFile() {
        return this.hasBytes;
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
