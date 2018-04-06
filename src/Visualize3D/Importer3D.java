package Visualize3D;


import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.IOException;

/**
 * Base Importer for all supported 3D file formats.
 */
public final class Importer3D {


    public static Group load(final String fileUrl) throws IOException {
        StlMeshImporter stlImporter = new StlMeshImporter();
        stlImporter.read(fileUrl);
        // STL includes only geometry data
        TriangleMesh cylinderHeadMesh = stlImporter.getImport();

        stlImporter.close();

        // Create Shape3D
        MeshView cylinderHeadMeshView = new MeshView();
        cylinderHeadMeshView.setMaterial(new PhongMaterial(Color.DARKBLUE));
        cylinderHeadMeshView.setMesh(cylinderHeadMesh);

        stlImporter.close();
        return new Group(cylinderHeadMeshView);
    }
}
