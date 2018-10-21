package ru.erked.beelife;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.I18NBundle;
import ru.erked.beelife.screens.Preview;
import ru.erked.beelife.utils.Fonts;
import ru.erked.beelife.utils.Sounds;

public class Main extends Game {

	public int lang;
	public TextureAtlas atlas;
	public Sounds sounds;
	public Fonts fonts;
	public Preferences prefs;
	public I18NBundle bundle;
	public float w;
	public float h;
	public static float METER;
	public int level;
	public boolean is_dev_mode;

	/* Values to memorise */
	public boolean is_sound = true;
	public boolean is_music = true;

	public float music_volume = 1f; // From 0f to 1f
	public float sound_volume = 1f; // From 0f to 1f

	public Main(int lang) { this.lang = lang; }

	@Override
	public void create () {
		setScreen(new Preview(this));
	}

	@Override
	public void dispose () {}
}
