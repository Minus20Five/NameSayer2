package namesayer.session;


import namesayer.model.CompositeName;
import namesayer.model.CompositeRecording;
import namesayer.persist.NameStorageManager;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static namesayer.persist.Config.USER_ATTEMPTS;

/**
 * Base class for Practice and Assessment Sessions
 * Allows basic operations on Names provided to the session
 */

public class Session implements Serializable {

    protected List<CompositeName> namesList;
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

        //Each session is separated by ID
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

    public boolean hasUserMadeRecording() {
        return !currentName.getUserAttempts().isEmpty();
    }


    public String getId() {
        return id;
    }

    public SessionType getType() {
        return type;
    }

    /**
     * Called when session is deleted
     * Removes all unsaved files created by this session
     */
    public void removeFiles() {
        for (CompositeName name : namesList) {
            for (CompositeRecording recording : name.getUserAttempts()) {
                try {
                    if (recording.getRecordingPath().toString().contains(USER_ATTEMPTS.toString())){
                        Files.deleteIfExists(recording.getRecordingPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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