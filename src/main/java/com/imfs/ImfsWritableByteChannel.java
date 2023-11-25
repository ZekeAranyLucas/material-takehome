package com.imfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class ImfsWritableByteChannel implements WritableByteChannel {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final WritableByteChannel channel = Channels.newChannel(output);
    private boolean isOpen = true;
    private final ImfsPath path;

    public ImfsWritableByteChannel(ImfsPath imfsPath) {
        this.path = imfsPath;
    }

    @Override
    public void close() throws IOException {
        channel.close();
        output.close();
        var bytes = output.toByteArray();
        var fileSystem = (ImfsFileSystem) path.getFileSystem();
        fileSystem.putBlob(path.getMaterializedPath(), bytes);
        this.isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public int write(ByteBuffer arg0) throws IOException {
        return channel.write(arg0);
    }

}
