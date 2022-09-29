import javafx.scene.paint.Color;

import java.util.Random;

/**
 * <h2>Different colour of the ball</h2>
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.0
 */
public class Ball extends GameObj{

    public Ball(int x, int y, int w, int h, Color c) {
        super(x, y, w, h, c);
    }

    /**
     * Changing color of the ball every time new game is played
     * @return random colour
     */
    private Color randomColour(){
   String x = Integer.toString(new Random().nextInt(255));
   String y = Integer.toString(new Random().nextInt(255));
   String z = Integer.toString(new Random().nextInt(255));
//   String col = "rgb("+x+","+z+","+y+")";
//    System.out.println(col);
        return Color.web("rgb("+x+","+z+","+y+")");
    }

    @Override
    public void setColour(Color colour) {
        super.setColour(randomColour());
    }

}
