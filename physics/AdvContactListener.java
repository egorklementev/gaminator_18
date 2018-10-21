package ru.erked.beelife.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import ru.erked.beelife.screens.Field;

public class AdvContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        // Here it is impossible to delete or create bodies
        // because of the Box2D structure
        // So, we are doing such staff in Field class
        for (int i = Field.pollen.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.pollen.get(i).getBody().getFixtureList().first())) {
                Field.pollen.get(i).setName("for_delete");
            }
        }
        for (int i = Field.waspsN.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.waspsN.get(i).getAdvBody().getBody().getFixtureList().first())) {
                Vector2 direction = new Vector2(
                        Field.player.getX() - Field.waspsN.get(i).getAdvBody().getX(),
                        Field.player.getY() - Field.waspsN.get(i).getAdvBody().getY()
                ).nor().scl(150000f);
                Field.player.getBody().applyForceToCenter(direction, true);
                Field.player.setName("was_hit");
            }
        }
        for (int i = Field.waspsP.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.waspsP.get(i).getAdvBody().getBody().getFixtureList().first())) {
                Vector2 direction = new Vector2(
                        Field.player.getX() - Field.waspsP.get(i).getAdvBody().getX(),
                        Field.player.getY() - Field.waspsP.get(i).getAdvBody().getY()
                ).nor().scl(150000f);
                Field.player.getBody().applyForceToCenter(direction, true);
                Field.player.setName("was_hit");
            }
        }
        for (int i = Field.waspsF.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.waspsF.get(i).getAdvBody().getBody().getFixtureList().first())) {
                Vector2 direction = new Vector2(
                        Field.player.getX() - Field.waspsF.get(i).getAdvBody().getX(),
                        Field.player.getY() - Field.waspsF.get(i).getAdvBody().getY()
                ).nor().scl(150000f);
                Field.player.getBody().applyForceToCenter(direction, true);
                Field.player.setName("was_hit");
            }
        }
        for (int i = Field.bullets.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.bullets.get(i).getAdvBody().getBody().getFixtureList().first())) {
                Vector2 direction = new Vector2(
                        Field.player.getX() - Field.bullets.get(i).getAdvBody().getX(),
                        Field.player.getY() - Field.bullets.get(i).getAdvBody().getY()
                ).nor().scl(50000f);
                Field.player.getBody().applyForceToCenter(direction, true);
                Field.player.setName("was_hit");
            }
        }
        for (int i = Field.ladybugs.size() - 1; i >= 0; --i) {
            if (contact.getFixtureA().equals(Field.player.getBody().getFixtureList().first()) &&
                    contact.getFixtureB().equals(Field.ladybugs.get(i).getAdvBody().getBody().getFixtureList().first())) {
                Field.ladybugs.get(i).setName("consumed");
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
