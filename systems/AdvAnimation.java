package ru.erked.beelife.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AdvAnimation {

    private int last_frame;
    private String current_state;
    private LinkedList<String> next_states;
    private ArrayList<Animation<TextureRegion>> animations;
    private float[] state_times;
    private boolean has_finished;
    private Map<String, Integer> states;

    public AdvAnimation (ArrayList<Animation<TextureRegion>> animations, String[] keys) {
        this.animations = animations;
        next_states = new LinkedList<>();
        state_times = new float[animations.size()];
        states = new HashMap<>();
        for (int i = 0; i < state_times.length; ++i) state_times[i] = 0f;
        for (int i = 0; i < keys.length; ++i) states.put(keys[i], i);
        current_state = keys[0];
        last_frame = 0;
    }

    public void addNextStates (String[] next_states) {
        for (String s : next_states) this.next_states.addLast(s);
    }

    private void update () {
        has_finished = false;
        if (isStateChange()) {
            if (!next_states.isEmpty()) {
                current_state = next_states.getFirst();
                next_states.removeFirst();
                has_finished = true;
            }
        }
        last_frame = animations.get(states.get(current_state)).getKeyFrameIndex(state_times[states.get(current_state)]);
    }

    public void changeState (String state) {
        current_state = state;
        next_states.clear();
    }

    public String getCurrentState () { return current_state; }
    public String getLastState () { return next_states.isEmpty() ? current_state : next_states.getLast(); }
    private boolean isStateChange () {
        switch(animations.get(states.get(current_state)).getPlayMode()) {
            case LOOP:{
                return animations.get(states.get(current_state)).getKeyFrameIndex(state_times[states.get(current_state)]) == 0 &&
                        last_frame == animations.get(states.get(current_state)).getKeyFrames().length - 1;
            }
            case LOOP_REVERSED:{
                return animations.get(states.get(current_state)).getKeyFrameIndex(state_times[states.get(current_state)]) == animations.get(states.get(current_state)).getKeyFrames().length - 1 &&
                        last_frame == 0;
            }
            case LOOP_PINGPONG:{ // Last frame is lost, so add additional frame to the texture
                return animations.get(states.get(current_state)).getKeyFrameIndex(state_times[states.get(current_state)]) == 0 &&
                        last_frame == 1;
            }

        }
        return false;
    }
    public boolean hasFinished () { return has_finished; }

    public TextureRegion getKeyFrame(float delta, boolean looping) {
        state_times[states.get(current_state)] += delta;
        update();
        return animations.get(states.get(current_state)).getKeyFrame(state_times[states.get(current_state)], looping);
    }
}
