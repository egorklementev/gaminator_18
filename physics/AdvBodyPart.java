package ru.erked.beelife.physics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Disposable;
import ru.erked.beelife.systems.AdvSprite;

public class AdvBodyPart extends AdvSprite implements Disposable {

    private Shape shape;
    private FixtureDef fixtureDef;

    public AdvBodyPart(Sprite sprite, float x, float y, float w, float h, Shape shape, FixtureDef fixtureDef) {
        super(sprite, x, y, w, h);
        this.shape = shape;
        this.fixtureDef = fixtureDef;
    }

    public Shape getShape() {
        return shape;
    }

    FixtureDef getFixtureDef() {
        return fixtureDef;
    }

    @Override
    public void dispose() {
        shape.dispose();
    }

}
