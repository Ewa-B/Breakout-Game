
// The breakout controller converts key presses from the user (received by the View object)
// into commands for the game (in the Model object)

// we need to use on JavaFX class
import javafx.scene.input.KeyEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.3
 */
public class Controller {
  // instance variables for the two other components of the MVC model
  //ENCAPSULATED FIELDS

  // instance variables for the two other components of the MVC model
  private Model model;
  public Model getModel() { return model; }

  private View view;
  private boolean start = false;

  public boolean isStart() {
    return start;
  } //controls initial state of the game

  // we don't really need a constructor method, but include one to print a
  // debugging message if required
  public Controller() {
    Debug.trace("Controller::<constructor>");
  }

  /**
   * This is how the View talks to the Controller AND how the Controller talks to the Model
   *   This method is called by the View to respond to key presses in the GUI<br>
   *  The controller's job is to decide what to do. In this case it converts
   *    the keypresses into commands which are run in the model
   * @param event KeyEvent from the keyboard
   */

  public void userKeyInteraction(KeyEvent event) {
    // print a debugging message to show a key has been pressed
    Debug.trace("Controller::userKeyInteraction: keyCode = " + event.getCode());

    //position of the bat (printout for debugging)
    int x = getModel().getBat().getTopX();
    // System.out.println("x:"+x);

    /* KeyEvent objects have a method getCode which tells us which key has been pressed.
    KeyEvent also provides variables LEFT, RIGHT, F, N, S (etc) which are the codes
    for individual keys. So you can add keys here just by using their name (which you
    can find out by googling 'JavaFX KeyCode')*/
    switch (event.getCode()) {
      case LEFT:
//         Left Arrow
        if (x <= getModel().getB()){
          getModel().moveBat(0);
        }else{
          getModel().moveBat(-1);   }       // move bat left
        break;
      case RIGHT:
        // Right arrow
        if( x >= getModel().getWidth() - getModel().getB() - getModel().getBRICK_WIDTH()*3){
          getModel().moveBat(0);
        }else{
          getModel().moveBat(+1);
        }
        break;
      case F:
        // Very fast ball movement
        getModel().setFast(true);
        break;
      case N:
        // Normal speed ball movement
        getModel().setFast(false);
        break;
      case S:
        // stop the game
        getModel().setGameState("finished");
        break;
      case ENTER:
        //start the game
        toggleLogin();
        break;
    }

  }

  /**
   * Method responsible for the Pause button event. Pauses the game and
   * changes the text displayed on the button.
   */
  public void togglePause() {
    if (getModel().getGameState().equals("running")) { //if game is running -> pause it
      getModel().setGameState("paused");
      view.getButton().setText("Resume Game");  //change Button text

    } else if (getModel().getGameState().equals("paused")) { //if game is paused -> run it
      getModel().setGameState("running");
      view.getButton().setText("Pause Game"); //change the text
    }
  }

  /**
   * This method restarts the whole game when RESTART GAME button is pressed.<br>
   * This button is hidden until the game is over.
   */
  public void toggleStart() {
    start = true;
    System.out.println("toggleStart() :: " + model.getGameState());  //debugging
    if (model.getGameState().equals(Model.FINISHED_GAME_STATE)) {
      model.setGameState(Model.RUNNING_GAME_STATE);  //change game state
      model.startGame();
      getModel().setFast(false);    // make sure speed of game is normal
      model.playerGameOver.stop();
    }
  }

  /**
   * Method responsible for pressing the login button.<br>
   * Checks if user name typed at least one character to login.<br>
   * Writes name to the hash map, sets score accordingly
   */
  public void toggleLogin() {
    String userName = view.getLogin();
    if(userName.isEmpty() || userName.trim().isEmpty()){
      model.setGameState(Model.LOGIN_GAME_STATE); // stay on the login screen until name is valid
    }else {
      view.addToMap();  //add to hashmap
      System.out.println(userName); //all printouts are for debugging purposes
      if (view.getPlayerScores().containsKey(userName)) {
        System.out.println("User name exists");
      } else {
        System.out.println("User doesn't exist");  // set score to -1 if the name isn't already in the map
        view.getPlayerScores().put(userName, "-1");
        view.setOldScore("-1");
      }

      //userKeyInteraction() method sets game speed to fast whe 'f' key is pressed
      if (userName.contains("f") || userName.contains("F")) { // check if players name contains "f" character
        model.setFast(false);
      }
      getModel().setGameState(Model.RUNNING_GAME_STATE);
    }
  }

  /**
   * This method writes from the text file "userScores.txt" into the HashMap, adds players names and scores <br>
   * Resources: https://www.geeksforgeeks.org/reading-text-file-into-java-hashmap/
   * @param path this is a path to the file
   * @return HashMap that is initialised in The View class
   */
  public HashMap<String, String> fromFile(String path) {
    // Initialise the hash map
    HashMap<String, String> map = new HashMap<>();
    BufferedReader br = null;
    try {
      // create file object
      File file = new File(path);
      // create buffer reader from the file
      br = new BufferedReader(new FileReader(file));
      String line = null;
      // read file line by line
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(":");   //split the line by :
        //first part in the players name second in the score
        String name = parts[0].trim();
        String number = parts[1].trim();
        //put name and score into the HashMap if they aren't an empty strings
        if (!name.equals("") && !number.equals(""))
          map.put(name, number);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close(); //close the reader
        } catch (Exception e) {
        };
      }
    }
    return map;
  }

  /**
   * This method writes from the hashMap into the text file "userScores.txt",
   * adds values on each line separated by the colon <br>
   * Resources: https://www.geeksforgeeks.org/write-hashmap-to-a-text-file-in-java/
   */
  public void toFile() {
    String path = "userScores.txt";     // set file path
    HashMap<String, String> map = view.getPlayerScores();
    String name = view.getLoginTF().getText();
    // all printouts for debugging purposes
    System.out.println("OLD SCORE:::"+view.getOldScore());
    System.out.println("Name: "+ name);

    for (Map.Entry<String, String> entry : map.entrySet()){
      System.out.println("WRITE FINAL Entry: "+entry);
    }

    File file = new File(path);
    BufferedWriter bf = null;
    try {
      //create new buffer writer for output file
      bf = new BufferedWriter(new FileWriter(file));
      //iterate the map entries
      for (Map.Entry<String, String> entry : map.entrySet()){
        bf.write(entry.getKey() + ":" + entry.getValue());
        bf.newLine(); //write each entry on new line
      }
      bf.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bf.close(); // close the writer
      } catch (Exception e) {
      }
    }
  }



  public void setModel(Model model) {
    this.model = model;
  }

  public View getView() {
    return view;
  }

  public void setView(View view) {
    this.view = view;
  }
}