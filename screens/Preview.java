package ru.erked.beelife.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.I18NBundle;
import ru.erked.beelife.Main;
import ru.erked.beelife.systems.AdvScreen;
import ru.erked.beelife.utils.Fonts;
import ru.erked.beelife.utils.Sounds;

import java.util.Locale;

public class Preview extends AdvScreen {

    private float timer = 0f;

    public Preview (Main game) {
        //
        super(game);
        //
    }

    @Override
    public void show () {
        //
        /*  Initialisation of main game systems */
        g.prefs = Gdx.app.getPreferences("bee_life_prefs");
        g.lang = g.prefs.getInteger("language");
        g.atlas = new TextureAtlas("textures/bee_life_resources.atlas");
        g.sounds = new Sounds();

        FileHandle baseFileHandle = Gdx.files.internal("lang/ru_RU");
        Locale locale = new Locale("ru", "RU");
        g.bundle = I18NBundle.createBundle(baseFileHandle, locale);

        g.fonts = new Fonts(g.bundle.get("FONT_CHARS"));

        g.w = Gdx.graphics.getWidth();
        g.h = Gdx.graphics.getHeight();

        g.is_music = g.prefs.getBoolean("is_music_on", true);
        g.is_sound = g.prefs.getBoolean("is_sound_on", true);

        g.music_volume = g.prefs.getFloat("music_volume", 1f);
        g.sound_volume = g.prefs.getFloat("sound_volume", 1f);

        if (g.is_music) {
            g.sounds.music_1.setLooping(true);
            g.sounds.music_1.setVolume(g.music_volume);
            g.sounds.music_1.play();
        }

    }

    @Override
    public void render (float delta) {
        //
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        /* To the next screen. */
        timer += delta;
        if (timer >= 5.5f) g.setScreen(new Menu(g));
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) timer += 5.5f;
        if (Gdx.input.justTouched()) timer += 5.5f;
        //
    }


}