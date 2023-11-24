package com.imfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ByteArraySeekableByteChannel implements SeekableByteChannel {
    private byte[] array;
    private long position = 0;

    public ByteArraySeekableByteChannel(byte[] array) {
        this.array = array;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (position >= array.length) {
            return -1; // EOF
        }

        int length = Math.min(dst.remaining(), array.length - (int) position);
        dst.put(array, (int) position, length);
        position += length;
        return length;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'write'");
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        position = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return array.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'truncate'");
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // we don't really care, do we?
    }
}
