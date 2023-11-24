package com.imfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

final class ImfsSeekableByteChannel implements SeekableByteChannel {
    private final ImfsWritableByteChannel result;

    ImfsSeekableByteChannel(ImfsWritableByteChannel result) {
        this.result = result;
    }

    @Override
    public void close() throws IOException {
        result.close();
    }

    @Override
    public boolean isOpen() {
        return result.isOpen();
    }

    @Override
    public long position() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'position'");
    }

    @Override
    public SeekableByteChannel position(long arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'position'");
    }

    @Override
    public int read(ByteBuffer arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public long size() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

    @Override
    public SeekableByteChannel truncate(long arg0) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'truncate'");
    }

    @Override
    public int write(ByteBuffer arg0) throws IOException {
        return result.write(arg0);
    }
}