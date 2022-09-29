

// The View class creates and manages the GUI for the application.
// It doesn't know anything about the game itself, it just displays
// the current state of the Model, and handles user input

// We import lots of JavaFX libraries (we may not use them all, but it
// saves us having to think about them if we add new code)

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.*;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.2
 */

public class View implements EventHandler<KeyEvent>
{

    // variables for components of the user interface
    private int width;       // width of window
    private int height;      // height of window

    // user interface objects
    private Pane pane;       // basic layout pane
    private Label infoText;  // score info at top of screen
    private Label  livesText; // lives info at top of the screen
    private Label userName, highestScore;     //labels for user name and highest score
    private Button pauseButton, startButton, loginButton;

    //PANE GOR LOGGING IN
    private Pane loginPane;
    private Label loginLabel;
    private TextField loginTF;

    private String oldScore = ""; // temporary variable to save the old score that will be compared with new score


    // The other parts of the model-view-controller setup
    private Controller controller;


    private Model model;
    public Model getModel() { return model; }
    public void setModel(Model model) { this.model = model; }


    // Data we access from the Model
    private GameObj   bat;            // The bat
    private GameObj   ball;           // The ball
    private GameObj[] bricks;         // The bricks
    private int       score =  0;     // The score
    private String lives = " ";


    //hashmap containing players names and scores
    private HashMap<String, String> playerScores = new HashMap<>();

    // For each GameObj instance we get from the model, we create a Rectangle object
    // to display on the screen. We use a HashMap to store these Rectangle objects
    // so we can use the same one each time
    private HashMap<GameObj, Rectangle> rectangleStore = new HashMap<>();
   
    // constructor method - we get told the width and height of the window
    public View(int w, int h)
    {
        Debug.trace("View::<constructor>");
        setWidth(w);
        setHeight(h);
    }

    // start is called from the Main class, to start the GUI up
    
    public void start(Stage window) 
    {

        // breakout is basically one big drawing canvas, and all the objects are
        // drawn on it as rectangles, except for the text at the top - this
        // is a label which sits 'in front of' the canvas.
        
        // Note that it is important to create control objects (Pane, Label etc) 
        // here not in the constructor (or as initialisations to instance variables),
        // to make sure everything is initialised in the right order
        setPane(new Pane());       // a simple layout pane
        getPane().setId("Breakout");  // Id to use in CSS file to style the pane if needed



        // infoText box for the score - a label which we position at the top of the Pane
        // to show the current score
        setInfoText(new Label("BreakOut: Score = " + getScore()));
        getInfoText().setTranslateX(20);  // these commands set the position of the text box
        getInfoText().setTranslateY(10);  // (measuring from the top left corner)
        infoText.setId("infoText");

        //INITIALIZING LABEL FOR LIVES, SETTING ID, AND POSITION
        setLivesText(new Label("Lives: " + getLives()));
        getLivesText().setTranslateX(50); //label  x position
        getLivesText().setTranslateY(70); //label y position
        getLivesText().setId("lives");//ID to style Lives Label in CSS


        pauseButton = new Button("Pause Game");
        getButton().setTranslateX(240);
        getButton().setTranslateY(110);
        pauseButton.setId("pauseButton");

        // Add the event handler to the paused button
        pauseButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> controller.togglePause());

        startButton = new Button("RESTART GAME");
        startButton.setTranslateX(380);
        startButton.setTranslateY(110);
        startButton.setId("startButton");

