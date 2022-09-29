

// The model represents all the actual content and functionality of the game
// For Breakout, it manages all the game objects that the View needs
// (the bat, ball, bricks, and the score), provides methods to allow the Controller
// to move the bat (and a couple of other functions - change the speed or stop 
// the game), and runs a background process (a 'thread') that moves the ball 
// every 20 milliseconds and checks for collisions 


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.*;
import javafx.application.Platform;

import java.nio.file.Paths;
import java.util.Random;

/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 2.0
 */
public class Model 
{
    // First,a collection of useful values for calculating sizes and layouts etc.
    //ENCAPSULATION
    private final int B              = 6;      // Border round the edge of the panel
    private  final int M              = 40;     // Height of menu bar space at the top

    private final int BALL_SIZE      = 30;     // Ball side
    public static final int BRICK_WIDTH    = 50;     // Brick size
    public static final int BRICK_HEIGHT   = 30;      //Brick height

    private final int BAT_MOVE       = 5;      // Distance to move bat on each keypress
    private int BALL_MOVE      = 3;      // Units to move the ball on each step

    private final int HIT_BRICK      = 50;     // Score for hitting a brick
    private final int HIT_BOTTOM     = -200;   // Score (penalty) for hitting the bottom of the screen
    private final int REMOVE_LIVE = -1; //live (penalty) the ball misses the bat
    private int brickCounter = 0; //COUNT THE VISIBLE BRICKS LEFT
    private boolean hit; // check for brick being hit by the call

    //Media and Media player fields for music/sound effects
     Media mediaGameMusic, mediaBrickMusic,mediaGameOver;
     MediaPlayerWrapper playerGameMusic, playerBrickMusic, playerGameOver;
    private PlatformWrapper platformWrapper = new PlatformWrapper();

    // The other parts of the model-view-controller setup
    View view;
    Controller controller;

    // The game 'model' - these represent the state of the game
    // and are used by the View to display it
    private GameObj ball;
    private GameObj[] bricks;
    private GameObj bat;

    private int score = 0;
    private int lives = 0;

    //
    private boolean pausedPlayer = false;

    // variables that control the game 
    //private String gameState = "running";// Set to "finished" to end the game
    private GameState gameState = new GameState();

    //public static void constants are defined to reduce code duplication and are good for maintenance
    public static final String PAUSED_GAME_STATE = "paused";
    public static final String RUNNING_GAME_STATE = "running";
    public static final String LOGIN_GAME_STATE = "login";
    public static final String FINISHED_GAME_STATE = "finished";

    private boolean fast = false;        // Set true to make the ball go faster

    private int fallingBrickIdx;
    // boolean variables for fixing the ball running along the bat issue
    private boolean lastHitByBat = false;
    private boolean lastHitByBottom = false;


    // initialisation parameters for the model
    private int width;                   // Width of game
    private int height;                  // Height of game

    // CONSTRUCTOR - needs to know how big the window will be
    public Model( int w, int h )
    {
        Debug.trace("Model::<constructor>");  
        width = w; 
        height = h;
    }

    /**
     * Start the animation thread
     */
    public void startGame()
    {

        initialiseGame();                           // set the initial game state
        Thread t = new Thread( this::runGame );     // create a thread running the runGame method
        t.setDaemon(true);                          // Tell system this thread can die when it finishes
        t.start();                                  // Start the thread running
    }

    /**
     * This method is initialised in the Main class and contains String variables with paths to the music/sound effects.<br>
     * It initialises <strong>Media</strong> class that represents a media source and <strong>MediaPlayer</strong> classes that controls and displays media playback.
     */
    public void initMedia(){
        String gameMusic = "breakout-music.mp3";
        String brickMusic = "breakout-expl.mp3";
        String gameOverMusic = "game-over.mp3";

        mediaGameMusic = new Media(Paths.get(gameMusic).toUri().toString());
        playerGameMusic = new MediaPlayerWrapper(mediaGameMusic);

        mediaBrickMusic = new Media(Paths.get(brickMusic).toUri().toString());
        playerBrickMusic = new MediaPlayerWrapper(mediaBrickMusic);

        mediaGameOver = new Media(Paths.get(gameOverMusic).toUri().toString());
        playerGameOver = new MediaPlayerWrapper(mediaGameOver);
    }


    // Initialise the game - reset the score and create the game objects
    public void initialiseGame()
    {
        score = 0;
        lives = 3;
        ball   = new Ball(width/2, height/2, BALL_SIZE, BALL_SIZE, Color.RED);
        bat    = new GameObj(width/2, height - BRICK_HEIGHT*3/2 +20 , BRICK_WIDTH*3,
                BRICK_HEIGHT/4, Color.GRAY);

        bricks = bricks();     //INITIALISE BRICKS INTO THE ARRAY
        brickCounter = bricks.length;   //Set the brick counter to the length on and bricks array
        playerGameMusic.play();    //start the music

        // find random brick that will fall down
        fallingBrickIdx = new Random().nextInt(bricks.length);
    }

