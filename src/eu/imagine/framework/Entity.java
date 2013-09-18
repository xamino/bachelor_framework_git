package eu.imagine.framework;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/18/13
 * Time: 2:40 PM
 */
public interface Entity extends OpenGLDraw{
    public int getID();
    public OpenGLDraw getDraw();
    public boolean getVisibility();
}
