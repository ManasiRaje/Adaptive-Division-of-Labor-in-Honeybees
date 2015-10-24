package mygame;

import com.jme3.scene.Spatial;

/**
 *
 * @author Manasi Deshmukh
 */
 
public interface Signal {
    public enum Type{
        UNEMPLOYED_BECOME_STORER,
        UNEMPLOYED_BECOME_FORAGER,
        FORAGER_FOOD_FOUND,
        FORAGER_STORER_ARRIVED,
        FORAGER_REST,
        STORER_FOOD_ARRIVED;
    }
    public void onSignalReceived(Type type, Spatial from);
}