    /**
     * Method initialising and returning an array of bricks, that will be displayed on the screen
     * @return array of GameObj (bricks)
     */
    public GameObj[] bricks (){
        int x = 0;
        int y = 170;
        int numBricks = (getWidth()/getBRICK_WIDTH()) *5; //length of the array
        int oneLine = numBricks/5;  //single line
        bricks = new GameObj[numBricks];
        for (int i = 0; i < numBricks; i++){
            bricks[i] = new GameObj(x, y, getBRICK_WIDTH() - 1, getBRICK_HEIGHT(), Color.BLUEVIOLET);
            if((i > oneLine-1 && i < oneLine*2) || (i > oneLine*3-1 && i < oneLine*4)){
                bricks[i].setColour(Color.CYAN); // rotate the colours of bricks
              //  System.out.println("position x: " + x);
            }
            x += getBRICK_WIDTH();
            if(i == 11 || i == 23 || i == 35 || i == 47){
                x = 0; // go to new line
                y += getBRICK_HEIGHT()+ 3; //move down
               // System.out.println("position Y : " + y);
            }
        }
        return bricks;
    }

    /**
     * @return Returns amount of units the ball is moving bt each step
     */
    public int getBALL_MOVE() {
        return BALL_MOVE;
    }

    public void runGame()
    {
        try
        {
            Debug.trace("Model::runGame: Game starting");
            boolean flag = controller.isStart();
            if(flag){
                setGameState(RUNNING_GAME_STATE);
            }else{
                setGameState(LOGIN_GAME_STATE);
            }
//            System.out.println("RunGame state::::" + getGameState());
            String currentGameState;
            do
            {
                currentGameState = getGameState();
                if(currentGameState.equals(LOGIN_GAME_STATE)){
                    playerGameMusic.stop();
                }

                if(getLives().equals("0")|| getBrickCounter() == 0){
                    setGameState(FINISHED_GAME_STATE);
                    modelChanged();
                }
                if(currentGameState.equals(RUNNING_GAME_STATE)){
                    playerGameMusic.play();
                    updateGame();                        // update the game state
                    modelChanged();                      // Model changed - refresh screen
                    Thread.sleep( getFast() ? 10 : 20 ); // wait a few milliseconds
                    if(pausedPlayer){
                        playerGameMusic.play();
                    }
                }
                if (currentGameState.equals(PAUSED_GAME_STATE)){
                    pausedPlayer = true;
                    playerGameMusic.pause();
                }
            } while (!currentGameState.equals(FINISHED_GAME_STATE));
            playerGameOver.play();
            Debug.trace("Model::runGame: Game finished");
        } catch (Exception e)
        {
             /* There is an exception thrown when user closes the game
             *  I'm nor sure but I think it may be related to multi threading,
             *  there is runGame thread which is trying to interact with mediaPlayers
             *  while application is terminating*/
           // e.printStackTrace();
            Debug.error("Model::runAsSeparateThread error: " + e.getMessage() );
           // throw new RuntimeException(e);
        }
    }

    /**
     * Method updating the game - this happens about 50 times a second to give the impression of movement
     */
    public synchronized void updateGame()
    {

        //falling brick variable
        GameObj fallingBrick = bricks[fallingBrickIdx];

        // move the ball one step (the ball knows which direction it is moving in)
        ball.moveX(BALL_MOVE);                      
        ball.moveY(BALL_MOVE);
        // get the current ball position (top left corner)
        int x = ball.getTopX();
        int y = ball.getTopY();
        // Deal with possible edge of board hit
        if (x >= width - B - BALL_SIZE){
            lastHitByBat = false;
            lastHitByBottom = false;
            ball.changeDirectionX();
        }

        if (x <= B)  ball.changeDirectionX();
        if (y >= height - B - BALL_SIZE)  // Bottom
        {
            lastHitByBat = false;
            lastHitByBottom = true;
            ball.changeDirectionY();
            addToScore( HIT_BOTTOM );// score penalty for hitting the bottom of the screen
            removeLive(REMOVE_LIVE);
            //STOP MEDIA PLAYER TO RESTART THE SOUND OF BALL HITTING THE BRICK
            playerBrickMusic.stop();
        }
        if (y <= M)  {
            lastHitByBat = false;
            lastHitByBottom = false;
            ball.changeDirectionY();
        }

       // check whether ball has hit a (visible) brick
        hit = checkVisibility();


        if (hit) {
            lastHitByBat = false;
            lastHitByBottom = false;
            if(fallingBrick.hitBy(ball)){
                addToScore(200); //add additional points when ball hits the falling brick

            }
            ball.changeDirectionY();
        }

        // check whether ball has hit the bat
        if ( ball.hitBy(bat)  && !lastHitByBat && !lastHitByBottom) {
            lastHitByBat = true;
            //STOP THE PLAYER
            playerBrickMusic.stop();


            //change direction if ball hits the edge of the bat
            if (!ball.batEdge(bat)) {
                ball.changeDirectionY();
            }

        }
        // look for random brick when one is gone
        if (!fallingBrick.isVisible()) {
            findNewFallingBrick();
        } else {
            //move falling brick slower than the ball
            fallingBrick.moveY(BALL_MOVE - 2);
            int temp = fallingBrick.getTopY(); //position of the falling brick
            int bottom = getHeight() - B - BRICK_HEIGHT; //bottom of the screen
            if (temp == bottom) {
                //remove falling brick when reaches the bottom and make it not visible
                setBrickCounter(getBrickCounter() - 1);
                fallingBrick.setVisible(false);
            }
        }
    }