        //add the event handler to the start button
        startButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> controller.toggleStart());

        userName = new Label();
        userName.setTranslateX(410);
        userName.setTranslateY(40);
        userName.setId("username");
        highestScore = new Label();
        highestScore.setTranslateX(360);
        highestScore.setTranslateY(70);
        highestScore.setId("highestScore");

        //LOGIN PANE DISPLAYED WHEN GAME IS LAUNCHED
        loginPane = new Pane();

        loginLabel = new Label("Your Name: ");
        loginTF = new TextField();
        loginButton = new Button("Login");
        loginLabel.setTranslateX(100);
        loginLabel.setTranslateY(200);
        loginTF.setTranslateX(280);
        loginTF.setTranslateY(200);
        loginButton.setTranslateX(250);
        loginButton.setTranslateY(280);
        loginButton.setId("loginButton");
        loginTF.setId("loginTF");
        loginLabel.setId("loginLabel");
        loginPane.getChildren().add(loginTF);
        loginPane.getChildren().add(loginLabel);
        loginPane.getChildren().add(loginButton);
        loginPane.setTranslateY(200);
        pane.getChildren().add(loginPane);

        // Add the event handler to the login button
        loginButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> controller.toggleLogin());


        Scene scene;

        //set scene depending on game state
        if(model.getGameState().equals("login")){
            scene = new Scene(loginPane, getWidth(),getHeight());
        }else{
            scene = new Scene(getPane(), getWidth(), getHeight());

        }
        scene.getStylesheets().add("breakout.css"); // tell the app to use our css file

        // Add an event handler for key presses. By using 'this' (which means 'this
        // view object itself') we tell JavaFX to call the 'handle' method (below)
        // whenever a key is pressed
        scene.setOnKeyPressed(this);
        window.setScene(scene);

        // put the scene in the window and display it
        window.show();

    }


    /**
     * Event handler for key presses - it just passes the event to the controller
     * @param event keyboard key event
     */
    public void handle(KeyEvent event)
    {
        // send the event to the controller
        getController().userKeyInteraction( event );

    }


    /**
     * Method drawing the game image
     */
    public void drawPicture()
    {
        // the game loop is running 'in the background' so we have
        // add the following line to make sure it doesn't change
        // the model in the middle of us updating the image
        synchronized (getModel())
        {


            // remove all the children from the pane ('clear the screen')
            getPane().getChildren().clear();

            // update the score display string with the new score 
            getInfoText().setText("BreakOut: Score = " + getScore());
            // add the label to the Pane
            getPane().getChildren().add(getInfoText());

            //ADD LABEL FOR LIVES TO THE PANE
            getLivesText().setText("Lives: " + getLives());
            getPane().getChildren().add(getLivesText());

            pane.getChildren().add(pauseButton);
            userName.setText("Hello " + getLoginTF().getText());
            pane.getChildren().add(userName);
            highestScore.setText("Highest score: " + previousScore());
            pane.getChildren().add(highestScore);

            // draw the bat and ball
            displayGameObj(getBall());   // Display the Ball
            displayGameObj(getBat());   // Display the Bat

            //RUN DISPLAY BRICKS METHOD
            displayBricks();

            gameOver();

        }
    }

    /**
     * Method adding content of the text File into the 'playersScores' hashmap
     *
     */
    public void addToMap(){
        playerScores = controller.fromFile("userScores.txt");
      //  I commented the printouts for testing and debugging purposes
          for(Map.Entry<String, String> entry : playerScores.entrySet()){
         System.out.println("MAP ENTRIES -> "+entry.getKey() + ":" + entry.getValue());
          }
         String temp = playerScores.get(getLoginTF().getText());
         System.out.println(temp);
        oldScore = playerScores.get(getLoginTF().getText());    //saves the old score
        //System.out.println(oldScore);
    }

    /**
     * This method is called when game is over.<br>
     * It makes sure that the score saved into the map and than into the file
     * is bigger than already existing one.
     */
    private void writeScores(){
        int score = getScore();
        String name = getLoginTF().getText();
        //System.out.println(name + ": Score "+score);
        /* The oldScore is saves in a map as a Sting and the actual score as a Integer
        * to compare both I parsed String int int*/
        if (score > Integer.parseInt(oldScore)){
            oldScore = Integer.toString(score);
        }
        //if the user logged in for the firs time their score is saved as a -1
        if(Integer.parseInt(oldScore) == -1){
            oldScore = Integer.toString(score);
        }
        playerScores.replace(name, Integer.toString(score));
        for (Map.Entry<String, String> entry : playerScores.entrySet()){
            //System.out.println( "FIRST PRINT VIEW writeScore()::::"+entry);
            if(entry.getKey().equals(name) && Integer.parseInt(oldScore) > Integer.parseInt(entry.getValue())){
                getPlayerScores().replace(name, oldScore);

            }
            //System.out.println( "VIEW FINAL PRINT writeScore()::::"+entry);

        }
    }

    /**
     * This method the score from the Hash Map<br>
     * String returned will be shown at the top of the screen.
     * @return score, if player already exist in the Map it returns the saved score, otherwise returns zero.
     */
    private String previousScore(){
        String highest = playerScores.get(getLoginTF().getText());
        if(highest.equals("-1")){
            //if the player logs in for the firs time the score is zero
            return "0";
        }
        return highest;
    }

/*      I used this method for testing and debugging the previous score,
      I wanted to save the current score only if was bigger than the one already saved*/
