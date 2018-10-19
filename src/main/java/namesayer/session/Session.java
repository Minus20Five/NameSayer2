package namesayer.session;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import namesayer.model.CompositeName;
import namesayer.model.PartialName;
import namesayer.persist.NameStorageManager;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class Session implements Serializable {

    protected List<CompositeName> namesList = new LinkedList<>();
    protected int currentIndex = 0;
    protected CompositeName currentName;
    protected String sessionName;
    protected String id;
    protected SessionType type;

    public enum SessionType {
        ASSESSMENT, PRACTISE
    }

    public Session(List<CompositeName> namesList) {
        if (namesList.isEmpty()) {
            throw new NameNotFoundException("Session cannot load with an empty list");
        }
        this.namesList = namesList;
        currentName = this.namesList.get(0);
        id = UUID.randomUUID().toString();
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String input) {
        sessionName = input;
    }

    public CompositeName getCurrentName() {
        return currentName;
    }

    public int getNumberOfNames() {
        return namesList.size();
    }

    public void resetToFirst() {
        currentIndex = 0;
        currentName = namesList.get(0);
    }

    public void next() {
        currentName = namesList.get(++currentIndex);
    }


    public boolean hasNext() {
        return !(currentIndex == namesList.size() - 1);
    }


    public int getCurrentIndex() {
        return currentIndex;
    }


    public void saveUserRecordings() {
        NameStorageManager.getInstance().persistCompleteRecordingsForName(currentName);
    }

    public String getId() {
        return id;
    }

    public SessionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return sessionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        return id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}