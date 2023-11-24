package com.imfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
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
        assertArrayEquals(new String[] { "math", "history", "spanish", "foo" }, kids.toArray());
    }

    @Test
    public void testCdSuccess() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());
        var math = context.cd("math");
        assertEquals(3, context.ls().size());
        assertEquals(0, math.ls().size());
        assertEquals("/math", math.pwd());

        var parent = math.cd("..");
        assertEquals(3, context.ls().size());
        assertEquals(0, math.ls().size());
        assertEquals(3, parent.ls().size());
        assertEquals("/", parent.pwd());
    }

    @Test
    public void testCdFailure() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());
        var math = context.cd("foo");
        assertEquals(3, context.ls().size());
        assertEquals(null, math);
    }

    @Test
    public void testCdLevels() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());
        var math = context.cd("math");
        assertEquals(3, context.ls().size());
        assertEquals(0, math.ls().size());
        assertEquals("/math", math.pwd());

        var foo = math.mkdir("foo");
        kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());
        assertEquals(1, math.ls().size());
        assertArrayEquals(new String[] { "foo" }, math.ls().toArray());
        assertEquals(3, context.ls().size());
        assertEquals(0, foo.ls().size());
    }

    @Test
    public void testRmdir() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());

        var history = Paths.get(URI.create("imfs://ImfsContextTest/history"));
        assertEquals(true, Files.isDirectory(history));

        context.rmdir("history");
        kids = context.ls();
        assertArrayEquals(new String[] { "math", "spanish" }, kids.toArray());
        assertEquals(false, Files.isDirectory(history));
    }
}
