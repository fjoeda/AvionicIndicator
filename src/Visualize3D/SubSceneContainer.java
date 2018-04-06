package Visualize3D;

import javafx.scene.SubScene;
import javafx.scene.layout.Pane;

public class SubSceneContainer extends Pane {
    private SubScene subScene;

    public void setSubScene(final SubScene subScene){
        this.subScene = subScene;
        getChildren().add(subScene);
    }

    protected void layoutChildren(){
        final double width = getWidth();
        final double height = getHeight();

        super.layoutChildren();

        subScene.setWidth(width);
        subScene.setHeight(height);
    }

}
