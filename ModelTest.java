import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.atLeastOnce;

@RunWith(MockitoJUnitRunner.class)
public class ModelTest {

    // Create mocks that will be injected into the instance
    @Mock
    private Controller controller;
    @Mock
    private View view;
    @Mock
    private MediaPlayerWrapper playerGameMusic;
    @Mock
    private PlatformWrapper platformWrapper;
    @Mock
    private MediaPlayerWrapper playerGameOver;
    @Mock
    private MediaPlayerWrapper playerBrickMusic;
    @Mock
    private GameState gameState;
    @Mock
    private GameObj ball;
    @Mock
    private GameObj bat;
    @Mock
    private GameObj brick;

    /**
     * Instance for model is set to Width 600 and Height 900
     * The width must stay that way because
     * the position of bricks in the brick() method is hard coded.
     */
    @InjectMocks
    private Model instance = new Model(600, 100);

    /**
     * setup method to initialize Model's media players
     * when Mockito injects mocks it does it based on mocks class,
     * and does not take into consideration variable names
     * that's why explicit configuration was needed
     */
    @Before
    public void setUp() {
        instance.setPlayerBrickMusic(playerBrickMusic);
        instance.setPlayerGameMusic(playerGameMusic);
        instance.setPlayerGameOver(playerGameOver);
    }

    /**
     * Testing if lives displayed are equal to lives set
     */
    @Test
    public void shouldReturnThreeHeartsWhenUserHasThreeLives() {
        instance.setLives(3);
        Assert.assertEquals("♥ ♥ ♥", instance.getLives());
    }

    /**
     * Testing if the brick array is initialised
     */
    @Test
    public void shouldInitializeBricks() {
        GameObj[] bricks = instance.bricks(); //array of bricks is equal to 60
        // check if it's equal to 60
        Assert.assertEquals(60, bricks.length);
        for (GameObj brick : bricks) {
            Assert.assertNotNull(brick); //check if isn't null
            Assert.assertEquals(Model.BRICK_WIDTH - 1, brick.getWidth()); //check with of the brick
            Assert.assertEquals(Model.BRICK_HEIGHT, brick.getHeight()); //check height of the brick
        }
        // check position of 1st brick
        Assert.assertEquals(0, bricks[0].getTopX());
        Assert.assertEquals(170, bricks[0].getTopY());
        Assert.assertEquals(Color.BLUEVIOLET, bricks[0].getColour());
        // check second
        Assert.assertEquals(Model.BRICK_WIDTH, bricks[1].getTopX());
        Assert.assertEquals(170, bricks[1].getTopY());
        // check 12th brick
        Assert.assertEquals(0, bricks[12].getTopX());
        Assert.assertEquals(203, bricks[12].getTopY());
        Assert.assertEquals(Color.CYAN, bricks[12].getColour());
        // check 25th brick
        Assert.assertEquals(Color.BLUEVIOLET, bricks[25].getColour());
        Assert.assertEquals(236, bricks[25].getTopY());
    }

    /**
     * This test uses Mockito framework.<br>
     * Testing the game state. Should be set to 'finished' when all lives are lost
     */
    @Test
    public void shouldFinishGameWhenLivesCountEqualsZero() {
        // this line is to prevent NullPointerException in Model class in line 188
        // additionally we are mocking getGameState function to return "finished" state to break out of infinite loop
        Mockito.when(gameState.getGameState()).thenReturn("running", "finished");

        // we have to initialize bricks to non null value, otherwise NPE will be thrown from checkVisibility() method
        instance.setBricks(new GameObj[] {});
        instance.setLives(0);
        instance.setBrickCounter(3);
        instance.runGame();

        Mockito.verify(gameState, atLeastOnce()).setGameState("finished");
    }

    /**
     * This test uses Mockito framework.<br>
     * Testing the game state. Should be set to 'finished' when all bricks are gone
     */
    @Test
    public void shouldFinishGameWhenBricksCountEqualsZero() {
        // this line is to prevent NPE in Model class in line 188
        // additionally we are mocking getGameState function to return "finished" state to break out of infinite loop
        Mockito.when(gameState.getGameState()).thenReturn("running", "finished");

        instance.setBricks(new GameObj[] {}); // have to initialise bricks array to non null
        instance.setLives(3);
        instance.setBrickCounter(0);
        instance.runGame();

        Mockito.verify(gameState, atLeastOnce()).setGameState("finished");
    }

    /**
     * This test uses Mockito framework.<br>
     * Testing the game state and music player. <br>
     * Should be set to 'running' when 'isStart' boolean in controller is true<br>
     * Should set Media Player to play
     */
    @Test
    public void shouldSetGameStateToRunning() {
        Mockito.when(gameState.getGameState()).thenReturn("finished");
        Mockito.when(controller.isStart()).thenReturn(true);

        instance.runGame();

        Mockito.verify(gameState).setGameState("running");
        Mockito.verify(playerGameOver).play();
    }

    /**
     * This test uses Mockito framework.<br>
     * Testing the game state and music player. <br>
     * Should be set to 'login' when 'isStart' boolean in the controller class is false<br>
     * Should set Media Player to play game over music
     */
    @Test
    public void shouldSetGameStateToLogin() {
        // Given section - initial values
        Mockito.when(gameState.getGameState()).thenReturn("finished");
        Mockito.when(controller.isStart()).thenReturn(false);

        // When section - test method invocation
        instance.runGame();

        // Then section - testing results
        Mockito.verify(gameState).setGameState("login");
        Mockito.verify(playerGameOver).play();
    }

    /**
     * This test uses Mockito framework.<br>
     * Testing if bricks are gone when ball hits them <br>
     * Testing if brick music is played
     */
    @Test
    public void shouldHideTheBrickWhenHitByBall() {
        Mockito.when(brick.isVisible()).thenReturn(true);
        Mockito.when(brick.hitBy(ball)).thenReturn(true);
        instance.setBricks(new GameObj[] { brick });
        instance.setBrickCounter(1); // set amount of visible bricks just to 1

        instance.checkVisibility();

        Mockito.verify(brick).setVisible(false); //checking if after hitting brick is not visible
        Mockito.verify(playerBrickMusic).play(); //checking if brick sound is played
        Assert.assertEquals(0, instance.getBrickCounter()); //checking if after hitting that one visible brick the counter is  zero
        Assert.assertEquals(50, instance.getScore()); //checking if we get correct score after hitting the brick
    }
}