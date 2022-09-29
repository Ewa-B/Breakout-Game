import javafx.application.Platform;

/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.0
 */
/* This is a wrapper for javafx's Platform class
 I wanted to test Model class in separation from javafx's Platform class
 otherwise exceptions would be thrown*/

public class PlatformWrapper {

    public void runLater(Runnable runnable) {
        Platform.runLater(runnable);
    }
}
