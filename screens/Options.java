package ru.erked.beelife.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import ru.erked.beelife.Main;
import ru.erked.beelife.systems.AdvScreen;
import ru.erked.beelife.systems.AdvSprite;
import ru.erked.beelife.systems.Button;
import ru.erked.beelife.systems.TextLine;

import java.util.ArrayList;

public class Options extends AdvScreen {

    /* Sprites */
    private ArrayList<AdvSprite> background;
    private AdvSprite lang_ru;
    private AdvSprite lang_en;
    private AdvSprite sound_on;
    private AdvSprite music_on;
    private AdvSprite scroll_m;
    private AdvSprite scroll_s;
    private AdvSprite r_music;
    private AdvSprite r_sound;

    /* Buttons */
    private Button back;
    private Button dev_mode;

    /* Text */
    private TextLine text_options;
    private TextLine text_dev_mode;

    /* Random */
    private float cursor_x = Gdx.input.getX(); // For rhombus movement
    private float cursor_y = Gdx.input.getY(); // For rhombus movement
    private final int HONEYCOMBS = 10; // Background
    private boolean was_touched = false;
    private boolean s_satisfy_x = false;
    private boolean s_satisfy_y = false;
    private boolean m_satisfy_x = false;
    private boolean m_satisfy_y = false;

    Options (Main game) {
        //
        super(game);
        //
    }

    @Override
    public void show () {
        //
        super.show();

        if (g.is_music && !change_screen) {
            g.sounds.music_1.setLooping(true);
            g.sounds.music_1.setVolume(g.music_volume);
            g.sounds.music_1.play();
        }

        text_initialization();
        texture_initialization();
        button_initialization();
        stage_addition();

        /* For smooth transition btw screens */
        for (Actor act : stage.getActors()) act.getColor().set(act.getColor().r, act.getColor().g, act.getColor().b, 0f);
        //
    }

    @Override
    public void render (float delta) {
        //
        super.render(delta);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        g.sounds.music_1.setVolume(g.music_volume);

        /* Transition btw screens */
        if (background.get(0).getColor().a == 0f) {
            if (change_screen) {
                this.dispose();
                g.setScreen(next_screen);
            } else {
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(1f, 0.5f));
            }
        }

        /* Teleportation of honeycombs */
        for (AdvSprite s : background) {
            if (s.getX() < -g.w / 8f) {
                s.setX(s.getX() + (g.w / 8f) * HONEYCOMBS);
                s.setY(s.getY() - (3f * g.w / 16f));
            }
            if (s.getY() > g.h + (3f * g.w / 16f)) {
                s.setY(s.getY() - (3f * g.w / 16f) * HONEYCOMBS);
            }
        }

        rhombuses_movement();

        stage.act(delta);
        stage.draw();

        /* Updating volume */
        g.music_volume = (r_music.getX() + 0.1f * r_music.getWidth() - scroll_m.getX()) / (scroll_m.getX() + scroll_m.getWidth());
        g.sound_volume = (r_sound.getX() + 0.1f * r_sound.getWidth() - scroll_s.getX()) / (scroll_s.getX() + scroll_s.getWidth());

