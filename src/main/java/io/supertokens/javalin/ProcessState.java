package io.supertokens.javalin;

import java.util.ArrayList;
import java.util.List;

public class ProcessState {

    private final List<EventAndException> history = new ArrayList<>();
    private static ProcessState instance = null;

    private ProcessState() {

    }

    public static void reset(){
        instance = null;
    }

    public static ProcessState getInstance() {
        if (instance == null) {
            instance = new ProcessState();
        }
        return instance;
    }

    public EventAndException getLastEventByName(PROCESS_STATE processState) {
        if (!Constants.IS_TESTING) {
            return null;
        }
        synchronized (this) {
            for (int i = history.size() - 1; i >= 0; i--) {
                if (history.get(i).state == processState) {
                    return history.get(i);
                }
            }
            return null;
        }
    }

    public void addState(PROCESS_STATE processState, Exception e) {
        if (!Constants.IS_TESTING) {
            return;
        }
        synchronized (this) {
            history.add(new EventAndException(processState, e));
        }
    }

    public enum PROCESS_STATE {
        CALLING_SERVICE_IN_VERIFY
    }

    public static class EventAndException {
        public Exception exception;
        PROCESS_STATE state;

        public EventAndException(PROCESS_STATE state, Exception e) {
            this.state = state;
            this.exception = e;
        }
    }

}
