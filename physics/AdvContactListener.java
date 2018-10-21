package ru.erked.beelife.physics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import ru.erked.beelife.screens.Field;

public class AdvContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        for (int i = Field.pollen.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.pollen.get(i).getBody().getFixtureList().first())) {
                Field.pollen.get(i).setName("for_delete");
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

}
