package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.Random;

/**
 *
 * @author Manasi Deshmukh
 */
public class AnimatControl extends AbstractControl implements Signal{

    private Main.Hive hive;
    private float directionAngle;
    private Vector3f velocity;
    private Vector3f hiveLocation;
    private Vector3f location;
    private float tpf;
    float turnSpeed;
    float speed;
    long waitTimeStart;
    long waitTime;
    Animat.Type type;
    Animat.State state;
    Vector3f destination;
    Vector3f last_destination;
    Spatial to;
    Animat.State last_state;
    float crop = 0;
    float s_forager = 0;
    float t_forager = 0.1f;
    float s_storer = 0;
    float t_storer = 0.1f;
    float n = 2;
    
    public AnimatControl(Animat.Type type, Animat.State state, Vector3f hiveLocation, Main.Hive hive){
        setType(type);
        this.velocity = new Vector3f();
        this.directionAngle = (float) (Math.random() * FastMath.PI * 2f);
        this.state = state;
        this.hiveLocation = hiveLocation;
        this.hive = hive;
        crop = FastMath.nextRandomFloat();
    }
    
    @Override
    protected void controlUpdate(float tpf){
        this.location = spatial.getLocalTranslation();
        this.tpf = tpf;
        if(crop<=0){
            spatial.removeFromParent();
            spatial.removeControl(this);
            return;
        }
        else if(crop <= 0.05 && state!= Animat.State.REFILL){
            last_state = state;
            last_destination = destination;
            destination = hiveLocation.add(-2.5f+0.25f, 2.5f-0.25f, 0);
            setState(Animat.State.REFILL);
        }
        switch(type){
            case UNEMPLOYED :
                crop = crop - tpf/400;
                switch(state){
                    case REFILL :
                        walk();
                        if(onDestinationReached(null, null))
                            refill();
                        break;
                    case ROAM :
                        roamInHive(null);
                        break;
                }
                break;
            case FORAGER:
                crop = crop - tpf/200;
                switch(state){
                    case SEARCH_FOOD :
                        if(Math.abs(location.z)>0.0005){
                            destination = location.clone().setZ(0);
                            walk();
                            break;
                        }
                        randomWalk(false, null, 0);
                        break;
                    case COLLECT :
                        if(to.getControl(FoodControl.class)==null){
                            destination = location.clone().setZ(0);
                            walk();
                            onDestinationReached(Animat.State.SEARCH_FOOD, null);
                            break;
                        }
                        walk();
                        if(onDestinationReached(Animat.State.RETURN, hiveLocation.add(1.25f+(FastMath.nextRandomFloat()-0.5f)/3, -1.25f+(FastMath.nextRandomFloat()-0.5f)/3f, 2))){
                            foodObtained(1-crop);
                            to.getControl(FoodControl.class).decrementValue();
                        }
                        break;
                    case RETURN :
                        walk();
                        if(onDestinationReached(Animat.State.WAIT_FOR_STORER, null))
                            waitTimeStart = System.currentTimeMillis();
                        break;
                    case WAIT_FOR_STORER :
                        waitTime = System.currentTimeMillis() - waitTimeStart;
                        if(crop<=0.25){
                            setSpeed(0.85f);
                            setState(Animat.State.SEARCH_FOOD);
                            changeColor(Animat.Type.getColor(type));
                        }
                        break;
                    case UNLOAD :
                        break;
                    case REST :
                        if(FastMath.nextRandomFloat()<0.00001){
                            setType(Animat.Type.UNEMPLOYED);
                            setState(Animat.State.ROAM);
                            changeColor(Animat.Type.getColor(this.type));
                            Main.foragerNode.detachChild(spatial);
                            Main.unemployedNode.attachChild(spatial);
                            break;
                        }
                        roamInHive(null);
                        if(crop<=0.15f)
                            setState(Animat.State.SEARCH_FOOD);
                        break;
                    case REFILL :
                        walk();
                        if(onDestinationReached(null, null))
                            refill();
                        break;
                }
                break;
            case STORER:
                crop = crop - tpf/300;
                switch(state){
                    case REFILL :
                        walk();
                        if(onDestinationReached(null, null))
                            refill();
                        break;
                    case SEARCH_FORAGER :
                        if(FastMath.nextRandomFloat()<0.00001){
                            setType(Animat.Type.UNEMPLOYED);
                            setState(Animat.State.ROAM);
                            changeColor(Animat.Type.getColor(this.type));
                            Main.storerNode.detachChild(spatial);
                            Main.unemployedNode.attachChild(spatial);
                            break;
                        }
                        roamInHive(hiveLocation.add(1.25f, -1.25f, 0));
                        break;
                    case LOAD :
                        AnimatControl animatControl = to.getControl(AnimatControl.class);
                        if(animatControl==null||animatControl.getState()!=Animat.State.WAIT_FOR_STORER) {
                            setState(Animat.State.SEARCH_FORAGER);
                            to = null;
                            break;
                        }
                        walk();
                        if(onDestinationReached(null, hiveLocation.add(-2.5f+0.25f, 2.5f-0.25f, 0))){
                            float take = Math.min(animatControl.getCrop()-0.25f, 0.8f-crop);
                            foodObtained(take);
                            animatControl.setCrop(animatControl.getCrop()-take);
                            setSpeed(0.5f);
                            ((Signal)animatControl).onSignalReceived(Type.FORAGER_STORER_ARRIVED, null);
                            if(crop>0.8)
                                setState(Animat.State.STORE);
                            else
                                setState(Animat.State.SEARCH_FORAGER);
                        }
                        break;
                    case STORE :
                        walk();
                        if(onDestinationReached(Animat.State.SEARCH_FORAGER, null)){
                            setSpeed(0.5f);
                            foodDeposited();
                        }
                        break;
                }
                break;
        }
    }
    
