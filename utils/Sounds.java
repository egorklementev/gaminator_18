package ru.erked.beelife.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.Arrays;

public class Sounds {

    /* Random */
    public Sound coin;
    public Sound hurt;
    public Sound click;
    public Sound shoot;
    public Sound healing;

    /* Music */
    public Music music_1;
    public Music music_2;

    /* All sounds & music */
    public ArrayList<Sound> s_list;
    public ArrayList<Music> m_list;

    public Sounds () {
        s_list = new ArrayList<>();
        m_list = new ArrayList<>();

        click = Gdx.audio.newSound(Gdx.files.internal("sounds/random/click.wav"));
        shoot = Gdx.audio.newSound(Gdx.files.internal("sounds/random/shoot.wav"));
        healing = Gdx.audio.newSound(Gdx.files.internal("sounds/random/healing.wav"));
        coin = Gdx.audio.newSound(Gdx.files.internal("sounds/random/coin.wav"));
        hurt = Gdx.audio.newSound(Gdx.files.internal("sounds/random/hurt.wav"));

        music_1 = Gdx.audio.newMusic(Gdx.files.internal("sounds/music/music_1.mp3"));
        music_2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/music/music_2.mp3"));

        s_list.add(click);

        m_list.add(music_1);
        m_list.add(music_2);
    }

}
