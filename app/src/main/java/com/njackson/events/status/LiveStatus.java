package com.njackson.events.status;

public class LiveStatus {

    public enum State {
        STARTED,
        STOPPED,
        DISABLED
    }

    public State _state;
    public State getState() { return _state; }

    public LiveStatus(State state) {
        this._state = state;
    }

}
