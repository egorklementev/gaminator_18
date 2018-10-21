package ru.erked.beelife.ai;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import ru.erked.beelife.Main;
import ru.erked.beelife.physics.AdvBodyPart;
import ru.erked.beelife.screens.Field;

public class Bug extends Entity {

    private Animation<TextureRegion> animation;

    /** @param g instance of Main class
     *  @param world world instance
     *  @param x x-coordinate in meters
     *  @param y y-coordinate in meters
     *  @param size size in units
     *  @param index index of the bug texture*/
    public Bug(Main g, World world, float x, float y, float lifetime, float size, int index) {
        super(world, x, y, lifetime);

        setSize(2f * size * Main.METER, 2f * size * Main.METER);

        // region Body init
        CircleShape bugShape = new CircleShape();
        bugShape.setRadius(size);

        FixtureDef bug_fd = new FixtureDef();
        bug_fd.shape = bugShape;
        bug_fd.friction = .1f;
        bug_fd.density = .1f;
        bug_fd.restitution = .1f;

        AdvBodyPart playerPart = new AdvBodyPart(
                g.atlas.createSprite("bug" + index, 1),
                -size * Main.METER,
                -size * Main.METER,
                2f * size * Main.METER,
                2f * size * Main.METER,
                bugShape,
                bug_fd
        );

        getAdvBody().addPart(playerPart);
        getAdvBody().setSize(size * Main.METER, size * Main.METER);
        // endregion
        // region FSA init
        State s_idle = new State("idle");
        State s_look = new State("look");
        State s_wander = new State("wander");

        s_idle.setConnected(s_look);
        s_look.setConnected(s_idle);
        s_idle.setConnected(s_wander);
        s_wander.setConnected(s_idle);

        getFsa().addState(s_idle);
        getFsa().addState(s_look);
        getFsa().addState(s_wander);
        // endregion

        animation = new Animation<>(
                0.05f,
                g.atlas.findRegions("bug" + index),
                Animation.PlayMode.LOOP
        );
    }

    public void updateLife(float delta) {
        super.updateLife(delta);

        if (isStateNeedToChange()) {
            int rand_n = MathUtils.random(getFsa().getCurrentState().getConnected().size() - 1);
            getFsa().changeState(getFsa().getCurrentState().getConnected().get(rand_n));
        } else {
            switch (getFsa().getCurrentState().getName()) {
                case "idle": {
                    if (MathUtils.random() > 0.99f) {
                        getAdvBody().getBody().applyAngularImpulse(
                                10f * MathUtils.random() * MathUtils.randomSign(),
                                false
                        );
                    }
                    break;
                }
                case "look": {
                    float x = Field.player.getX() - getAdvBody().getX();
                    float y = Field.player.getY() - getAdvBody().getY();
                    float angle = (float)(Math.atan2(x, y));
                    getAdvBody().getBody().setTransform(getAdvBody().getBody().getPosition(), -angle);
                    break;
                }
                case "wander": {
                    getAdvBody().getParts().get(0).getSprite().setRegion(animation.getKeyFrame(Field.state_time, true));
                    if (getAdvBody().getBody().getLinearVelocity().len() < 3f) {

                        float x = (float)(Math.random() - 0.5);
                        float y = (float)(Math.random() - 0.5);
                        float angle = (float)(Math.atan2(x, y));
                        getAdvBody().getBody().setTransform(getAdvBody().getBody().getPosition(), -angle);

                        getAdvBody().getBody().applyForceToCenter(
                                new Vector2(
                                        x,
                                        y
                                ).nor().scl(4500f),
                                true
                        );
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }
}
