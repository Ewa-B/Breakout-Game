
/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.3
 */

// this class was created in order to test Model.runGame method
// by using Mockito mocking capabilities and extracting gameState to separate class
// I were able to break infinite loop and test code inside it

public class GameState {
    private String gameState = Model.RUNNING_GAME_STATE;

    public synchronized String getGameState() {
        return gameState;
    }

    public void setGameState(String value) {
        this.gameState = value;
    }
}
