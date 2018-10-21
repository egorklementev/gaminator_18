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

public class WaspF extends Entity {

    private float attack_timer = 0f;
    private Animation<TextureRegion> animation;

    /** @param g instance of Main class
     *  @param world world instance
     *  @param x x-coordinate in meters
     *  @param y y-coordinate in meters
     *  @param size size in units*/
    public WaspF(Main g, World world, float x, float y, float lifetime, float size) {
        super(world, x, y, lifetime);

        setSize(2f * size * Main.METER, 2f * size * Main.METER);

        // region Body init
        CircleShape waspShape = new CircleShape();
        waspShape.setRadius(size);

        FixtureDef wasp_fd = new FixtureDef();
        wasp_fd.shape = waspShape;
        wasp_fd.friction = .1f;
        wasp_fd.density = .1f;
        wasp_fd.restitution = .1f;

        AdvBodyPart playerPart = new AdvBodyPart(
                g.atlas.createSprite("bee_fat", 1),
                -size * Main.METER,
                -size * Main.METER,
                2f * size * Main.METER,
                2f * size * Main.METER,
                waspShape,
                wasp_fd
        );

        getAdvBody().addPart(playerPart);
        getAdvBody().setSize(size * Main.METER, size * Main.METER);
        // endregion
        // region FSA init
        State s_idle = new State("idle");
        State s_attack = new State("shoot");
        State s_wander = new State("wander");

        s_attack.setConnected(s_idle);
        s_attack.setConnected(s_wander);
        s_idle.setConnected(s_attack);
        s_idle.setConnected(s_wander);
        s_wander.setConnected(s_attack);
        s_wander.setConnected(s_idle);

        getFsa().addState(s_idle);
        getFsa().addState(s_attack);
        getFsa().addState(s_wander);
        // endregion

        animation = new Animation<>(
                0.05f,
                g.atlas.findRegions("bee_fat"),
                Animation.PlayMode.LOOP
        );
    }

    public void updateLife(float delta) {
        super.updateLife(delta);

        Vector2 distance = new Vector2(
                Field.player.getX() - getAdvBody().getX(),
                Field.player.getY() - getAdvBody().getY()
        );

        if (distance.len() <= 750f) {
            getFsa().changeState(getFsa().getState("shoot"));

            getAdvBody().getParts().get(0).getSprite().setRegion(animation.getKeyFrame(Field.state_time, true));

            float attack_time = 1f;
            if (attack_timer > attack_time) {
                attack_timer = -.5f + MathUtils.random();
                setName("shooting");
            } else {
                float x = Field.player.getX() - getAdvBody().getX();
                float y = Field.player.getY() - getAdvBody().getY();
                float angle = (float)(Math.atan2(x, y));
                getAdvBody().getBody().setTransform(getAdvBody().getBody().getPosition(), -angle);
                attack_timer += delta;
            }
        } else {
            if (isStateNeedToChange()) {
                if (MathUtils.randomBoolean()) {
                    getFsa().changeState(getFsa().getState("wander"));
                } else {
                    getFsa().changeState(getFsa().getState("idle"));
                }
            } else {
                switch (getFsa().getCurrentState().getName()) {
                    case "idle": {
                        if (MathUtils.random() > 0.9f && getAdvBody().getBody().getAngularVelocity() < 1f) {
                            getAdvBody().getBody().applyAngularImpulse(
                                    45f * MathUtils.random() * MathUtils.randomSign(),
                                    false
                            );
                        }
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
                                    ).nor().scl(15000f),
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
}
