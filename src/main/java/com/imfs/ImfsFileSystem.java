package com.imfs;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.DirectoryStream.Filter;

public abstract class ImfsFileSystem extends FileSystem {

    public abstract boolean contains(String kid);

    public abstract ImfsRecord getRecord(String srcKid);

    public abstract void putRecord(ImfsRecord dstRecord);

    public abstract void removeEntry(String kid);

    public abstract void putBlob(String materializedPath, byte[] bs);

    public abstract DirectoryStream<Path> streamChildren(String materializedPath, Filter<? super Path> filter);

    public abstract byte[] getBlob(String string);

    public abstract void reset();

    public abstract String getKey();
}
