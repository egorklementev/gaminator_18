package ru.erked.beelife.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AdvSprite extends Actor {

    Sprite sprite;
    private boolean drawPartially;
    private float p_x;
    private float p_y;
    private float p_width;
    private float p_height;
    private boolean p_flipX;
    private boolean p_flipY;

    public AdvSprite (Sprite sprite, float x, float y, float width, float height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setOrigin(width / 2f, height / 2f);
        this.sprite = sprite;
        this.sprite.setBounds(x, y, width, height);
        this.sprite.setOriginCenter();
        drawPartially = false;
    }

    public AdvSprite (AdvSprite copy) {
        setX(copy.getX());
        setY(copy.getY());
        setWidth(copy.getWidth());
        setHeight(copy.getHeight());
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        sprite = new Sprite(copy.sprite);
        drawPartially = false;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        update();
        float oldAlpha = getColor().a;
        sprite.setAlpha(getColor().a * parentAlpha);
        if (drawPartially) {
            if (p_flipX && p_flipY) {
                //drawPartFlipXY(batch, p_x, p_y, p_width, p_height);
            } else if (p_flipX) {
                drawPartFlipX(batch, p_x, p_y, p_width, p_height);
            } else if (p_flipY) {
                drawPartFlipY(batch, p_x, p_y, p_width, p_height);
            } else {
                drawPart(batch, p_x, p_y, p_width, p_height);
            }
        } else {
            sprite.draw(batch, parentAlpha);
        }
        sprite.setAlpha(oldAlpha);
    }

    private void drawPartFlipX(Batch batch, float x, float y, float width, float height) {
        Color old = batch.getColor();
        batch.setColor(sprite.getColor());
        batch.draw(
                sprite.getTexture(),
                getX() + getWidth() * (1f - width),
                getY(),
                getOriginX(),
                getOriginY(),
                getWidth() * width,
                getHeight() * height,
                getScaleX(),
                getScaleY(),
                getRotation(),
                (int) (sprite.getRegionX() + sprite.getRegionWidth() * x + sprite.getRegionWidth() * (1f - width)),
                (int) (sprite.getRegionY() + sprite.getRegionHeight() * y),
                (int) (sprite.getRegionWidth() * width),
                (int) (sprite.getRegionHeight() * height),
                sprite.isFlipX(),
                sprite.isFlipY()
        );
        batch.setColor(old);
    }
    private void drawPartFlipY(Batch batch, float x, float y, float width, float height) {
        Color old = batch.getColor();
        batch.setColor(sprite.getColor());
        batch.draw(
                sprite.getTexture(),
                getX(),
                getY() + getHeight() * (1f - height),
                getOriginX(),
                getOriginY(),
                getWidth() * width,
                getHeight() * height,
                getScaleX(),
                getScaleY(),
                getRotation(),
                (int) (sprite.getRegionX() + sprite.getRegionWidth() * x),
                (int) (sprite.getRegionY() + sprite.getRegionHeight() * y),
                (int) (sprite.getRegionWidth() * width),
                (int) (sprite.getRegionHeight() * height),
                sprite.isFlipX(),
                sprite.isFlipY()
        );
        batch.setColor(old);
    }
    private void drawPart (Batch batch, float x, float y, float width, float height) {
        Color old = batch.getColor();
        batch.setColor(sprite.getColor());
        batch.draw(
                sprite.getTexture(),
                getX(),
                getY(),
                getOriginX(),
                getOriginY(),
                getWidth() * width,
                getHeight() * height,
                getScaleX(),
                getScaleY(),
                getRotation(),
                (int) (sprite.getRegionX() + sprite.getRegionWidth() * x),
                (int) (sprite.getRegionY() + sprite.getRegionHeight() * y + sprite.getRegionHeight() * (1f - height)),
                (int) (sprite.getRegionWidth() * width),
                (int) (sprite.getRegionHeight() * height),
                sprite.isFlipX(),
                sprite.isFlipY()
        );
        batch.setColor(old);
    }

    public void setDrawPartially (float x, float y, float width, float height, boolean flipX, boolean flipY) {
        p_x = x;
        p_y = y;
        p_width = width;
        p_height = height;
        p_flipX = flipX;
        p_flipY = flipY;
        drawPartially = true;
    }

    public void setPartialSize (float p_width, float p_height) {
        this.p_width = p_width;
        this.p_height = p_height;
    }

    public Vector2 getPartialSize () {
        return new Vector2(p_width, p_height);
    }

    public void setPartialCoordinates (float p_x, float p_y) {
        this.p_x = p_x;
        this.p_y = p_y;
    }

    public Vector2 getPartialCoordinates () {
        return new Vector2(p_x, p_y);
    }

    public void setPartialFlipY (boolean p_flipY) {
        this.p_flipY = p_flipY;
    }

    public void resetDrawPartially () { drawPartially = false; }

    private void update() {
        sprite.setBounds(getX(), getY(), getWidth(), getHeight());
        sprite.setOrigin(getOriginX(), getOriginY());
        sprite.setRotation(getRotation());
        sprite.setColor(getColor());
    }

    public Sprite getSprite () {
        return sprite;
    }
}
