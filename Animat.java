package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author Manasi Deshmukh
 */
 
public class Animat {
    
    public static enum Type {
        
        UNEMPLOYED,
        FORAGER,
        STORER;
        
        public static ColorRGBA getColor(Type type){
            ColorRGBA colorRGBA = ColorRGBA.DarkGray;
            switch(type){
            case UNEMPLOYED :
                colorRGBA = ColorRGBA.White; break;
            case FORAGER :
                colorRGBA = ColorRGBA.Blue; break;
            case STORER :
                colorRGBA = ColorRGBA.Green; break;
            }
            return colorRGBA;
        }
        
        public static Node getNode(Type type){
            switch(type){ 
            case UNEMPLOYED :
                return Main.unemployedNode;
            case FORAGER :
                return Main.foragerNode;
            case STORER :
                return Main.storerNode;
            default:
                return null;
            }
        }
        
        public static float getSpeed(Type type){
            switch(type){ 
            case UNEMPLOYED :
                return 0.45f;
            case FORAGER :
                return 0.85f;
            case STORER :
                return 0.6f;
            default:
                return 0;
            }
        }
    }
    
    public static enum State{
        SEARCH_FOOD, COLLECT, RETURN, WAIT_FOR_STORER, UNLOAD, REST, LEAVE_HIVE,
        SEARCH_FORAGER, LOAD, STORE,
        ROAM,
        REFILL;
    }
    
    public static void loadAnimat(Type type) {
        
        Sphere b = new Sphere(20, 20, 0.05f);
        Geometry geom = new Geometry("Animat", b);
        Material mat = new Material(Main.myAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", Type.getColor(type));
        geom.setMaterial(mat);
        double random = Math.random();
        Vector3f location = Main.hive1.getLocalTranslation();
        Main.Hive hive = Main.Hive.HIVE1;
        if(random>0.75){
            location = Main.hive4.getLocalTranslation();
            hive = Main.Hive.HIVE4;
        }
        else if(random>0.5){
            location = Main.hive3.getLocalTranslation();
            hive = Main.Hive.HIVE3;
        }
        else if(random>0.25){
            location = Main.hive2.getLocalTranslation();
            hive = Main.Hive.HIVE4;
        }
        if(type==Animat.Type.FORAGER)
            geom.setLocalTranslation(location.add(0, 0, 2));
        else
            geom.setLocalTranslation(location.add(FastMath.nextRandomFloat()*5-2.5f, FastMath.nextRandomFloat()*5-2.5f, 0));
        geom.setUserData("HIVE", hive.toString());
        State state;
        switch(type){
            case FORAGER : state = State.SEARCH_FOOD; break;
            case STORER : state = State.SEARCH_FORAGER; break;
            case UNEMPLOYED : state = State.ROAM; break;
            default: state = null; break;
        }
        geom.addControl(new AnimatControl(type, state ,location,hive));
        Type.getNode(type).attachChild(geom);
        
    }
}
