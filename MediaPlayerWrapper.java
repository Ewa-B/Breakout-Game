// This is a wrapper for javafx's MediaPlayer class
// I created it to test Model class in separation from javafx's MediaPlayer class
// otherwise exceptions would be thrown

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


/**
 * @author <strong>Ewa Bancerz</strong>
 * @version 1.2
 */
public class MediaPlayerWrapper {

    private MediaPlayer mediaPlayer;

    public MediaPlayerWrapper(Media media) {
        mediaPlayer = new MediaPlayer(media);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void play() {
        mediaPlayer.play();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void pause() {
        mediaPlayer.pause();
    }
}
