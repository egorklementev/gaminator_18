package ru.erked.beelife.ai;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import ru.erked.beelife.Main;
import ru.erked.beelife.screens.Field;
import ru.erked.beelife.systems.AdvSprite;

public class Ant extends AdvSprite {

    private float ctime = 0f;
    private float state_timer = 3f;

    private FSA fsa;
    private Animation<TextureRegion> animation;

    public Ant(Main g, float x, float y, float size) {
        super(g.atlas.createSprite("ant", 1), x, y, size, size);

        // region FSA init
        fsa = new FSA();

        State s_idle = new State("idle");
        State s_look = new State("look");
        State s_wander = new State("wander");

        s_idle.setConnected(s_look);
        s_look.setConnected(s_idle);
        s_idle.setConnected(s_wander);
        s_wander.setConnected(s_idle);

        fsa.addState(s_idle);
        fsa.addState(s_look);
        fsa.addState(s_wander);
        // endregion

        animation = new Animation<>(
                0.1f,
                g.atlas.findRegions("ant"),
                Animation.PlayMode.LOOP
        );
    }

    public FSA getFsa() {
        return fsa;
    }

    public void updateLife(float delta) {
        if (ctime > state_timer) {
            int rand_n = MathUtils.random(getFsa().getCurrentState().getConnected().size() - 1);
            getFsa().changeState(getFsa().getCurrentState().getConnected().get(rand_n));
            ctime = 0f;
        } else {
            ctime += delta;
            switch (getFsa().getCurrentState().getName()) {
                case "idle": {
                    if (MathUtils.random() > 0.99f) {
                        addAction(Actions.rotateBy(
                                30f * MathUtils.random() * MathUtils.randomSign(),
                                .5f
                        ));
                    }
                    break;
                }
                case "look": {
                    float x = Field.player.getX() - getX();
                    float y = Field.player.getY() - getY();
                    float angle = (float)(Math.atan2(x, y));
                    rotateBy(angle);
                    break;
                }
                case "wander": {
                    getSprite().setRegion(animation.getKeyFrame(Field.state_time, true));
                    if (!hasActions()) {
                        float x = (float)(Math.random() - 0.5);
                        float y = (float)(Math.random() - 0.5);
                        float angle = (float)(Math.atan2(x, y));
                        rotateBy(angle);

                        addAction(Actions.moveBy(
                                x * Main.METER * 10f,
                                y * Main.METER * 10f,
                                1f
                        ));
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateLife(delta);
    }
}
