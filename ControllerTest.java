import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class ControllerTest {

    @Mock
    private Model model;
    @Mock
    private View view;
    @InjectMocks
    private Controller instance;

    /**
     * This test uses mock test file - "userScoresTestFile.txt" - with 3 names added to it <br>
     * and checks if it's size is actual 3 <br>
     * Then it checks if the particular name in the file is associated with correct score.
     */
    @Test
    public void shouldLoadScoresFromFile() {
        // Add names from file into the hash map
        HashMap<String, String> scores = instance.fromFile("userScoresTestFile.txt");
        Assert.assertEquals(3, scores.size()); // check the size
        Assert.assertEquals("300", scores.get("Chris")); //check specific Key and Value
    }

    /**
     * This test uses Mockito framework.<br>
     * Testing if players that don't exist in the map are added to it
     * after login in (toggleLogin() method)
     */
     @Test
     public void shouldAddLoginToScoresListWhenNotPresent() {
         HashMap<String, String> scores = new HashMap<>();
         scores.put("Ewa", "500"); //add one name to the map
         Mockito.when(view.getPlayerScores()).thenReturn(scores);
         Mockito.when(view.getLogin()).thenReturn("Chris"); //Chris is logging in (name not in the map)

         //event after pressing login button
         instance.toggleLogin();

         //check if size of the map is 2 (two names added)
         Assert.assertEquals(2, scores.size());
         //check if the score for name that wasn't in the map is set to -1
         Assert.assertEquals("-1", scores.get("Chris"));
         Mockito.verify(view).setOldScore("-1");
         Mockito.verify(view).addToMap();
     }

     /**
      * This test uses Mockito framework.<br>
      * This test is opposite to tho one above<br>
      * Testing players that exist in the map and their score
      * after pressing login button
      */
     @Test
     public void shouldNotAddLoginToScoresListWhenAlreadyPresent() {
         HashMap<String, String> scores = new HashMap<>();
         scores.put("Ewa", "500"); // add player
         Mockito.when(view.getPlayerScores()).thenReturn(scores);
         Mockito.when(view.getLogin()).thenReturn("Ewa"); //Ewa is logging in again

         instance.toggleLogin();

         Assert.assertEquals(1, scores.size());
         Assert.assertEquals("500", scores.get("Ewa")); //Ewa's score score stays the same
         Mockito.verify(view).addToMap();
     }
}