package ca.awoo.lcmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.junit.Test;

import ca.awoo.lcmm.sync.Paths;

public class PathTest {
    @Test
    public void absoluteTest(){
        Path path = Paths.get("/foo");
        assertTrue("Path is absolute", path.isAbsolute());
    }

    @Test
    public void relativeTest(){
        Path path = Paths.get("foo");
        assertFalse("Path is relative", path.isAbsolute());
    }

    @Test
    public void getRootTest(){
        Path absolute = Paths.get("/foo");
        assertEquals("Root is Paths.root", Paths.root, absolute.getRoot());
        Path relative = Paths.get("foo");
        assertNull("Root is null", relative.getRoot());
    }

    @Test
    public void getFileNameTest(){
        Path path = Paths.get("/foo/bar");
        assertEquals("FileName is bar", Paths.get("bar"), path.getFileName());
    }

    @Test
    public void getParentTest(){
        Path path = Paths.get("/foo/bar");
        assertEquals("Parent is /foo", Paths.get("/foo"), path.getParent());
    }

    @Test
    public void getNameCountTest(){
        Path path0 = Paths.get("/");
        Path path0r = Paths.get("");
        Path path1 = Paths.get("/foo");
        Path path2 = Paths.get("/foo/bar");
        assertEquals("/ has 0 names", 0, path0.getNameCount());
        assertEquals("\"\" has 0 names", 0, path0r.getNameCount());
        assertEquals("/foo has 1 names", 1, path1.getNameCount());
        assertEquals("/foo/bar has 2 names", 2, path2.getNameCount());
    }

    @Test
    public void getNameTest(){
        Path abs = Paths.get("/foo/bar/baz");
        Path rel = Paths.get("foo/bar/baz");
        assertEquals("Name 0 -> foo", Paths.get("foo"), abs.getName(0));
        assertEquals("Name 0 -> foo", Paths.get("foo"), rel.getName(0));
        assertEquals("Name 1 -> bar", Paths.get("bar"), abs.getName(1));
        assertEquals("Name 1 -> bar", Paths.get("bar"), rel.getName(1));
        assertEquals("Name 2 -> baz", Paths.get("baz"), abs.getName(2));
        assertEquals("Name 2 -> baz", Paths.get("baz"), rel.getName(2));
    }

    @Test
    public void subPathTest(){
        Path path = Paths.get("/foo/bar/baz/buz");
        Path subPath = path.subpath(1, 3);
        assertEquals("Path is size 2", 2, subPath.getNameCount());
        assertEquals("Name 0 is bar", Paths.get("bar"), subPath.getName(0));
        assertEquals("Name 1 is baz", Paths.get("baz"), subPath.getName(1));
    }

    @Test
    public void startsWithTest(){
        Path path = Paths.get("/foo/bar/baz/buz");
        Path parent = Paths.get("/foo/bar");
        Path notParent = Paths.get("/baz/qux");
        assertTrue("/foo/bar/baz/buz starts with /foo/bar", path.startsWith(parent));
        assertFalse("/foo/bar/baz/buz does not start with /baz/qux", path.startsWith(notParent));
    }

    @Test
    public void endsWithTest(){
        Path path = Paths.get("/foo/bar/baz/buz");
        Path suffix = Paths.get("baz/buz");
        Path notSuffix = Paths.get("baz/qux");
        assertTrue("/foo/bar/baz/buz ends with baz/buz", path.endsWith(suffix));
        assertFalse("/foo/bar/baz/buz does not end with baz/qux", path.endsWith(notSuffix));
    }

    @Test
    public void normalizeTest(){
        Path path = Paths.get("/foo/bar/.././baz");
        Path normal = Paths.get("/foo/baz");
        assertEquals("Normalize", normal, path.normalize());
    }

    @Test
    public void resolveTest(){
        Path path = Paths.get("/foo/bar");
        Path abs = Paths.get("/baz");
        Path rel = Paths.get("baz");
        Path full = Paths.get("/foo/bar/baz");
        assertEquals("absolute", abs, path.resolve(abs));
        assertEquals("relative", full, path.resolve(rel));
    }

    @Test
    public void relativizeTest(){
        Path path = Paths.get("/foo/bar/baz");
        Path root = Paths.root;
        Path foo = Paths.get("/foo");
        assertEquals("Strip root", Paths.get("foo/bar/baz"), root.relativize(path));
        assertEquals("Strip foo", Paths.get("bar/baz"), foo.relativize(path));
    }

    @Test
    public void relativizeResolveTest(){
        Path p = Paths.get("/foo");
        Path q = Paths.get("bar");
        assertEquals("Test from javadoc", q, p.relativize(p.resolve(q)));
    }

    @Test
    public void toStringTest(){
        String[] strings = new String[]{
            "/foo",
            "/foo/bar",
            "/",
            "foo",
            "foo/bar"
        };
        for(String string : strings){
            Path path = Paths.get(string);
            assertEquals("String good", string, path.toString());
        }
    }
}
