package ru.erked.beelife.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ru.erked.beelife.Main;

import java.util.ArrayList;

public class Dialog extends AdvSprite {

    private AdvSprite picture;
    private Animation<TextureRegion> picture_anim;
    private InGameText text;
    private ArrayList<Button> buttons = new ArrayList<>();
    private float state_time = 0f;

    public Dialog (Main game, float x, float y, float width, String key, boolean is_key, String back_btn) {
        super(game.atlas.createSprite("dialog"), x, y, width, 0.5f * width);
        text = new InGameText(game, game.fonts.f_10S, key, 0.9f*width, x + 0.05f*width, y + 0.2f*width, is_key);
        text.setY(getY() + .875f*getHeight() - text.getHeight());
        text.updatePos(0f);
        addButton(game, back_btn, new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.is_sound) game.sounds.click.play(game.sound_volume);
                close();
            }
        });
    }

    public Dialog (Main game, float width, String key, boolean is_key, String back_btn) {
        this(game, 0.5f*(game.w - width), 0.5f*(game.h - 0.5f*width), width, key, is_key, back_btn);
    }

    public Dialog (Main game, float x, float y, float width, String key, boolean is_key, String picture, String back_btn) {
        super(game.atlas.createSprite("picture_dialog"), x, y, width, width);
        float picture_height = 0.5f * width;
        float picture_width = picture_height * (game.atlas.createSprite(picture).getWidth() / game.atlas.createSprite(picture).getHeight());
        this.picture = new AdvSprite(
                game.atlas.createSprite(picture),
                x + 0.5f * (width - picture_width),
                y + 0.4f * width,
                picture_width,
                picture_height
        );
        text = new InGameText(game, game.fonts.f_15S, key, 0.9f*width, x + 0.05f*width, y + 0.2f*width, is_key);
        addButton(game, back_btn, new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.is_sound) game.sounds.click.play(game.sound_volume);
                close();
            }
        });
    }

    public Dialog (Main game, float width, String key, boolean is_key, String picture, String back_btn) {
        this(game, 0.5f*(game.w - width), 0.5f*(game.h - 0.5f*width), width, key, is_key, picture, back_btn);
    }

    public Dialog (Main game, float x, float y, float width, String key, boolean is_key, String picture, float duration, Animation.PlayMode playmode, String back_btn) {
        this(game, x, y, width, key, is_key, picture, back_btn);
        picture_anim = new Animation<>(
                duration,
                game.atlas.findRegions(picture),
                playmode
        );
    }

    public Dialog (Main game, float width, String key, boolean is_key, String picture, float duration, Animation.PlayMode playmode, String back_btn) {
        this(game, 0.5f*(game.w - width), 0.5f*(game.h - width), width, key, is_key, picture, duration, playmode, back_btn);
    }

    public void addButton (Main game, String text_btn, ClickListener listeners) {
        for (Button b : buttons) b.get().addAction(Actions.moveBy((-3f/4f)*b.get().getWidth(), 0f));
        if (buttons.size()  > 0) {
            buttons.add(new Button(
                    game,
                    buttons.get(buttons.size() - 1).get().getX() + 1.5f * buttons.get(buttons.size() - 1).get().getWidth(),
                    getY() + 0.05f * getWidth(),
                    0.2f * getWidth(),
                    game.fonts.f_15.getFont(),
                    game.bundle.get(text_btn),
                    1,
                    "dialog_btn_" + buttons.size()
            ));
            buttons.get(buttons.size() - 1).get().addListener(listeners);
        } else {
            buttons.add(new Button(
                    game,
                    getX() + .4f*getWidth(),
                    getY() + .05f * getWidth(),
                    .2f * getWidth(),
                    game.fonts.f_15.getFont(),
                    game.bundle.get(text_btn),
                    1,
                    "dialog_btn_" + buttons.size()
            ));
            buttons.get(buttons.size() - 1).get().addListener(listeners);
        }
    }

    public ArrayList<Button> getButtons () {
        return buttons;
    }

    public InGameText getText() {
        return text;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        update();
        sprite.draw(batch, parentAlpha);
        text.draw(batch, parentAlpha);
        for (Button b : buttons) {
            b.get().draw(batch, parentAlpha);
            b.get().setChecked(false);
        }
        if (picture_anim != null) {
            state_time += Gdx.graphics.getDeltaTime();
            picture.getSprite().setRegion(picture_anim.getKeyFrame(state_time, true));
        }
        if (picture != null) {
            picture.setColor(getColor());
            picture.draw(batch, parentAlpha);
        }
    }

    private void close () {
        text.getParent().removeActor(text);
        for (Button b : buttons) b.get().getParent().removeActor(b.get());
        getParent().removeActor(this);
    }

    private void update () {
        sprite.setBounds(getX(), getY(), getWidth(), getHeight());
        sprite.setRotation(getRotation());
        sprite.setColor(getColor());
    }
}
