package ru.erked.beelife.ai;

import java.util.ArrayList;

public class State {

    private String name;
    private ArrayList<State> connected;

    public State(String name) {
        this.name = name;
        connected = new ArrayList<>();
    }

    public void setConnected(State state) {
        connected.add(state);
    }

    public ArrayList<State> getConnected() {
        return connected;
    }

    public String getName() {
        return name;
    }
}
