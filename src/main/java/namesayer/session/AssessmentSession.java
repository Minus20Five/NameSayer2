package namesayer.session;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import namesayer.model.CompositeName;
import namesayer.model.CompositeRecording;

import java.util.List;

/**
 * An instance of Assessment Mode - in progress or saved to file
 */

public class AssessmentSession extends Session {

    public AssessmentSession(List<CompositeName> namesList) {
        super(namesList);
        this.type = SessionType.ASSESSMENT;
    }

    /**
     * Plays the exemplar first, then plays the user's own recording
     *
     * @param onFinished Runs when finishes playing
     */
    public void compareUserAttemptWithExemplar(EventHandler<ActionEvent> onFinished) {
        Thread thread = new Thread(() -> {
            try {
                currentName.getExemplar().playAudio();
                Thread.sleep(new Double(currentName.getExemplar().getLength() * 1000).longValue());
                if (!currentName.getUserAttempts().isEmpty()) {
                    CompositeRecording userRecording = currentName.getUserAttempts().get(0);
                    userRecording.playAudio();
                    Thread.sleep(new Double(userRecording.getLength() * 1000).longValue());
                    onFinished.handle(new ActionEvent());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    public CompositeRecording getCurrentRecording() {
        if (!currentName.getUserAttempts().isEmpty()) {
            return currentName.getUserAttempts().get(0);
        } else {
            throw new RuntimeException("No recordings have been made");
        }
    }

    public double getAverageRating() {
        double rating = 0;
        for (CompositeName name : namesList) {
            rating += name.getUserAttempts().get(0).getRating();
        }
        return rating / namesList.size();
    }


}
