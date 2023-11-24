package com.imfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
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

    @Test
    public void testMkfile() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());

        context.mkfile("fun.txt");
        kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish", "fun.txt" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isDirectory(fun));
    }

    @Test
    public void testMkfileFails() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());

        assertThrows(FileAlreadyExistsException.class,
                () -> context.mkfile("math"));

    }

    @Test
    public void testWriteLines() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());

        context.write("fun.txt", new String[] { "hello", "world" });
        kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish", "fun.txt" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isDirectory(fun));

        var path = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        var fileSystem = (ImfsFileSystem) path.getFileSystem();
        var blob = fileSystem.getBlob("fun.txt");
        assertArrayEquals(new byte[] { 'h', 'e', 'l', 'l', 'o', '\n', 'w', 'o', 'r', 'l', 'd', '\n' }, blob);
    }

    @Test
    public void testReadLines() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());

        context.write("fun.txt", new String[] { "hello", "world", "again" });
        kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish", "fun.txt" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isDirectory(fun));

        var lines = context.readLines("fun.txt");
        assertArrayEquals(new String[] { "hello", "world", "again" }, lines.toArray());
    }

    @Test
    public void testMoveDir() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/arithmetics"));
        assertEquals(false, Files.isDirectory(fun));

        context.mv("math", "arithmetics");

        kids = context.ls();
        assertArrayEquals(new String[] { "history", "spanish", "arithmetics" }, kids.toArray());
        assertEquals(true, Files.isDirectory(fun));
    }

    @Test
    public void testMoveFile() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isRegularFile(fun));

        context.write("dumb.txt", new String[] { "hello", "world", "again" });
        kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish", "dumb.txt" }, kids.toArray());

        context.mv("dumb.txt", "fun.txt");

        kids = context.ls();
        assertArrayEquals(new String[] { "math", "history", "spanish", "fun.txt" }, kids.toArray());
        assertEquals(true, Files.isRegularFile(fun));
    }

    @Test
    public void testRmdirNonEmpty() throws IOException {
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

        // TODO: this should fail, but right now it doesn't
        // Files.delete() doesn't throw an exception when deleting a non-empty directory
        // so it must fall to the FileSystem implementation to do so.
        assertThrows(IOException.class, () -> context.rmdir("math"));
    }
}
