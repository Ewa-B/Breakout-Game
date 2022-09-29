
// An object in the game, represented as a rectangle, with a position,
// a size, a colour and a direction of movement.

// Watch out for the different spellings of Color/colour - the class uses American
// spelling, but we have chosen to use British spelling for the instance variable!

// import Athe JavaFX Color class
import javafx.scene.paint.Color;

/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.0
 */
public class GameObj
{
    // state variables for a game object
    //ENCAPSULATED FIELDS
    private boolean visible  = true;     // Can be seen on the screen (change to false when the brick gets hit)
    private int topX   = 0;              // Position - top left corner X
    private int topY   = 0;              // position - top left corner Y
    private int width  = 0;              // Width of object
    private int height = 0;              // Height of object
    private Color colour;                // Colour of object
    private int   dirX   = 1;            // Direction X (1, 0 or -1)
    private int   dirY   = 1;            // Direction Y (1, 0 or -1)


    public GameObj( int x, int y, int w, int h, Color c )
    {
        setTopX(x);
        setTopY(y);
        setWidth(w);
        setHeight(h);
        setColour(c);
    }

    // move in x axis
    public void moveX( int units )
    {
        setTopX(getTopX() + units * getDirX());
    }

    // move in y axis
    public void moveY( int units )
    {
        setTopY(getTopY() + units * getDirY());
    }


    // change direction of movement in x axis (-1, 0 or +1)
    public void changeDirectionX()
    {
        setDirX(-getDirX());
    }

    // change direction of movement in y axis (-1, 0 or +1)
    public void changeDirectionY()
    {
        setDirY(-getDirY());
    }


    /**
     * Detect collision between this object and the argument object
     * It's easiest to work out if they do NOT overlap
     * @param obj GameObj object passed to the method
     * @return  return boolean - the opposite
     */
    public boolean hitBy( GameObj obj )
    {
        boolean separate =  
            getTopX() >= obj.getTopX() + obj.getWidth() ||    // '||' means 'or'
            getTopX() + getWidth() <= obj.getTopX() ||
            getTopY() >= obj.getTopY() + obj.getHeight() ||
            getTopY() + getHeight() <= obj.getTopY();
        
        // use ! to return the opposite result - hitBy is 'not separate')
        return(! separate);  
          
    }

    /**
     * Checks if the ball hits the edges of the bat and
     * changes direction of the ball so it doesn't run along the bat
     * @return true if edge, false if not
     * @param ob ball object passed to the method
     */
    public boolean batEdge(GameObj ob){
        int x = (topX+width/2) - ob.topX - (ob.width / 2);
        System.out.println("position: " + x);
        if(x > 80 || x < -80){
            changeDirectionY();
            return true;
        }
        return false;
    }


    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getTopX() {
        return topX;
    }

    public void setTopX(int topX) {
        this.topX = topX;
    }

    public int getTopY() {
        return topY;
    }

    public void setTopY(int topY) {
        this.topY = topY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public int getDirX() {
        return dirX;
    }

    public void setDirX(int dirX) {
        this.dirX = dirX;
    }

    public int getDirY() {
        return dirY;
    }

    public void setDirY(int dirY) {
        this.dirY = dirY;
    }
}
