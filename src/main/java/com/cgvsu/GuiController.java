package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.io.IOException;
import java.io.File;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    private static final float TRANSLATION = 0.5F;

    @FXML
    private StackPane viewport;

    @FXML
    private Canvas canvas;

    private Model mesh;

    private final Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100000);

    private Timeline timeline;

    @FXML
    private void initialize() {
        canvas.widthProperty().bind(viewport.widthProperty());
        canvas.heightProperty().bind(viewport.heightProperty());

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            if (width <= 0 || height <= 0) return;
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String fileContent = Files.readString(file.toPath());
            Model loaded = ObjReader.read(fileContent);
            frameModel(loaded);
            mesh = loaded;
        } catch (IOException | ObjReaderException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(canvas.getScene().getWindow());
            alert.setTitle("Cannot load model");
            alert.setHeaderText("Could not open " + file.getName());
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    private void frameModel(Model model) {
        float minX = Float.POSITIVE_INFINITY, minY = minX, minZ = minX;
        float maxX = Float.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (var vertex : model.vertices) {
            minX = Math.min(minX, vertex.x); maxX = Math.max(maxX, vertex.x);
            minY = Math.min(minY, vertex.y); maxY = Math.max(maxY, vertex.y);
            minZ = Math.min(minZ, vertex.z); maxZ = Math.max(maxZ, vertex.z);
        }
        float x = (minX + maxX) / 2, y = (minY + maxY) / 2, z = (minZ + maxZ) / 2;
        float size = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        float aspect = (float) (canvas.getHeight() > 0 ? canvas.getWidth() / canvas.getHeight() : 1);
        float distance = Math.max(0.1f, size * 1.8f / Math.min(1, Math.max(aspect, 0.1f)));
        camera.setTarget(new Vector3f(x, y, z));
        camera.setPosition(new Vector3f(x, y, z + distance));
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}
