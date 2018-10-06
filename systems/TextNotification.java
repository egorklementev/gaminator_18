package ru.erked.beelife.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class TextNotification extends TextLine {

    private float lifetime;
    private float speed;

    public TextNotification (Font font, String text, float x, float y, float lifetime) {
        super(font, text, x, y);
        this.lifetime = lifetime;
        speed = .5f*font.getHeight("A");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        addAction(Actions.moveBy(0f, speed));
        if (lifetime <= 1f && lifetime >= 0f) addAction(Actions.alpha(lifetime));
        lifetime -= Gdx.graphics.getDeltaTime();
        speed *= 0.9f;
    }

    public boolean isOver () { return lifetime < 0; }

}