        /* ESC listener. */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { this.dispose();Gdx.app.exit(); }
        if (Gdx.input.isKeyPressed(Input.Buttons.BACK)) {
            change_screen = true;
            next_screen = new Menu(g);
            for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 0.5f));
        }
        //
    }

    private void rhombuses_movement() {
        //
        /* Move rhombuses using cursor */

        if (Gdx.input.isTouched() && !was_touched) {

            was_touched = true;

            m_satisfy_x = cursor_x >= r_music.getX() &&
                    cursor_x <= r_music.getX() + r_music.getWidth();
            m_satisfy_y = cursor_y >= r_music.getY() &&
                    cursor_y <= r_music.getY() + r_music.getHeight();

            s_satisfy_x = cursor_x >= r_sound.getX() &&
                    cursor_x <= r_sound.getX() + r_sound.getWidth();
            s_satisfy_y = cursor_y >= r_sound.getY() &&
                    cursor_y <= r_sound.getY() + r_sound.getHeight();

        } else if (Gdx.input.isTouched()) {
            if (m_satisfy_x && m_satisfy_y)
                r_music.addAction(Actions.moveBy(Gdx.input.getDeltaX(), 0f));
            if (s_satisfy_x && s_satisfy_y)
                r_sound.addAction(Actions.moveBy(Gdx.input.getDeltaX(), 0f));
        } else if (!Gdx.input.isTouched()) {
            was_touched = false;
        }

        if (r_music.getX() < scroll_m.getX()) r_music.setX(scroll_m.getX());
        if (r_music.getX() + r_music.getWidth() > scroll_m.getX() + scroll_m.getWidth())
            r_music.setX(scroll_m.getX() + scroll_m.getWidth() - r_music.getWidth());
        if (r_sound.getX() < scroll_s.getX()) r_sound.setX(scroll_s.getX());
        if (r_sound.getX() + r_sound.getWidth() > scroll_s.getX() + scroll_s.getWidth())
            r_sound.setX(scroll_s.getX() + scroll_s.getWidth() - r_sound.getWidth());
        cursor_x = Gdx.input.getX();
        cursor_y = g.h - Gdx.input.getY();
        //
    }

    private void text_initialization() {
        //
        text_options = new TextLine(
                g.fonts.f_0S,
                g.bundle.get("options_btn"),
                0f,
                0.9f * g.h
        );
        text_options.setX(0.5f * (g.w - text_options.getWidth()));

        text_dev_mode = new TextLine(
                g.fonts.f_0S,
                g.bundle.get("is_dev_mode"),
                .4f * g.w,
                .1f * g.h
        );
        //
    }
    private void texture_initialization() {
        //
        background = new ArrayList<>();
        for (int i = 0; i < HONEYCOMBS * HONEYCOMBS; i++) {
            background.add(new AdvSprite(
                    g.atlas.createSprite("honeycomb"),
                    (i % HONEYCOMBS) * g.w / 8f,
                    (i / HONEYCOMBS - 1) * (3f * g.w / 16f),
                    g.w / 8f,
                    3f * g.w / 16f
            ));
            background.get(i).addAction(Actions.forever(Actions.moveBy(-(g.w / 8f), (3f * g.w / 16f), 5f)));
        }

        scroll_m = new AdvSprite(
                g.atlas.createSprite("scroll_line"),
                0.15f * g.w,
                0.3125f * g.w,
                0.2f * g.w,
                0.025f * g.w
        );
        scroll_s = new AdvSprite(
                g.atlas.createSprite("scroll_line"),
                0.15f * g.w,
                0.1875f * g.w,
                0.2f * g.w,
                0.025f * g.w
        );

        //
    }
    private void button_initialization() {
        //
        /* --BACK-- */
        back = new Button(
                g,
                .875f * g.w,
                .025f * g.w,
                .1f * g.w,
                g.fonts.f_5.getFont(),
                g.bundle.get("back_btn"),
                1,
                "back_btn"
        );
        back.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                change_screen = true;
                next_screen = new Menu(g);
                back.get().setChecked(false);
                for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 0.5f));
            }
        });
        /* --DEV MODE-- */
        dev_mode = new Button(
                g,
                .325f * g.w,
                .025f * g.w,
                .05f * g.w,
                g.fonts.f_5.getFont(),
                g.is_dev_mode ? "1" : "0",
                2,
                "dev_btn"
        );
        dev_mode.get().setChecked(g.is_dev_mode);
        dev_mode.get().addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                g.is_dev_mode = !(g.is_dev_mode);
                dev_mode.get().setText(g.is_dev_mode ? "1" : "0");
                dev_mode.get().setChecked(g.is_dev_mode);
            }
        });

        /* --LANG_RU-- */
        lang_ru = new AdvSprite(
                g.atlas.createSprite("opt_lang_ru"),
                .025f * g.w,
                .025f * g.w,
                .1f * g.w,
                .1f * g.w
        );
        if (g.lang != 0) lang_ru.setColor(Color.GRAY);
        lang_ru.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                lang_ru.setColor(Color.WHITE);
                lang_en.setColor(Color.GRAY);
                g.lang = 0;
                g.bundle = I18NBundle.createBundle(Gdx.files.internal("lang/ru_RU"));
                text_options.setText(g.bundle.get("options_btn"));
                text_options.setPosition(.5f*(g.w - g.fonts.f_0S.getWidth(g.bundle.get("options_btn"))), text_options.getY());
                text_dev_mode.setText(g.bundle.get("is_dev_mode"));
                back.get().setText(g.bundle.get("back_btn"));
            }
        });

        /* --LANG_EN-- */
        lang_en = new AdvSprite(
                g.atlas.createSprite("opt_lang_en"),
                .15f * g.w,
                .025f * g.w,
                0.1f * g.w,
                0.1f * g.w
        );
        if (g.lang != 1) lang_en.setColor(Color.GRAY);
        lang_en.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                lang_ru.setColor(Color.GRAY);
                lang_en.setColor(Color.WHITE);
                g.lang = 1;
                g.bundle = I18NBundle.createBundle(Gdx.files.internal("lang/en_US"));
                text_options.setText(g.bundle.get("options_btn"));
                text_options.setPosition(.5f*(g.w - g.fonts.f_0S.getWidth(g.bundle.get("options_btn"))), text_options.getY());
                text_dev_mode.setText(g.bundle.get("is_dev_mode"));
                back.get().setText(g.bundle.get("back_btn"));
            }
        });

        /* --SOUND-- */
        sound_on = new AdvSprite(
                g.atlas.createSprite("opt_sound"),
                0.025f * g.w,
                0.15f * g.w,
                0.1f * g.w,
                0.1f * g.w
        );
        if (!g.is_sound) sound_on.setColor(Color.GRAY);
        sound_on.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                if (g.is_sound) {
                    g.is_sound = false;
                    sound_on.setColor(Color.GRAY);
                } else {
                    g.is_sound = true;
                    sound_on.setColor(Color.WHITE);
                }
            }
        });

        /* --MUSIC-- */
        music_on = new AdvSprite(
                g.atlas.createSprite("opt_music"),
                0.025f * g.w,
                0.275f * g.w,
                0.1f * g.w,
                0.1f * g.w
        );
        if (!g.is_music) music_on.setColor(Color.GRAY);
        music_on.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume); // Click sound
                if (g.is_music) {
                    if (g.sounds.music_1.isPlaying()) g.sounds.music_1.stop();
                    g.is_music = false;
                    music_on.setColor(Color.GRAY);
                } else {
                    g.sounds.music_1.setLooping(true);
                    g.sounds.music_1.setVolume(0.5f);
                    g.sounds.music_1.play();
                    g.is_music = true;
                    music_on.setColor(Color.WHITE);
                }
            }
        });

        /* --RHOMBUS-- */
        r_music = new AdvSprite(
                g.atlas.createSprite("rhombus"),
                scroll_m.getX() + g.music_volume * (scroll_m.getX() + scroll_m.getWidth()) - 0.0035f * g.w,
                0.3f * g.w,
                0.05f * g.w,
                0.05f * g.w
        );
        r_sound = new AdvSprite(
                g.atlas.createSprite("rhombus"),
                scroll_s.getX() + g.sound_volume * (scroll_s.getX() + scroll_s.getWidth()) - 0.0035f * g.w,
                0.175f * g.w,
                0.05f * g.w,
                0.05f * g.w
        );

        scroll_m.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume);
                r_music.setPosition(
                        x - .5f*r_music.getWidth() + scroll_m.getX(),
                        r_music.getY()
                );
            }
        });

        scroll_s.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.is_sound) g.sounds.click.play(g.sound_volume);
                r_sound.setPosition(
                        x - .5f*r_sound.getWidth() + scroll_s.getX(),
                        r_sound.getY()
                );
            }
        });

        //
    }

    private void stage_addition () {
        //
        for (AdvSprite s : background) stage.addActor(s);
        stage.addActor(back.get());
        stage.addActor(dev_mode.get());
        stage.addActor(lang_en);
        stage.addActor(lang_ru);
        stage.addActor(music_on);
        stage.addActor(sound_on);
        stage.addActor(scroll_s);
        stage.addActor(scroll_m);
        stage.addActor(r_music);
        stage.addActor(r_sound);
        stage.addActor(text_options);
        stage.addActor(text_dev_mode);
        //
    }

    @Override
    public void resume() {
        if (g.is_music && !change_screen) {
            g.sounds.music_1.setLooping(true);
            g.sounds.music_1.setVolume(g.music_volume);
            g.sounds.music_1.play();
        }
    }

}
