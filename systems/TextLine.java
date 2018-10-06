package ru.erked.beelife.systems;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class TextLine extends Actor {

    private Font font;
    private String text;

    public TextLine (Font font, String text, float x, float y) {
        this.font = font;
        this.text = text;
        setX(x);
        setY(y);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        font.setColor(getColor());
        font.draw(batch, text, getX(), getY());
    }

    @Override
    public float getWidth () {
        return font.getWidth(text);
    }

    @Override
    public float getHeight () {
        return font.getHeight(text);
    }

    public String getText () { return text; }

    public void setText (String text) { this.text = text; }

    public Font getFont() { return font; }

}

