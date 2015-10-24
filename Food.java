package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 *
 * @author Manasi Deshmukh
 */
 
public class Food {
    
    public static void loadFoad(int value, Vector3f location){
        
        if(value==0)
            return;
        Box b = new Box(0.025f, 0.025f, 0f);
        Geometry geom = new Geometry("Food", b);
        Material mat = new Material(Main.myAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        geom.setMaterial(mat);
        geom.setLocalTranslation(location);
        geom.addControl(new FoodControl(value));
        Main.foodNode.attachChild(geom);
        
    }
}
