package namesayer.view.controller;


import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.Utils;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import namesayer.model.CompositeName;
import namesayer.model.CompositeRecording;
import namesayer.model.Name;
import namesayer.persist.StatsManager;
import namesayer.session.PractiseSession;
import namesayer.util.EmptySelectionModel;
import namesayer.util.SnackBarLoader;
import namesayer.view.alert.MicTestAlert;
import namesayer.view.cell.PractiseListCell;
import namesayer.view.alert.SaveAlert;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.List;

import static namesayer.persist.Config.BUFFER_TIME;
import static namesayer.util.TransitionFactory.Direction.LEFT;
import static namesayer.util.TransitionFactory.Direction.RIGHT;
import static namesayer.util.TransitionFactory.cardDoubleSlideTransition;


public class PractiseScreenController {

    @FXML private JFXTextField nameSearchBar;
    @FXML private JFXButton micTestButton;
    @FXML private GridPane parentPane;
    @FXML private Label cardNumber;
    @FXML private JFXListView<CompositeRecording> practiseListView;
    @FXML private JFXSpinner recordingSpinner;
    @FXML private JFXButton nextButton;
    @FXML private JFXButton prevButton;
    @FXML private StackPane cardPane;
    @FXML private Label label;
    @FXML private JFXSpinner playingSpinner;
    @FXML private JFXButton recordingButton;

    private PractiseSession session;

    /**
     * This method must be called to provide the Session model
     *
     * @param session PracticeSession Session
     */
    public void injectSession(PractiseSession session) {
        this.session = session;
        disableArrows(false);
        label.setText(session.getCurrentName().toString());
        practiseListView.setCellFactory(param -> new PractiseListCell(session, this));
        practiseListView.setSelectionModel(new EmptySelectionModel<>());
        Label label = new Label("Listen to the sample and make your own recording!");
        label.setFont(new Font(15));
        practiseListView.setPlaceholder(label);
        SuggestionProvider<CompositeName> suggestions = SuggestionProvider.create(Name::toString, session.getSuggestions());
        AutoCompletionBinding<CompositeName> autocompletion = TextFields.bindAutoCompletion(nameSearchBar, suggestions);
        autocompletion.setPrefWidth(190);
        refreshList();
    }


    public void initialize() {
        playingSpinner.setProgress(1);
        recordingSpinner.setProgress(1);
    }

    /**
     * Update labels for a new Name
     */
    private void loadNewCard(boolean isNext) {
        if (isNext) {
            session.next();
        } else {
            session.prev();
        }
        label.setText(session.getCurrentName().toString());
        disableArrows(false);
        refreshList();
    }


    /**
     * Play the exemplar and animate the playButton
     */
    public void onPlayButtonClicked(MouseEvent mouseEvent) {
        session.getCurrentName().getExemplar().playAudio();
        disableArrows(true);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(playingSpinner.progressProperty(), 0)),
                new KeyFrame(
                        Duration.seconds(session.getCurrentName().getExemplar().getLength()),
                        event -> disableArrows(false),
                        new KeyValue(playingSpinner.progressProperty(), 1)
                )
        );
        timeline.play();
    }

    /**
     * Start the recording and animate the RecordingButton
     */
    public void onRecordingButtonClicked(MouseEvent mouseEvent) {
        disableArrows(true);
        session.getCurrentName().makeNewRecording(event -> {
            disableArrows(false);
            refreshList();
        });
        recordingButton.setMouseTransparent(true);
        recordingSpinner.setVisible(true);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(recordingSpinner.progressProperty(), 0)),
                new KeyFrame(
                        Duration.seconds(session.getCurrentName().getExemplar().getLength() + BUFFER_TIME),
                        event -> recordingButton.setMouseTransparent(false),
                        new KeyValue(recordingSpinner.progressProperty(), 1)
                )
        );
        timeline.play();
    }

    /**
     * Transition to the next card and animate it
     */
    public void onNextButtonClicked(MouseEvent mouseEvent) {
        SequentialTransition transition = cardDoubleSlideTransition(cardPane, LEFT, event -> loadNewCard(true));
        transition.play();
    }

    /**
     * Transition to the previous card and animate it
     */
    public void onPrevButtonClicked(MouseEvent mouseEvent) {
        SequentialTransition transition = cardDoubleSlideTransition(cardPane, RIGHT, event -> loadNewCard(false));
        transition.play();
    }

    /**
     * Disable various UI elements so the user does not get into a bad state
     *
     * @param disableArrows Disable arrow buttons for next / prev
     */
    private void disableArrows(boolean disableArrows) {
        if (disableArrows) {
            nextButton.setDisable(true);
            prevButton.setDisable(true);
        } else {
            nextButton.setDisable(!session.hasNext());
            prevButton.setDisable(!session.hasPrev());
        }
    }

    /**
     * Updates the List of Names when a recording is finished
     */
    public void refreshList() {
        List<CompositeRecording> userAttempts = session.getCurrentName().getUserAttempts();
        ObservableList<CompositeRecording> recordings = FXCollections.observableArrayList(userAttempts);
        practiseListView.setItems(recordings);
        practiseListView.refresh();
        cardNumber.setText(session.getCurrentIndex() + 1 + "/" + session.getNumberOfNames());
        cardPane.requestFocus();
    }


    public void onBackButtonClicked(MouseEvent mouseEvent) {
        SaveAlert alert = new SaveAlert((Stage) parentPane.getScene().getWindow(), session);
        alert.show();
        StatsManager.getInstance().save();
    }

    /**
     * Attempts to save all CompositeRecordings made for the current name
     * and displays error or success message
     */
    public void onSaveButtonClicked(MouseEvent mouseEvent) {
        if (session.hasUserMadeRecording()) {
            session.saveUserRecordings();
            SnackBarLoader.displayMessage(parentPane, "Recordings have been saved");
        } else {
            SnackBarLoader.displayMessage(parentPane, "Recording list is empty");
        }
    }

    /**
     * Show Microphone testing dialog with volume information
     */
    public void onMicTestButtonClicked(MouseEvent mouseEvent) {
        MicTestAlert alert = new MicTestAlert((Stage) parentPane.getScene().getWindow());
        alert.show();
        micTestButton.setDisableVisualFocus(true);
    }

    /**
     * Allows the user to jump to a different name by entering one in the search bar
     */
    public void searchNameKeyTyped(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            boolean found = session.jumpTo(nameSearchBar.getText());
            nameSearchBar.clear();
            if (found) {
                SequentialTransition transition = cardDoubleSlideTransition(cardPane, LEFT, event -> {
                    label.setText(session.getCurrentName().toString());
                    disableArrows(false);
                    refreshList();
                });
                transition.play();
            } else {
                SnackBarLoader.displayMessage(parentPane, "Name is not in session");
            }
        }

    }
}