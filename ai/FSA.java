package ru.erked.beelife.ai;

import java.util.ArrayList;

public class FSA {

    private State currentState;
    private ArrayList<State> states;

    public FSA() {
        states = new ArrayList<>();
    }

    public void addState(State state) {
        if (states.size() == 0) {
            currentState = state;
        }
        states.add(state);
    }

    public void changeState(State state) {
        currentState = state;
    }

    public State getCurrentState() {
        return currentState;
    }

    public State getState(String name) {
        for (State s : states) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public ArrayList<State> getStates() {
        return states;
    }

}