//    public String currentScore(){
//        String endScore = "";
//        if(gameOver()){
//            endScore = Integer.toString(getScore());
//        }
//        return endScore;
//    }


    /**
     * Method responsible for checking if the game state is 'finished'. <br>
     * Uses writeScore() to add to the map.<br>
     * Uses toFile() from controller to write to the file.<br>
     * Displays 'game-over' GIF on the screen and plays the music.
     * @return true whe the game is over
     */
    private boolean gameOver(){

        if(model.getGameState().equals("finished")) { //DISPLAY WHEN GAME OVER
            writeScores();
            controller.toFile();
            Rectangle rect = new Rectangle(50, 300, 500, 430);
            if(getLives().equals("0")){
                Image img = new Image("game-over2.gif");
                ImagePattern imgPattern = new ImagePattern(img, 100, 300, 450, 450, false);
                rect.setFill(imgPattern);
                pane.getChildren().add(rect);
            }
            if (model.getBrickCounter()==0){
                Rectangle rect1 = new Rectangle(50, 300, 500, 430);
                Image img = new Image("win.jpg");
                ImagePattern imgPattern = new ImagePattern(img, 100, 300, 450, 450, false);
                rect1.setFill(imgPattern);
                pane.getChildren().add(rect1);

            }
            model.getPlayerGameMusic().stop();
            displayButton();
            model.getPlayerGameOver().play();
            return true;
    }
        return false;
}

    /**
     * Adds "Restart game" button the the pane only when game is over
     */
    private void displayButton(){
       pane.getChildren().add(startButton);
}

    /**
     * Method to display visible bricks
     */
    public void displayBricks(){
        for (GameObj brick : getBricks()){
            if (brick.isVisible()){
                displayGameObj(brick);
            }
        }
    }


    /**
     * Display a game object - create a Rectangle object (if necessary) and add it to the Pane in the right position
     * @param go Game Object
     */
    public void displayGameObj( GameObj go )
    {
        Rectangle s;
        // check whether we have seen this GameObject before
        if (getRectangleStore().containsKey(go) ) {
            // yes we have - so we already have a Rectangle object for it in the store
            s = getRectangleStore().get(go);
        } else {
            // no we haven't, so make one and add it to the store (for next time)
            s = new Rectangle();
            // set properties for it that are not going to change
            s.setFill(go.getColour());

            s.setWidth(go.getWidth());
            s.setHeight(go.getHeight());
            // add it to the pane
            getRectangleStore().put(go,s);
        }
        // set the position of rectangle
        s.setX(go.getTopX());
        s.setY(go.getTopY());
        // and add it to the Pane
        getPane().getChildren().add(s);
    }
    
    /**
     * This is how the Model talks to the View
     *  This method gets called BY THE MODEL, whenever the model changes
     *  It has to do whatever is required to update the GUI to show the new game position
     */
    public void update()
    {
        // Get from the model the ball, bat, bricks & score
        setBall(getModel().getBall());              // Ball
        setBricks(getModel().getBricks());            // Bricks
        setBat(getModel().getBat());               // Bat
        setScore(getModel().getScore());             // Score
        //Debug.trace("Update");


        //GET MODEL FOR LIVES
        setLives(getModel().getLives());
        drawPicture();                     // Re draw game
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
    public Pane getPane() {
        return pane;
    }
    public void setPane(Pane pane) {
        this.pane = pane;
    }
    public Label getInfoText() {
        return infoText;
    }
    public Label getLivesText(){return livesText;}
    public void setInfoText(Label infoText) {
        this.infoText = infoText;
    }
    public Controller getController() {
        return controller;
    }
    public void setController(Controller controller) {
        this.controller = controller;
    }
    public GameObj getBat() {
        return bat;
    }
    public void setBat(GameObj bat) {
        this.bat = bat;
    }
    public GameObj getBall() {
        return ball;
    }
    public void setBall(GameObj ball) {
        this.ball = ball;
    }
    public GameObj[] getBricks() {
        return bricks;
    }
    public void setBricks(GameObj[] bricks) {
        this.bricks = bricks;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public HashMap<GameObj, Rectangle> getRectangleStore() {
        return rectangleStore;
    }
    public void setRectangleStore(HashMap<GameObj, Rectangle> rectangleStore) {
        this.rectangleStore = rectangleStore;
    }

    //GETTERS AND SETTERS FOR LIVES LABEL AND LIVES COUNT

    public void setLivesText(Label livesText) {
        this.livesText = livesText;
    }

    public String getLives() {
        return lives;
    }

    public void setLives(String lives) {
        this.lives = lives;
    }


    public Button getButton() {
        return pauseButton;
    }

    public void setButton(Button button) {
        this.pauseButton = button;
    }
    public Button getStartButton() {
        return startButton;
    }
    public Pane getLoginPane() {
        return loginPane;
    }

    public void setLoginPane(Pane loginPane) {
        this.loginPane = loginPane;
    }
    public TextField getLoginTF() {
        return loginTF;
    }

    public void setLoginTF(TextField loginTF) {
        this.loginTF = loginTF;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }
    public Label getLoginLabel() {
        return loginLabel;
    }

    public void setLoginLabel(Label loginLabel) {
        this.loginLabel = loginLabel;
    }
    public HashMap<String, String> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(HashMap<String, String> playerScores) {
        this.playerScores = playerScores;
    }
    public String getOldScore() {
        return oldScore;
    }

    public void setOldScore(String oldScore) {
        this.oldScore = oldScore;
    }

public String getLogin(){
        return loginTF.getText();
}

    public void setPauseButton(Button pauseButton) {
        this.pauseButton = pauseButton;
    }
}

