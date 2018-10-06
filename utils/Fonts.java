package ru.erked.beelife.utils;

import com.badlogic.gdx.graphics.Color;
import ru.erked.beelife.systems.Font;

public class Fonts {

    public Font f_20;
    public Font f_15;
    public Font f_15S;
    public Font f_15Black;
    public Font f_10;
    public Font f_10S;
    public Font f_10Black;
    public Font f_5;
    public Font f_5S;
    public Font f_5Black;
    public Font f_0;
    public Font f_0S;

    public Fonts (String FONT_CHARS) {
        int min_size = 75;
        f_20 = new Font("fonts/regular.ttf", min_size + 20, Color.WHITE, FONT_CHARS);
        f_15 = new Font("fonts/regular.ttf", min_size + 15, Color.WHITE, FONT_CHARS);
        f_15S = new Font("fonts/regular.ttf", min_size + 15, Color.WHITE, 2, 2, Color.BLACK, FONT_CHARS);
        f_15Black = new Font("fonts/regular.ttf", min_size + 15, Color.BLACK, FONT_CHARS);
        f_10 = new Font("fonts/regular.ttf", min_size + 10, Color.WHITE, FONT_CHARS);
        f_10S = new Font("fonts/regular.ttf", min_size + 10, Color.WHITE, 3, 3, Color.BLACK, FONT_CHARS);
        f_10Black = new Font("fonts/regular.ttf", min_size + 10, Color.BLACK, FONT_CHARS);
        f_5 = new Font("fonts/regular.ttf", min_size + 5, Color.WHITE, FONT_CHARS);
        f_5S = new Font("fonts/regular.ttf", min_size + 5, Color.WHITE, 4, 4, Color.BLACK, FONT_CHARS);
        f_5Black = new Font("fonts/regular.ttf", min_size + 5, Color.BLACK, FONT_CHARS);
        f_0 = new Font("fonts/regular.ttf", min_size, Color.WHITE, FONT_CHARS);
        f_0S = new Font("fonts/regular.ttf", min_size, Color.WHITE, 5, 5, Color.BLACK, FONT_CHARS);
    }

}
