package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.io.BufferedWriter;
import java.util.Random;
import static mygame.Main.unemployedNode;


/**
 * Animat Project
 * @author Manasi Deshmukh
 */
 
public class Main extends SimpleApplication {

    static AssetManager myAssetManager;
    static Node unemployedNode;
    static Node foragerNode;
    static Node storerNode;
    static Node nurseNode;
    static Node foodNode;
    static Node hive1, hive2, hive3, hive4;
    
    long spawnTimeStart;
    int foodSpawnChance = 20;
    int k = 0;
    boolean flag1 = false, flag2 = false;
    long start;
    
    BufferedWriter output;
    
    public static enum Hive{
        HIVE1,
        HIVE2,
        HIVE3,
        HIVE4;
        
        public static Node getHiveNode(Hive hive){
            switch(hive){
                case HIVE1 : return hive1;
                case HIVE2 : return hive2;
                case HIVE3 : return hive3;
                case HIVE4 : return hive4;
                default: return null;
            }
        }
    }
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Main.myAssetManager = assetManager;
        
        unemployedNode = new Node("Unemployed");
        foragerNode = new Node("Forager");
        storerNode = new Node("Storer");
        nurseNode = new Node("Nurse");
        foodNode = new Node("Food");
        hive1 = new Node();
        
        rootNode.attachChild(unemployedNode);
        rootNode.attachChild(foragerNode);
        rootNode.attachChild(storerNode);
        rootNode.attachChild(nurseNode);
        rootNode.attachChild(foodNode);
        
        initEnvironment();
        
        for(int i = 0; i<65; i++){
            Animat.loadAnimat(Animat.Type.UNEMPLOYED);
        }
        
        for(int i = 0; i<400; i++){
            Animat.loadAnimat(Animat.Type.FORAGER);
        }
        
        for(int i = 0; i<235; i++){
            Animat.loadAnimat(Animat.Type.STORER);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        spawnFood();
        for(int i = 0; i<foragerNode.getQuantity() ; i++){
            Spatial forager = foragerNode.getChild(i);
            AnimatControl foragerControl = forager.getControl(AnimatControl.class);
            Signal foragerSignal = (Signal) foragerControl;
            if(i==0 || i==foragerNode.getQuantity()-1){
                for(int j=0; j<unemployedNode.getQuantity() ; j++){
                    Spatial unemployed = unemployedNode.getChild(j);
                    AnimatControl unemployedControl = unemployed.getControl(AnimatControl.class);
                    Signal unemployedSignal = (Signal) unemployedControl;
                    if(i==0){
                        unemployedControl.resetStorerStimulus();
                        unemployedControl.resetForagerStimulus();
                    }
                    else if(i==foragerNode.getQuantity()-1){
                        if(unemployedControl.getStorerStimulus()>0)
                            unemployedSignal.onSignalReceived(Signal.Type.UNEMPLOYED_BECOME_STORER, null);
                        if(unemployedControl.getForagerStimulus()>0)
                            unemployedSignal.onSignalReceived(Signal.Type.UNEMPLOYED_BECOME_FORAGER, null);
                    }
                }
            }
            switch(foragerControl.getState()){
            case SEARCH_FOOD :
                for(int j=0; j<foodNode.getQuantity() ; j++){
                    Spatial food = foodNode.getChild(j);
                    if(forager.getLocalTranslation().distance(food.getLocalTranslation())<2* FastMath.sqrt(2))
                        foragerSignal.onSignalReceived(Signal.Type.FORAGER_FOOD_FOUND, food);
                    }
                break;
            case WAIT_FOR_STORER :
                if(foragerControl.getWaitTime()>5000){
                    foragerControl.resetWaitTime();
                    for(int j=0; j<unemployedNode.getQuantity() ; j++){
                        Spatial unemployed = unemployedNode.getChild(j);
                        AnimatControl unemployedControl = unemployed.getControl(AnimatControl.class);
                        float distance = forager.getLocalTranslation().distance(unemployed.getLocalTranslation());
                        if(Main.Hive.valueOf((String)forager.getUserData("HIVE"))==Main.Hive.valueOf((String)unemployed.getUserData("HIVE"))&&distance<2.24f){
                            float intensity = 1/(FastMath.sqrt(FastMath.sqr(distance)-4)+1);
                            unemployedControl.increaseStorerStimulus(intensity);
                        }
                    }
                }
                
                for(int j=0; j<storerNode.getQuantity() ; j++){
                    Spatial storer = storerNode.getChild(j);
                    AnimatControl storerControl = storer.getControl(AnimatControl.class);
                    Signal storerSignal = (Signal) storerControl;
                    if(Main.Hive.valueOf((String)forager.getUserData("HIVE"))==Main.Hive.valueOf((String)storer.getUserData("HIVE"))&&storerControl.getState()== Animat.State.SEARCH_FORAGER&&forager.getLocalTranslation().distance(storer.getLocalTranslation())<2.14f)
                        storerSignal.onSignalReceived(Signal.Type.STORER_FOOD_ARRIVED, forager);
                }
                break;
            case UNLOAD :
                if(foragerControl.getWaitTime()<1500){
                    foragerControl.resetWaitTime();
                    for(int j=0; j<unemployedNode.getQuantity() ; j++){
                        Spatial unemployed = unemployedNode.getChild(j);
                        AnimatControl unemployedControl = unemployed.getControl(AnimatControl.class);
                        float distance = forager.getLocalTranslation().distance(unemployed.getLocalTranslation());
                        if(Main.Hive.valueOf((String)forager.getUserData("HIVE"))==Main.Hive.valueOf((String)unemployed.getUserData("HIVE"))&&distance<2.24f){
                            float intensity = 1/(FastMath.sqrt(FastMath.sqr(distance)-4)+1);
                            unemployedControl.increaseForagerStimulus(intensity);
                        }
                    }  
                }
                foragerSignal.onSignalReceived(Signal.Type.FORAGER_REST, null);
                break;
            }
        }
    }
    
