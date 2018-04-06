package Visualize3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * Stores the content of the 3D scene including meshes, lights, and cameras.
 */
public class ViewerModel {

    private final ObjectProperty<Node> contentProperty = new SimpleObjectProperty<>();
    private final Group root = new Group();
    private final SubScene subScene;
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Rotate cameraXRotate = new Rotate(-115, 0, 0, 0, Rotate.X_AXIS);
    private final Rotate cameraZRotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
    private final Translate cameraPosition = new Translate(0, 0, -20);
    private RotateTransition rotateTransition;
    private boolean isRotating;
    private double scaleFactor = 1;

    /**
     * Creates a content model for the 3D scene.
     */
    public ViewerModel() {

        subScene = new SubScene(root, 400, 400, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);

        camera.setNearClip(1);
        camera.setFarClip(10000);
        camera.getTransforms().addAll(cameraXRotate, cameraZRotate, cameraPosition);

        subScene.setCamera(camera);
        root.getChildren().add(camera);


        contentProperty.addListener(new ChangeListener<Node>() {

            @Override
            public void changed(final ObservableValue<? extends Node> ov, final Node oldContent, final Node newContent) {

                root.getChildren().remove(oldContent);

                if (newContent!=null) {
                    root.getChildren().add(newContent);

                    adjustForSize();

                    rotateTransition = new RotateTransition();
                    rotateTransition.setAxis(Rotate.Y_AXIS);
                    rotateTransition.setDelay(Duration.millis(4));
                    rotateTransition.setDuration(Duration.millis(5000));
                    rotateTransition.setCycleCount(Animation.INDEFINITE);
                    rotateTransition.setAutoReverse(false);
                    rotateTransition.setInterpolator(Interpolator.LINEAR);
                    rotateTransition.setByAngle(360);
                    rotateTransition.setNode(newContent);

                    if (isRotating) {
                        rotateTransition.play();
                    }
                }
            }
        });

    }


    public void setPitch(double angle){
        cameraXRotate.setAngle(-108-angle);
    }
    public void setRoll(double angle){
        cameraZRotate.setAngle(-angle);
    }


    /**
     * Property for the content of the 3D scene.
     *
     * @return the content property of the 3D scene
     */
    public ObjectProperty<Node> contentProperty() {
        return contentProperty;
    }

    /**
     * Gets the current content of the 3D scene.
     *
     * @return the current content of the 3D scene
     */
    public Node getContent() {
        return contentProperty.get();
    }

    /**
     * Sets the content to be displayed in the 3D scene.
     *
     * @return the content to be displayed
     */
    public void setContent(Node content) {
        contentProperty.set(content);
    }

    /**
     * Gets the sub-scene that the 3D model is displayed in.
     *
     * @return the sub-scene the 3D model is displayed in
     */
    public SubScene getSubScene() {
        return subScene;
    }

    /**
     * Toggles rotation of the content on and off.
     */
    public void toggleRotation() {

        if (isRotating) {
            rotateTransition.pause();
            isRotating = false;
        } else {
            rotateTransition.play();
            isRotating = true;
        }
    }

    /**
     * Gets the 3D scene's camera.
     *
     * @return the perspective camera of the 3D scene
     */
    public PerspectiveCamera getCamera() {
        return camera;
    }

    /**
     * Sets initial translate values and camera settings based on the size of the model.
     */
    private void adjustForSize() {

        Node content = contentProperty.get();

        double width = content.getLayoutBounds().getWidth();
        double height = content.getLayoutBounds().getHeight();
        double depth = content.getLayoutBounds().getDepth();

        content.setTranslateX(-content.getLayoutBounds().getMinX() - width / 2);
        content.setTranslateY(-content.getLayoutBounds().getMinY() - height / 2);
        content.setTranslateZ(-content.getLayoutBounds().getMinZ() - depth / 2);

        scaleFactor = Math.max(Math.max(width, height), depth)/25;

        cameraPosition.setZ(-60*scaleFactor);
    }


}
