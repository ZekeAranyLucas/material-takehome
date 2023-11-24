package com.imfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

// Deliberately not constrained to unit testing,
// while still being developer testing.
public class ImfsContextTest {
    @Before
    public void setup() throws IOException {
        var path = Paths.get(URI.create("imfs://ImfsContextTest/"));
        var fs = (ImfsFileSystem) path.getFileSystem();
        fs.reset();

    }

    @Test
    public void testPwd() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        assertEquals("/", context.pwd());
    }

    @Test
    public void testMkdir() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        assertEquals("/", context.pwd());
        var kids = context.ls();
        // Magic: 3 is because "*Test" is pre-populated with 3 entries
        assertEquals(3, kids.size());
        context.mkdir("foo");
        kids = context.ls();
        assertEquals(4, kids.size());
        assertArrayEquals(kids.toArray(), new String[] { "math", "history", "spanish", "foo" });
        // context.cd("foo");
        // context.ls(); }
    }
}
