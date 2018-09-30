package namesayer.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PartialName extends Name {

    private ObservableList<PartialNameRecording> recordings = FXCollections.observableArrayList();

    public PartialName(String name) {
        super(name);
    }

    public void addDatabaseRecording(PartialNameRecording recording) {
        recordings.add(recording);
    }


}