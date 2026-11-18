package com.cgvsu.objreader;

import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class ObjReaderTest {

    @Test
    public void testParseVertex01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.03f);
        Assertions.assertTrue(result.equals(expectedResult));
    }

    @Test
    public void testParseVertex02() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.10f);
        Assertions.assertFalse(result.equals(expectedResult));
    }

    @Test
    public void testParseVertex03() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        ObjReaderException exception = Assertions.assertThrows(ObjReaderException.class,
                () -> ObjReader.parseVertex(wordsInLineWithoutToken, 10));
        Assertions.assertEquals("Error parsing OBJ file on line: 10. Failed to parse float value.", exception.getMessage());
    }

    @Test
    public void testParseVertex04() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        ObjReaderException exception = Assertions.assertThrows(ObjReaderException.class,
                () -> ObjReader.parseVertex(wordsInLineWithoutToken, 10));
        Assertions.assertEquals("Error parsing OBJ file on line: 10. Too few vertex arguments.", exception.getMessage());
    }

    @Test
    public void testParseVertex05() {
        // OBJ permits an optional fourth homogeneous coordinate.
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
        Assertions.assertTrue(ObjReader.parseVertex(wordsInLineWithoutToken, 10).equals(new Vector3f(1, 2, 3)));
    }

    @Test
    void readsWhitespaceCommentsAndRelativeIndices() {
        var model = ObjReader.read("  v 0 0 0\n v 1 0 0\n v 0 1 0\n  f -3 -2 -1 # triangle\n");
        Assertions.assertEquals(java.util.List.of(0, 1, 2), model.polygons.get(0).getVertexIndices());
    }

    @Test
    void rejectsInvalidFaces() {
        String vertices = "v 0 0 0\nv 1 0 0\nv 0 1 0\n";
        Assertions.assertThrows(ObjReaderException.class, () -> ObjReader.read(vertices + "f 1 2 4"));
        Assertions.assertThrows(ObjReaderException.class, () -> ObjReader.read(vertices + "f 1 2"));
        Assertions.assertThrows(ObjReaderException.class, () -> ObjReader.read(vertices + "f 0 2 3"));
        Assertions.assertThrows(ObjReaderException.class, () -> ObjReader.read(""));
        Assertions.assertThrows(ObjReaderException.class, () -> ObjReader.read("v NaN 0 0\nf 1 1 1"));
    }

    @Test
    void loadsExampleModels() throws java.io.IOException {
        for (String name : java.util.List.of("triangle.obj", "Teapot.obj", "Torus.obj")) {
            try (var input = ObjReaderTest.class.getResourceAsStream("/examples/" + name)) {
                Assertions.assertNotNull(input, name);
                var model = ObjReader.read(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                Assertions.assertFalse(model.vertices.isEmpty());
                Assertions.assertFalse(model.polygons.isEmpty());
            }
        }
    }
}
