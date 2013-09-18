package eu.imagine.app;

import eu.imagine.framework.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/18/13
 * Time: 2:57 PM
 */
public class Tracking implements Entity {

    private int ID;
    private String object;

    public Tracking(int ID, String object) {
        this.ID = ID;
        this.object = object;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public String getObject() {
        return object;
    }

    @Override
    public boolean getVisibility() {
        return true;
    }
}
