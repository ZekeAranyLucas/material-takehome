package com.imfs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Paths;

// Deliberately not constrained to unit testing,
// while still being developer testing.
public class ImfsProviderTest {

    @Test
    public void testGetScheme() {
        ImfsProvider provider = new ImfsProvider();
        String scheme = provider.getScheme();
        assertEquals("imfs", scheme);
    }

    @Test
    public void testGetProvider() {
        var path = Paths.get(URI.create("imfs://default/"));
        assertEquals("imfs://default/", path.toUri().toString());
        assertEquals(ImfsPath.class, path.getClass());
    }

    @Test
    public void testGetFileSystem() {
        ImfsProvider provider = new ImfsProvider();
        FileSystem fileSystem = provider.getFileSystem(URI.create("imfs://default/"));
        assertNotNull(fileSystem);
        assertTrue(fileSystem instanceof ImfsFileSystem);
        assertEquals("default", ((ImfsFileSystem) fileSystem).getKey());
    }
}