    private void spawnFood(){
        if(System.currentTimeMillis()-spawnTimeStart>20){
            spawnTimeStart = System.currentTimeMillis();
            if (foodNode.getQuantity() < 100 && new Random().nextInt(foodSpawnChance) == 0) {
                float x = FastMath.nextRandomFloat()*30-15;
                float y = FastMath.nextRandomFloat()*30-15;
                Vector3f location = new Vector3f(x, y, -2f);
                if(location.distance(hive1.getLocalTranslation())>2.5*FastMath.sqrt(2)
                   &&location.distance(hive2.getLocalTranslation())>2.5*FastMath.sqrt(2)
                   &&location.distance(hive3.getLocalTranslation())>2.5*FastMath.sqrt(2)
                   &&location.distance(hive4.getLocalTranslation())>2.5*FastMath.sqrt(2))
                    Food.loadFoad((int)(10*Math.random()), location);
            }
        }
    }
    
    private void initEnvironment() {
        Material mat = new Material(Main.myAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        
        Box box2 = new Box(0.01f, 0.5f*5, 0.25f);
        Box box1 = new Box(0.5f*5, 0.01f, 0.25f);
        Geometry geom1 = new Geometry("Boundary", box1);
        Geometry geom2 = new Geometry("Boundary", box2);
        geom1.setMaterial(mat);
        geom2.setMaterial(mat);
        
        hive1.attachChild(geom1);
        hive1.attachChild(geom2);
        
        geom1.setLocalTranslation(new Vector3f(0f, 2.5f, 0f));
        geom2.setLocalTranslation(new Vector3f(2.5f, 0f, 0f));
        
        Spatial s1 = geom1.clone();
        Spatial s2 = geom2.clone();
        
        hive1.attachChild(s1);
        hive1.attachChild(s2);
        
        s1.setLocalTranslation(new Vector3f(0f, -2.5f, 0f));
        s2.setLocalTranslation(new Vector3f(-2.5f, 0f, 0f));
        
        Box box = new Box(0.25f, 0.25f, 0.025f);
        Geometry store = new Geometry("Store", box);
        mat = new Material(Main.myAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        store.setMaterial(mat);
        
        hive1.attachChild(store);
        store.setLocalTranslation(-2.5f+0.25f, 2.5f-0.25f, 0);
        
        hive2 = hive1.clone(true);
        hive3 = hive1.clone(true);
        hive4 = hive1.clone(true);
        
        rootNode.attachChild(hive1);
        rootNode.attachChild(hive2);
        rootNode.attachChild(hive3);
        rootNode.attachChild(hive4);
        
        hive1.setLocalTranslation(new Vector3f(5f, 5f, -2f));
        hive2.setLocalTranslation(new Vector3f(-5f, 5f, -2f));
        hive3.setLocalTranslation(new Vector3f(-5f, -5f, -2f));
        hive4.setLocalTranslation(new Vector3f(5f, -5f, -2f));
        
        hive1.setUserData("Quantity", 1000f);
        hive2.setUserData("Quantity", 1000f);
        hive3.setUserData("Quantity", 1000f);
        hive4.setUserData("Quantity", 1000f);
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        
    }
}
