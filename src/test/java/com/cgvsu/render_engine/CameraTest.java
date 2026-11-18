package com.cgvsu.render_engine;

import org.junit.jupiter.api.Test;
import javax.vecmath.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

class CameraTest {
    @Test void movesTargetByTranslation() {
        Camera camera = new Camera(new Vector3f(0, 0, 5), new Vector3f(1, 2, 3), 1, 1, .1f, 100);
        camera.moveTarget(new Vector3f(2, -1, 4));
        assertEquals(new Vector3f(3, 1, 7), camera.getTarget());
    }

    @Test void mapsViewportCornersAndCenter() {
        var topLeft = GraphicConveyor.vertexToPoint(new Vector3f(-1, 1, 0), 101, 51);
        var bottomRight = GraphicConveyor.vertexToPoint(new Vector3f(1, -1, 0), 101, 51);
        var center = GraphicConveyor.vertexToPoint(new Vector3f(0, 0, 0), 101, 51);
        assertEquals(0, topLeft.x); assertEquals(0, topLeft.y);
        assertEquals(100, bottomRight.x); assertEquals(50, bottomRight.y);
        assertEquals(50, center.x); assertEquals(25, center.y);
    }
}
