package ru.erked.beelife.ai;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import ru.erked.beelife.Main;
import ru.erked.beelife.physics.AdvBodyPart;

public class Bullet extends Entity {

    /** @param g instance of Main class
     *  @param world world instance
     *  @param x x-coordinate in meters
     *  @param y y-coordinate in meters
     *  @param size size in units*/
    public Bullet(Main g, World world, float x, float y, float lifetime, float size) {
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
                g.atlas.createSprite("bullet"),
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
    }
}