    /**
     * Finds the falling bricks until there are bricks displayed <br>
     * Gets new falling brick when the previous one is gone
     */
    private void findNewFallingBrick() {
        if (getBrickCounter() > 0) {
            do {
                fallingBrickIdx = new Random().nextInt(bricks.length);
            } while (!bricks[fallingBrickIdx].isVisible());
        }
    }

    /**
     * Method to check visibility of the brick and change it, when hit by the ball
     * @return boolean 'hit'
     */
    public boolean checkVisibility(){
       hit = false;
        for (GameObj brick : bricks){
            if (brick.isVisible() && brick.hitBy(ball)){
                brick.setVisible(false);
                hit = true;
                //PLAY THE SOUND WHEN BALL HITS THE BRICK
                playerBrickMusic.play();
                brickCounter--;
                addToScore(HIT_BRICK);
                break;
            }
        }
        return hit;
    }

    public int getBrickCounter() {
        return brickCounter;
    }

    // This is how the Model talks to the View
    // Whenever the Model changes, this method calls the update method in
    // the View. It needs to run in the JavaFX event thread, and Platform.runLater 
    // is a utility that makes sure this happens even if called from the
    // runGame thread
    public synchronized void modelChanged()
    {
        // during tests Platform.runLater throws exception because javafx is not initialized
        // thats why we are using wrapper class
//        Platform.runLater(view::update);
        platformWrapper.runLater(view::update);
    }
    
    
    // Methods for accessing and updating values
    // these are all synchronized so that the can be called by the main thread 
    // or the animation thread safely
    
    // Change game state - set to "running" or "finished"
    public synchronized void setGameState(String value)
    {
        gameState.setGameState(value);
    }
    
    // Return game running state
    public synchronized String getGameState()
    {

        return gameState.getGameState();
    }

    // Change game speed - false is normal speed, true is fast
    public synchronized void setFast(Boolean value)
    {  
        fast = value;
    }
    
    // Return game speed - false is normal speed, true is fast
    public synchronized Boolean getFast()
    {  
        return(fast);
    }

    // Return bat object
    public synchronized GameObj getBat()
    {
        return(bat);
    }
    
    // return ball object
    public synchronized GameObj getBall()
    {
        return(ball);
    }
    
    // return bricks
    public synchronized GameObj[] getBricks()
    {
        return(bricks);
    }
    public void setBricks(GameObj[] bricks) {
        this.bricks = bricks;
    }
    
    // return score
    public synchronized int getScore()
    {
        return(score);
    }
    
     // update the score
    public synchronized void addToScore(int n)    
    {
        score += n;        
    }

    public void removeLive(int n){lives += n;}
    
    // move the bat one step - -1 is left, +1 is right
    public synchronized void moveBat( int direction )
    {

        int dist = direction * BAT_MOVE;    // Actual distance to move
        Debug.trace( "Model::moveBat: Move bat = " + dist );
        bat.moveX(dist);

    }

    public String getLives() {
        if (this.lives == 3) {
            return "♥ ♥ ♥";
        }
        if (this.lives == 2) {
            return "♥ ♥";
        }
            if (this.lives == 1) {
                return "♥";
            }

        return "0";
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
    public MediaPlayer getPlayerGameMusic() {
        return playerGameMusic.getMediaPlayer();
    }

    public static String getPausedGameState() {
        return PAUSED_GAME_STATE;
    }
    public MediaPlayer getPlayerGameOver() {
        return playerGameOver.getMediaPlayer();
    }
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    public int getB() {
        return B;
    }
    public int getBRICK_WIDTH() {
        return BRICK_WIDTH;
    }
    public int getBRICK_HEIGHT() {
        return BRICK_HEIGHT;
    }
    public int getM() {
        return M;
    }
    public void setBrickCounter(int brickCounter) {
        this.brickCounter = brickCounter;
    }

    public void setScore(int score) { this.score = score; }

    public boolean isHit() {return hit; }

    public void setHit(boolean hit) {this.hit = hit; }

    public void setBALL_MOVE(int BALL_MOVE) {
        this.BALL_MOVE = BALL_MOVE;
    }

    public boolean isFast() {
        return fast;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }

    public int getBALL_SIZE() {
        return BALL_SIZE;
    }

    public int getREMOVE_LIVE() {
        return REMOVE_LIVE;
    }

    public int getHIT_BOTTOM() { return HIT_BOTTOM; }

    public void setPlayerGameMusic(MediaPlayerWrapper playerGameMusic) {
        this.playerGameMusic = playerGameMusic;
    }

    public void setPlayerBrickMusic(MediaPlayerWrapper playerBrickMusic) {
        this.playerBrickMusic = playerBrickMusic;
    }

    public void setPlayerGameOver(MediaPlayerWrapper playerGameOver) {
        this.playerGameOver = playerGameOver;
    }

}
    