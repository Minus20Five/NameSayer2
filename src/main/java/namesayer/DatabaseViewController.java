package namesayer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSpinner;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import namesayer.model.CompositeName;
import namesayer.model.CompositeRecording;
import namesayer.model.PartialName;
import namesayer.model.PartialRecording;
import namesayer.model.Recording;
import namesayer.persist.NameStorageManager;
import org.controlsfx.control.Rating;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseViewController implements Initializable {

    @FXML private JFXButton backButton;
    @FXML private JFXListView nameList;
    @FXML private JFXListView recordingList;
    @FXML private GridPane parentPane;
    @FXML private JFXSpinner playingSpinner;
    private double ratingValue;
    @FXML private Rating rating;
    @FXML private JFXButton ratingButton;
    @FXML private MaterialIconView ratingIcon;
    private JFXSnackbar bar;
    private ObservableList<CompositeName> userRecordings;
    private ObservableList<PartialName> databaseRecordings;
    private boolean isNameDatabase;

    /**
     * Initialize listView
     */
    public void initialise() {
        userRecordings = NameStorageManager.getInstance().getCompositeNames();
        databaseRecordings = NameStorageManager.getInstance().getPartialNames();
        playingSpinner.setProgress(1);
        setRatingVisible(false, false);
    }

    /**
     * Show partial name database
     */
    public void onNameDatabaseClicked() {

        isNameDatabase = true;
        initialise();
        if (databaseRecordings.isEmpty()) {
            System.out.println("empty");
            bar.enqueue(new JFXSnackbar.SnackbarEvent("No recordings in datatbase"));
        } else {
            showNameDatabase();

        }
    }

    /**
     * Show user recordings
     */
    public void onUserRecordingClicked() {

        isNameDatabase = false;
        initialise();
        if (userRecordings.isEmpty()) {
            System.out.println("empty");
            bar.enqueue(new JFXSnackbar.SnackbarEvent("No user recording in datatbase"));
        } else {
            //System.out.println("222");
            showComNameDatabase();
        }
    }

    /**
     * Back to main menu
     */
    public void onBackClicked() throws IOException {
        Scene scene = backButton.getScene();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuScreen.fxml"));
        Parent root = loader.load();
        scene.setRoot(root);
    }

    /**
     * Play the selected recording
     */
    public void onPlayButtonClicked(MouseEvent e) {
        Recording recording = (Recording) recordingList.getSelectionModel().getSelectedItem();
        if (recording == null) {
            bar.enqueue(new JFXSnackbar.SnackbarEvent("Please select a recording first"));
        } else {
            recording.playAudio();
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0), new KeyValue(playingSpinner.progressProperty(), 0)),
                    new KeyFrame(
                            Duration.seconds(recording.getLength()),
                            //event -> disableButtons(false, true),
                            new KeyValue(playingSpinner.progressProperty(), 1)
                    )
            );
            timeline.play();
            System.out.println(recording.toString());
        }
    }

    /**
     * Show the recording list of selected partial name
     */
    private void showNameDatabase() {
        nameList.setItems(databaseRecordings);
        nameList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e) {
                setRatingVisible(false, false);
                PartialName name = (PartialName) nameList.getSelectionModel().getSelectedItem();
                recordingList.setItems(name.getRecordings());
                recordingActionListener();
            }

        });


    }

    /**
     * Show the recording list of selected composite name
     */
    private void showComNameDatabase() {
        nameList.setItems(userRecordings);
        nameList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                setRatingVisible(false, false);
                CompositeName name = (CompositeName) nameList.getSelectionModel().getSelectedItem();
                recordingList.setItems(name.getUserAttempts());
                userAttemptsListener();
            }


        });
    }

    /**
     * Add event listener to each composite recording
     */
    private void userAttemptsListener() {
        recordingList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                setRatingVisible(false, true);
                CompositeRecording r = (CompositeRecording) recordingList.getSelectionModel().getSelectedItem();
                rating.setRating(r.getRating());
                setUserAttemptsRating();
            }

        });

    }

    /**
     * Add event listener to each partial recording
     */
    private void recordingActionListener() {
        recordingList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                setRatingVisible(true, false);
                PartialRecording r = (PartialRecording) recordingList.getSelectionModel().getSelectedItem();
                setRating(r.getIsBadQuality());
            }
        });
    }

    /**
     * Show quality of partial recording
     */
    private void setRating(boolean isBadQuality) {
        if (isBadQuality) {
            ratingIcon.setGlyphName("THUMB_DOWN");
        } else {
            ratingIcon.setGlyphName("THUMB_UP");
        }
    }

    /**
     * Save the quality to partial recording
     */
    public void setRecordingQuality(MouseEvent e) {
        PartialRecording r = (PartialRecording) recordingList.getSelectionModel().getSelectedItem();
        if (r.getIsBadQuality()) {
            ratingIcon.setGlyphName("THUMB_UP");
            r.setIsBadQuality(false);

        } else {
            ratingIcon.setGlyphName("THUMB_DOWN");
            r.setIsBadQuality(true);
        }

    }

    /**
     * Save rating to composite recording
     */
    public void setUserAttemptsRating() {
        CompositeRecording r = (CompositeRecording) recordingList.getSelectionModel().getSelectedItem();
        ratingValue = rating.getRating();
        r.setRating(ratingValue);

    }

    /**
     * Initialise database view
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        bar = new JFXSnackbar(parentPane);
        bar.getStylesheets().addAll("/css/Material.css");
        rating.setRating(3.0);
        setRatingVisible(false, false);

    }

    /**
     * Set visibility of ratings
     */
    private void setRatingVisible(boolean thumb, boolean star) {
        rating.setVisible(star);
        ratingButton.setVisible(thumb);
    }

}
