import javafx.animation.PathTransition;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 *
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.0
 */
//This supposed to be LevelTwo class displayed after player finishes LevelOne
// originally Model didn't have falling bricks
//I didn't know how to connect it to the project in the end, so It was used as a main game instead
public class LevelTwo  extends Model{

    private GameObj[] bricks2;
    private int fallingBrickIdx;

    public LevelTwo(int w, int h) {
        super(w, h);
    }

    @Override
    public void setBricks(GameObj[] bricks2) {
        super.setBricks(bricks2);
    }

    @Override
    public GameObj[] bricks() {
        int x = 0;
        int y = 170;
        int numBricks = (getWidth()/getBRICK_WIDTH()) * 5;
        int oneLine = numBricks/5;
        bricks2 = new GameObj[numBricks];
        for (int i = 0; i < numBricks; i++){
            bricks2[i] = new GameObj(x, y, getBRICK_WIDTH() - 1, getBRICK_HEIGHT(), Color.BLUEVIOLET);
            if((i > oneLine-1 && i < oneLine*2) || (i > oneLine*3-1 && i < oneLine*4)){
                bricks2[i].setColour(Color.CYAN);
            }
            x += getBRICK_WIDTH();
            if(i == 11 || i == 23 || i == 35 || i == 47){
                x = 0;
                y += getBRICK_HEIGHT()+ 3;
            }
        }
        return bricks2;
    }

    @Override
    public void initialiseGame() {
        super.initialiseGame();
        fallingBrickIdx = new Random().nextInt(bricks2.length);
    }

    @Override
    public void updateGame() {
        GameObj brick = bricks2[fallingBrickIdx];
        if (!brick.isVisible()) {
            findNewFallingBrick();
        } else {
            brick.moveY(getBALL_MOVE() - 2);
            int y = brick.getTopY();
            int bottom = getHeight() - getB() - Model.BRICK_HEIGHT;
            if (y == bottom) {
                setBrickCounter(getBrickCounter() - 1);
                brick.setVisible(false);
            }
        }
        super.updateGame();
    }
    private void findNewFallingBrick() {
        if (getBrickCounter() > 0) {
            do {
                fallingBrickIdx = new Random().nextInt(bricks2.length);
            } while (!bricks2[fallingBrickIdx].isVisible());
        }
    }
}
