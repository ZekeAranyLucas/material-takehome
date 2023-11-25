package com.imfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        assertArrayEquals(new String[] { "Spanish", "foo", "history", "math" }, kids.toArray());
    }

    @Test
    public void testCdSuccess() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
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

    private void assertDefaultKids(List<String> kids) {
        assertArrayEquals(new String[] { "Spanish", "history", "math" }, kids.toArray());
    }

    @Test
    public void testCdFailure() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var math = context.cd("foo");
        assertEquals(3, context.ls().size());
        assertEquals(null, math);
    }

    @Test
    public void testCdLevels() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var math = context.cd("math");
        assertEquals(3, context.ls().size());
        assertEquals(0, math.ls().size());
        assertEquals("/math", math.pwd());

        var foo = math.mkdir("foo");
        kids = context.ls();
        assertDefaultKids(kids);
        assertEquals(1, math.ls().size());
        assertArrayEquals(new String[] { "foo" }, math.ls().toArray());
        assertEquals(3, context.ls().size());
        assertEquals(0, foo.ls().size());
    }

    @Test
    public void testRmdir() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);

        var history = Paths.get(URI.create("imfs://ImfsContextTest/history"));
        assertEquals(true, Files.isDirectory(history));

        context.rmdir("history");
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "math" }, kids.toArray());
        assertEquals(false, Files.isDirectory(history));
    }

    @Test
    public void testMkfile() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);

        context.mkfile("fun.txt");
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "fun.txt", "history", "math" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isDirectory(fun));
    }

    @Test
    public void testMkfileFails() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);

        assertThrows(FileAlreadyExistsException.class,
                () -> context.mkfile("math"));

    }

    @Test
    public void testWriteLines() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);

        context.write("fun.txt", new String[] { "hello", "world" });
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "fun.txt", "history", "math" }, kids.toArray());
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
        assertDefaultKids(kids);

        context.write("fun.txt", new String[] { "hello", "world", "again" });
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "fun.txt", "history", "math" }, kids.toArray());
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isDirectory(fun));

        var lines = context.readLines("fun.txt");
        assertArrayEquals(new String[] { "hello", "world", "again" }, lines.toArray());
    }

    @Test
    public void testMoveDir() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/arithmetics"));
        assertEquals(false, Files.isDirectory(fun));

        context.mv("math", "arithmetics");

        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "arithmetics", "history" }, kids.toArray());
        assertEquals(true, Files.isDirectory(fun));
    }

    @Test
    public void testMoveFile() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);

        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isRegularFile(fun));

        context.write("dumb.txt", new String[] { "hello", "world", "again" });
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "dumb.txt", "history", "math" }, kids.toArray());

        context.mv("dumb.txt", "fun.txt");

        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "fun.txt", "history", "math" }, kids.toArray());
        assertEquals(true, Files.isRegularFile(fun));
    }

    @Test
    public void testCopyDir() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/arithmetics"));
        assertEquals(false, Files.isDirectory(fun));

        context.cp("math", "arithmetics");

        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "arithmetics", "history", "math" }, kids.toArray());
        assertEquals(true, Files.isDirectory(fun));
    }

    @Test
    public void testCopyFile() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isRegularFile(fun));

        context.write("dumb.txt", new String[] { "hello", "world", "again" });
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "dumb.txt", "history", "math" }, kids.toArray());

        context.cp("dumb.txt", "fun.txt");

        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "dumb.txt", "fun.txt", "history", "math" }, kids.toArray());
        assertEquals(true, Files.isRegularFile(fun));
    }

    @Test
    public void testCopyColllision() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var fun = Paths.get(URI.create("imfs://ImfsContextTest/fun.txt"));
        assertEquals(false, Files.isRegularFile(fun));

        context.write("dumb.txt", new String[] { "hello", "world", "again" });
        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "dumb.txt", "history", "math" }, kids.toArray());

        assertThrows(FileAlreadyExistsException.class, () -> context.cp("dumb.txt", "math"));
    }

    @Test
    public void testFindName() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        assertEquals("imfs://ImfsContextTest/math", context.find("math").toString());
        assertEquals(null, context.find("fun.txt"));
    }

    @Test
    public void testRmdirNonEmpty() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var math = context.cd("math");
        assertEquals(3, context.ls().size());
        assertEquals(0, math.ls().size());
        assertEquals("/math", math.pwd());

        var foo = math.mkdir("foo");
        kids = context.ls();
        assertDefaultKids(kids);
        assertEquals(1, math.ls().size());
        assertArrayEquals(new String[] { "foo" }, math.ls().toArray());
        assertEquals(3, context.ls().size());
        assertEquals(0, foo.ls().size());

        assertThrows(DirectoryNotEmptyException.class, () -> context.rmdir("math"));
    }

    @Test
    public void testRmdirsNonEmpty() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();
        assertDefaultKids(kids);
        var math = context.cd("math");
        assertEquals(3, context.ls().size());
        assertEquals(0, math.ls().size());
        assertEquals("/math", math.pwd());

        var foo = math.mkdir("foo");
        kids = context.ls();
        assertDefaultKids(kids);
        assertEquals(1, math.ls().size());
        assertArrayEquals(new String[] { "foo" }, math.ls().toArray());
        assertEquals(3, context.ls().size());
        assertEquals(0, foo.ls().size());

        context.rmdirs("math");

        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "history" }, kids.toArray());
        assertEquals(0, math.ls().size());
        assertEquals(false, Files.isDirectory(foo.getPath()));
    }

    @Test
    public void testImportFiles() throws IOException {
        var context = new ImfsContext("imfs://ImfsContextTest/");
        var kids = context.ls();

        // use working dir to copy the source code files into memory
        context.importFiles("src", "src");

        kids = context.ls();
        assertArrayEquals(new String[] { "Spanish", "history", "math", "src" }, kids.toArray());
        Pattern pattern = Pattern.compile(".*Test\\.java$");
        var tests = Files.find(context.getPath(), Integer.MAX_VALUE, (path, attr) -> {
            var pathString = path.toUri().toString();
            var matched = pattern.matcher(pathString).matches();
            return matched;
        }).map(path -> path.toUri().getPath()).collect(Collectors.toList());
        assertArrayEquals(new String[] { "/src/test/java/com/imfs/ImfsContextTest.java",
                "/src/test/java/com/imfs/ImfsProviderTest.java",
                "/src/test/java/com/imfs/app/AppTest.java" }, tests.toArray());

    }
}