    private void refill(){
        Node hiveNode = Main.Hive.getHiveNode(hive);
        float quantity = ((Float) hiveNode.getUserData("Quantity")).floatValue();
        if(quantity>1-crop){
            quantity = quantity - (1-crop);
            crop = 1;
        }
        else if(quantity>0){
            crop = crop + quantity;
            quantity = 0;
        }
        hiveNode.setUserData("Quantity", quantity);
        setState(last_state);
        destination = last_destination;
        last_destination = null;
    }
    
    private boolean onDestinationReached(Animat.State state, Vector3f newDestination){
        if(location.distance(destination)<0.0005){
            setState(state);
            destination = newDestination;
            return true;
        }
        return false;
    }
    
    public void onSignalReceived(Signal.Type type, Spatial from) {
        switch(type){
            case FORAGER_REST :
                setState(Animat.State.REST);
                break;
            case STORER_FOOD_ARRIVED :
                setSpeed(0.9f);
                setState(Animat.State.LOAD);
                this.destination = from.getLocalTranslation();
                this.to = from;
                break;
            case FORAGER_FOOD_FOUND :    
                setState(Animat.State.COLLECT);
                this.destination = from.getLocalTranslation();
                this.to = from;
                break;
            case FORAGER_STORER_ARRIVED :
                if(crop<=0.25f){
                    setState(Animat.State.UNLOAD);
                    changeColor(Animat.Type.getColor(this.type));
                    setSpeed(speed*4/3);
                }
                break;
            case UNEMPLOYED_BECOME_STORER :
                float prob1 = FastMath.pow(s_storer, n)/(FastMath.pow(s_storer, n)+FastMath.pow(t_storer, n));
                if(FastMath.nextRandomFloat()<=prob1) {
                    if(t_storer>=0.01f)
                        t_storer = t_storer - 0.01f;
                    setType(Animat.Type.STORER);
                    setState(Animat.State.SEARCH_FORAGER);
                    changeColor(Animat.Type.getColor(this.type));
                    Main.unemployedNode.detachChild(spatial);
                    Main.storerNode.attachChild(spatial);
                }
                else if(t_storer<=0.999f)
                    t_storer = t_storer + 0.001f;
                break;
            case UNEMPLOYED_BECOME_FORAGER :
                float prob2 = FastMath.pow(s_forager, n)/(FastMath.pow(s_forager, n)+FastMath.pow(t_forager, n));
                if(FastMath.nextRandomFloat()<=prob2) {
                    if(t_forager>=0.01f)
                        t_forager = t_forager - 0.01f;
                    setType(Animat.Type.FORAGER);
                    setState(Animat.State.SEARCH_FOOD);
                    changeColor(Animat.Type.getColor(this.type));
                    Main.unemployedNode.detachChild(spatial);
                    Main.foragerNode.attachChild(spatial);
                }
                else if(t_forager<=0.999f)
                    t_forager = t_forager + 0.001f;
                break;
        }
    }
    
    public void roamInHive(Vector3f bias){
        if(Math.abs(location.z+2)>0.0005){
            setSpeed(0.9f);
            destination = location.clone().setZ(-2);
            walk();
            return;
        }
        setSpeed(Animat.Type.getSpeed(type));
        if(bias == null)
            randomWalk(true, hiveLocation, 2.5f);
        else
            randomWalkWithBias(true, hiveLocation, 2.5f, bias);
    }
    
