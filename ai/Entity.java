package ru.erked.beelife.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import ru.erked.beelife.physics.AdvBody;
import ru.erked.beelife.physics.AdvBodyPart;

public class Entity extends Actor {

    private FSA fsa;
    private AdvBody body;
    private boolean is_alive = true;

    private float ctime = 0f;
    private float lifetime;
    private float state_timer = 3f;
    private boolean stateNeedToChange = false;

    /** @param world world instance
     *  @param x x-coordinate in meters
     *  @param y y-coordinate in meters*/
    public Entity(World world, float x, float y, float lifetime) {
        this.lifetime = lifetime;

        body = new AdvBody(
                new Vector2(x, y),
                BodyDef.BodyType.DynamicBody
        );
        body.createBody(world);

        fsa = new FSA();
    }

    public void kill() {
        if (is_alive) {
            lifetime = 0f;
            for (AdvBodyPart part : body.getParts()) {
                part.addAction(Actions.sequence(
                        Actions.sizeTo(0f, 0f, 1f),
                        Actions.removeActor())
                );
                part.dispose();
            }
            is_alive = false;
        }
    }

    public boolean isAlive() {
        return is_alive;
    }

    public void updateLife(float delta) {
        if (lifetime < 0f) {
            kill();
        } else {
            if (ctime > state_timer) {
                ctime = 0f;
                stateNeedToChange = true;
            } else {
                ctime += delta;
                lifetime -= delta;
                stateNeedToChange = false;
            }
        }
    }

    public FSA getFsa() {
        return fsa;
    }

    public AdvBody getAdvBody() {
        return body;
    }

    public float getLifetime() {
        return lifetime;
    }

    public boolean isStateNeedToChange() {
        return stateNeedToChange;
    }

    protected void setStateToChange() {
        stateNeedToChange = true;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public void setStateTimer(float state_timer) {
        this.state_timer = state_timer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateLife(delta);
    }

}
