package ru.erked.beelife.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;

public class AdvBody extends Actor {

    private Body body;
    private BodyDef bodyDef;
    private ArrayList<AdvBodyPart> parts;

    public AdvBody(Vector2 position, BodyDef.BodyType type) {
        bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = type;

        parts = new ArrayList<>();
    }

    public Body getBody() {
        return body;
    }

    public void addPart(AdvBodyPart part) {
        body.createFixture(part.getFixtureDef());
        parts.add(part);
    }

    public ArrayList<AdvBodyPart> getParts() {
        return parts;
    }

    public void createBody(World world) {
        body = world.createBody(bodyDef);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        for (AdvBodyPart part : parts) {
            part.setPosition(
                    part.getOffsetX() + body.getPosition().x,
                    part.getOffsetY() + body.getPosition().y
            );
        }
    }
}