    public void walk(){
        Vector3f direction = destination.subtract(location).normalize();
        direction.multLocal(1000f);
        velocity.addLocal(direction);
        velocity.multLocal(speed);
        spatial.move(velocity.mult(tpf*0.0005f));
    }
    
    public void randomWalk(boolean check, Vector3f center, float maxDistance){
        directionAngle += (float) ((Math.random() * turnSpeed - turnSpeed/2) * tpf);
        Vector3f directionVector = new Vector3f(FastMath.cos(directionAngle), FastMath.sin(directionAngle), 0);
        directionVector.multLocal(1000f);
        velocity.addLocal(directionVector);
        velocity.multLocal(speed);
        if(check)
            checkBounds(center, maxDistance);
        spatial.move(velocity.mult(tpf*0.0005f));
    }
    
    public void randomWalkWithBias(boolean check, Vector3f center, float maxDistance, Vector3f bias){
        float n =  (float) new Random().nextGaussian();
        Vector3f directionVector;
        if(FastMath.abs(n)<0.003){
            directionVector = bias.subtract(location).normalize();
            directionAngle = FastMath.atan2(directionVector.y, directionVector.x);
        }
        else{
            directionAngle += (float) ((Math.random() * turnSpeed - turnSpeed/2) * tpf);
            directionVector = new Vector3f(FastMath.cos(directionAngle), FastMath.sin(directionAngle), 0);
        }
         
        directionVector.multLocal(1000f);
        velocity.addLocal(directionVector);
        velocity.multLocal(speed);
        if(check)
            checkBounds(center, maxDistance);
        spatial.move(velocity.mult(tpf*0.0005f));
    }
    
    public void checkBounds(Vector3f center, float maxDistance){
        if(Math.abs(location.x-center.x)>maxDistance){
            directionAngle = FastMath.PI - directionAngle;
            velocity.multLocal(-Math.signum(velocity.x)*Math.signum(location.x-center.x), 1, 1);
            spatial.setLocalTranslation(new Vector3f(Math.signum(location.x-center.x)*maxDistance+center.x, location.y, location.z));
        }
        if(Math.abs(location.y-center.y)>maxDistance){
            directionAngle = -directionAngle;
            velocity.multLocal(1, -Math.signum(velocity.y)*Math.signum(location.y-center.y), 1);
            spatial.setLocalTranslation(new Vector3f(location.x,Math.signum(location.y-center.y)*maxDistance+center.y , location.z));
        }
    }
    
    private void foodObtained(float taken){
        crop = crop + taken;
        setSpeed(speed * 3/4);
        if(crop>=0.8)
            changeColor(ColorRGBA.Red);
    }
    
    private void foodDeposited(){
        setSpeed(speed * 4/3);
        float deposit = crop - 0.25f;
        crop = 0.25f;
        Node hiveNode = Main.Hive.getHiveNode(hive);
        float quantity = ((Float) hiveNode.getUserData("Quantity")).floatValue();
        quantity = quantity+deposit;
        hiveNode.setUserData("Quantity", quantity);
        changeColor(Animat.Type.getColor(type));
    }
    
    private void changeColor(ColorRGBA colorRGBA){
        Geometry geom = (Geometry) spatial;
        Material mat = new Material(Main.myAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", colorRGBA);
        geom.setMaterial(mat);
    }
    
    public void setType(Animat.Type type){
        this.type = type;
        setSpeed(Animat.Type.getSpeed(type));
        switch(type){
            case FORAGER :
                this.turnSpeed = 150f;
                break;
            case STORER :
                this.turnSpeed = 70f;
                break;
            case UNEMPLOYED :
                this.turnSpeed = 60f;
                break;
        }
    }
    
    public void resetForagerStimulus(){
        s_forager = 0;
    }
    
    public void increaseForagerStimulus(float intensity){
        s_forager = s_forager + intensity;
    }
    
    public void resetStorerStimulus(){
        s_storer = 0;
    }
    
    public void increaseStorerStimulus(float intensity){
        s_storer = s_storer + intensity;
    }
    
    public float getForagerStimulus(){
        return this.s_forager;
    }
    
    public float getStorerStimulus(){
        return this.s_storer;
    }
    
    public void setState(Animat.State state){
        this.state = state;
    }
    
    public Animat.State getState(){
        return this.state;
    }
    
    public float getCrop(){
        return this.crop;
    }
    
    public void setCrop(float crop){
        this.crop = crop;
    }
    
    public void resetWaitTime(){
        this.waitTimeStart = System.currentTimeMillis();
    }
    
    public long getWaitTime(){
        return waitTime;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
