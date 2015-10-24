package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Manasi Deshmukh
 */
public class FoodControl extends AbstractControl{
    
    int value;

    public FoodControl(int value) {
        this.value = value;
    }
    
    public void decrementValue(){
        this.value = value-1;
    }
    
    public int getValue(){
        return this.value;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(value<=0){
            spatial.removeFromParent();
            spatial.removeControl(this);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
}
