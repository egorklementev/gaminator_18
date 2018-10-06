package ru.erked.beelife.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import ru.erked.beelife.Main;

import java.util.ArrayList;

public class InGameText extends Actor {

    private ArrayList<TextLine> text_lines;
    private ArrayList<Float> x_offsets;
    private ArrayList<Float> y_offsets;
    private TextType text_type;

    private enum TextType { PLAIN_TEXT, CENTERED_TEXT }

    /** Constructor of the centered text.
     *  @param game - GameStarter instance
     *  @param font - font of the text
     *  @param keys - keys to each string with the line of text
     *  @param x - x position of the whole text
     *  @param y - y position of the whole text
     *  @param indents - the y-position of the text lines */
    private InGameText(Main game, Font font, String[] keys, float x, float y, int[] indents, boolean is_key) {
        text_type = TextType.CENTERED_TEXT;
        text_lines = new ArrayList<>();
        x_offsets = new ArrayList<>();
        y_offsets = new ArrayList<>();
        setX(x);
        setY(y);
        setWidth(0f);
        setHeight(0f);
        for (int i = 0; i < keys.length; ++i) {
            text_lines.add(new TextLine(
                    font,
                    (is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(0) == '%' ?
                            (is_key ? game.bundle.get(keys[i]) : keys[i]).substring(10) :
                            (is_key ? game.bundle.get(keys[i]) : keys[i]),
                    x,
                    y - 1.5f*indents[i]*font.getHeight("A")
            ));
            // Color parsing (format is %255255255 before the word. Example, %000010100apple)
            Color word_color = new Color(1f,1f,1f,1f);
            if ((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(0) == '%') {
                StringBuilder r = new StringBuilder();
                StringBuilder g = new StringBuilder();
                StringBuilder b = new StringBuilder();
                for (int k = 0; k < 3; ++k) r.append((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(k + 1));
                for (int k = 0; k < 3; ++k) g.append((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(k + 4));
                for (int k = 0; k < 3; ++k) b.append((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(k + 7));
                word_color.a = 1.0f;
                word_color.r = Integer.parseInt(r.toString()) / 255f;
                word_color.g = Integer.parseInt(g.toString()) / 255f;
                word_color.b = Integer.parseInt(b.toString()) / 255f;
                text_lines.get(text_lines.size() - 1).setColor(word_color);
            }
            setWidth(Math.max(getWidth(), font.getWidth(is_key ? game.bundle.get(keys[i]) : keys[i])));
        }
        setHeight(1.5f*font.getHeight("A")*indents[indents.length - 1] + font.getHeight("A"));
        for (TextLine t : text_lines) {
            t.setX(x + 0.5f*(getWidth() - t.getWidth()));
            x_offsets.add(t.getX());
        }
        for (int i : indents) y_offsets.add(1.5f*i*font.getHeight("A"));
    }

    /** Constructor of the centered text.
     *  The x & y is set by default to the center of the screen.
     *  @param game - GameStarter instance
     *  @param font - font of the text
     *  @param keys - keys to each string with the line of text
     *  @param indents - the y-position of the text lines */
    public InGameText(Main game, Font font, String[] keys, int[] indents, boolean is_key) {
        this(game, font, keys, 0f, 0f, indents, is_key);
        updateX(0.5f*(game.w - getWidth()));
        updateY(0.5f*(game.h - getHeight()), 0f);
    }

    /** Constructor of the plain text.
     *  @param game - GameStarter instance
     *  @param font - font of the text
     *  @param key - key to the string with the text
     *  @param width - maximum width of the text
     *  @param x - x position
     *  @param y - y position */
    public InGameText(Main game, Font font, String key, float width, float x, float y, boolean is_key) {
        text_type = TextType.PLAIN_TEXT;
        text_lines = new ArrayList<>();
        x_offsets = new ArrayList<>();
        y_offsets = new ArrayList<>();
        setWidth(width);
        setX(x);
        setY(y);
        String[] words = is_key ? game.bundle.get(key).split(" ") : key.split(" ");
        float current_width = 0f;
        float x_offset, y_offset = 0f;
        int words_counter = 0, from = 0;
        for (int i = 0; i < words.length; ++i) {
            if (font.getWidth(words[i].charAt(0) == '%' ? words[i].substring(10) : words[i]) + current_width + words_counter*font.getWidth("  ") < width) {
                current_width += font.getWidth(words[i].charAt(0) == '%' ? words[i].substring(10) : words[i]);
                words_counter++;
            } else {
                StringBuilder raw_words = new StringBuilder();
                for (int j = from; j < from + words_counter; ++j) raw_words.append(words[j].charAt(0) == '%' ? words[j].substring(10) : words[j]);
                float raw_width = font.getWidth(raw_words.toString());
                float space_width = 0f;
                if (words_counter != 1) space_width = (width - raw_width)/(words_counter - 1f);
                if (space_width > font.getWidth("A")) space_width = font.getWidth("A");
                StringBuilder cur_string = new StringBuilder();
                for (int j = from; j < from + words_counter; ++j) {
                    x_offset = font.getWidth(cur_string.toString()) + space_width*(j - from);
                    x_offsets.add(x_offset);
                    text_lines.add(new TextLine(
                            font,
                            words[j].charAt(0) == '%' ? words[j].substring(10) : words[j],
                            getX() + x_offset,
                            getY() - y_offset + 1.5f*font.getHeight("A")
                    ));
                    // Color parsing (format is %255255255 before the word. Example, %000255100apple)
                    Color word_color = new Color(1f,1f,1f,1f);
                    if (words[j].charAt(0) == '%') {
                        StringBuilder r = new StringBuilder();
                        StringBuilder g = new StringBuilder();
                        StringBuilder b = new StringBuilder();
                        for (int k = 0; k < 3; ++k) r.append(words[j].charAt(k + 1));
                        for (int k = 0; k < 3; ++k) g.append(words[j].charAt(k + 4));
                        for (int k = 0; k < 3; ++k) b.append(words[j].charAt(k + 7));
                        word_color.a = 1.0f;
                        word_color.r = Integer.parseInt(r.toString()) / 255f;
                        word_color.g = Integer.parseInt(g.toString()) / 255f;
                        word_color.b = Integer.parseInt(b.toString()) / 255f;
                        text_lines.get(text_lines.size() - 1).setColor(word_color);
                    }

                    cur_string.append(words[j].charAt(0) == '%' ? words[j].substring(10) : words[j]);
                }
                y_offset += 1.5f*font.getHeight("A");
                for (int j = from; j < from + words_counter; ++j) y_offsets.add(y_offset);
                current_width = 0f;
                words_counter = 0;
                from = i--;
            }
        }
        if (words_counter != 0) {
            y_offset += 1.5f*font.getHeight("A");
            StringBuilder cur_string = new StringBuilder();
            float space_width = font.getWidth("  ");
            for (int j = from; j < from + words_counter; ++j) y_offsets.add(y_offset);
            for (int j = from; j < from + words_counter; ++j) {
                x_offset = font.getWidth(cur_string.toString()) + space_width*(j - from);
                x_offsets.add(x_offset);
                text_lines.add(new TextLine(
                        font,
                        words[j].charAt(0) == '%' ? words[j].substring(10) : words[j],
                        getX() + x_offset,
                        getY() - y_offset + 1.5f*font.getHeight("A")
                ));
                // Color parsing (format is %255255255 before the word. Example, %000010100apple)
                Color word_color = new Color(1f,1f,1f,1f);
                if (words[j].charAt(0) == '%') {
                    StringBuilder r = new StringBuilder();
                    StringBuilder g = new StringBuilder();
                    StringBuilder b = new StringBuilder();
                    for (int k = 0; k < 3; ++k) r.append(words[j].charAt(k + 1));
                    for (int k = 0; k < 3; ++k) g.append(words[j].charAt(k + 4));
                    for (int k = 0; k < 3; ++k) b.append(words[j].charAt(k + 7));
                    word_color.a = 1.0f;
                    word_color.r = Integer.parseInt(r.toString()) / 255f;
                    word_color.g = Integer.parseInt(g.toString()) / 255f;
                    word_color.b = Integer.parseInt(b.toString()) / 255f;
                    text_lines.get(text_lines.size() - 1).setColor(word_color);
                }

                cur_string.append(words[j].charAt(0) == '%' ? words[j].substring(10) : words[j]);
            }
        }
        updateY(y, 1.5f*font.getHeight("A"));
        setHeight(y_offset);
    }

    /** Constructor of the plain text.
     *  The x & y is set by default to the center of the screen.
     *  @param game - GameStarter instance
     *  @param font - font of the text
     *  @param key - key to the string with the text
     *  @param width - maximum width of the text */
    public InGameText(Main game, Font font, String key, float width, boolean is_key) {
        this(game, font, key, width, 0.5f*(game.w - width), 0f, is_key);
        updateY(0.5f*(game.h - getHeight()), 1.5f*font.getHeight("A"));
    }

    /** Recreates plain text redefining the h of the text. */
    public void setPlainText(Main game, String key, boolean is_key) {
        Font font = text_lines.get(0).getFont();
        text_lines.clear();
        x_offsets.clear();
        y_offsets.clear();
        String[] words = is_key ? game.bundle.get(key).split(" ") : key.split(" ");
        float current_width = 0f;
        float x_offset, y_offset = 0f;
        int words_counter = 0, from = 0;
        for (int i = 0; i < words.length; ++i) {
            if (font.getWidth(words[i].charAt(0) == '%' ? words[i].substring(10) : words[i]) + current_width + words_counter*font.getWidth("  ") < getWidth()) {
                current_width += font.getWidth(words[i].charAt(0) == '%' ? words[i].substring(10) : words[i]);
                words_counter++;
            } else {
                StringBuilder raw_words = new StringBuilder();
                for (int j = from; j < from + words_counter; ++j) raw_words.append(words[j].charAt(0) == '%' ? words[j].substring(10) : words[j]);
                float raw_width = font.getWidth(raw_words.toString());
                float space_width = 0f;
                if (words_counter != 1) space_width = (getWidth() - raw_width)/(words_counter - 1f);
                if (space_width > font.getWidth("A")) space_width = font.getWidth("A");
                StringBuilder cur_string = new StringBuilder();
                for (int j = from; j < from + words_counter; ++j) {
                    x_offset = font.getWidth(cur_string.toString()) + space_width*(j - from);
                    x_offsets.add(x_offset);
                    text_lines.add(new TextLine(
                            font,
                            words[j].charAt(0) == '%' ? words[j].substring(10) : words[j],
                            getX() + x_offset,
                            getY() - y_offset + 1.5f*font.getHeight("A")
                    ));
                    // Color parsing (format is %255255255 before the word. Example, %000255100apple)
                    Color word_color = new Color(1f,1f,1f,1f);
                    if (words[j].charAt(0) == '%') {
                        StringBuilder r = new StringBuilder();
                        StringBuilder g = new StringBuilder();
                        StringBuilder b = new StringBuilder();
                        for (int k = 0; k < 3; ++k) r.append(words[j].charAt(k + 1));
                        for (int k = 0; k < 3; ++k) g.append(words[j].charAt(k + 4));
                        for (int k = 0; k < 3; ++k) b.append(words[j].charAt(k + 7));
                        word_color.a = 1.0f;
                        word_color.r = Integer.parseInt(r.toString()) / 255f;
                        word_color.g = Integer.parseInt(g.toString()) / 255f;
                        word_color.b = Integer.parseInt(b.toString()) / 255f;
                        text_lines.get(text_lines.size() - 1).setColor(word_color);
                    }

                    cur_string.append(words[j].charAt(0) == '%' ? words[j].substring(10) : words[j]);
                }
                y_offset += 1.5f*font.getHeight("A");
                for (int j = from; j < from + words_counter; ++j) y_offsets.add(y_offset);
                current_width = 0f;
                words_counter = 0;
                from = i--;
            }
        }
        if (words_counter != 0) {
            y_offset += 1.5f*font.getHeight("A");
            StringBuilder cur_string = new StringBuilder();
            float space_width = font.getWidth("  ");
            for (int j = from; j < from + words_counter; ++j) y_offsets.add(y_offset);
            for (int j = from; j < from + words_counter; ++j) {
                x_offset = font.getWidth(cur_string.toString()) + space_width*(j - from);
                x_offsets.add(x_offset);
                text_lines.add(new TextLine(
                        font,
                        words[j].charAt(0) == '%' ? words[j].substring(10) : words[j],
                        getX() + x_offset,
                        getY() - y_offset + 1.5f*font.getHeight("A")
                ));
                // Color parsing (format is %255255255 before the word. Example, %000010100apple)
                Color word_color = new Color(1f,1f,1f,1f);
                if (words[j].charAt(0) == '%') {
                    StringBuilder r = new StringBuilder();
                    StringBuilder g = new StringBuilder();
                    StringBuilder b = new StringBuilder();
                    for (int k = 0; k < 3; ++k) r.append(words[j].charAt(k + 1));
                    for (int k = 0; k < 3; ++k) g.append(words[j].charAt(k + 4));
                    for (int k = 0; k < 3; ++k) b.append(words[j].charAt(k + 7));
                    word_color.a = 1.0f;
                    word_color.r = Integer.parseInt(r.toString()) / 255f;
                    word_color.g = Integer.parseInt(g.toString()) / 255f;
                    word_color.b = Integer.parseInt(b.toString()) / 255f;
                    text_lines.get(text_lines.size() - 1).setColor(word_color);
                }

                cur_string.append(words[j].charAt(0) == '%' ? words[j].substring(10) : words[j]);
            }
        }
        updateY(getY(), 1.5f*font.getHeight("A"));
        setHeight(y_offset);
    }

    /** Recreates centered text */
    public void setCenteredText (Main game, String[] keys, int[] indents, boolean is_key) {
        Font font = text_lines.get(0).getFont();
        text_lines.clear();
        x_offsets.clear();
        y_offsets.clear();
        setWidth(0f);
        setHeight(1.5f*font.getHeight("A")*indents[indents.length - 1] + font.getHeight("A"));
        for (int i = 0; i < keys.length; ++i) {
            text_lines.add(new TextLine(
                    font,
                    (is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(0) == '%' ?
                            (is_key ? game.bundle.get(keys[i]) : keys[i]).substring(10) :
                            (is_key ? game.bundle.get(keys[i]) : keys[i]),
                    getX(),
                    (getY() + getHeight()) - 1.5f*indents[i]*font.getHeight("A")
            ));
            // Color parsing (format is %255255255 before the word. Example, %000010100apple)
            Color word_color = new Color(1f,1f,1f,1f);
            if ((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(0) == '%') {
                StringBuilder r = new StringBuilder();
                StringBuilder g = new StringBuilder();
                StringBuilder b = new StringBuilder();
                for (int k = 0; k < 3; ++k) r.append((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(k + 1));
                for (int k = 0; k < 3; ++k) g.append((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(k + 4));
                for (int k = 0; k < 3; ++k) b.append((is_key ? game.bundle.get(keys[i]) : keys[i]).charAt(k + 7));
                word_color.a = 1.0f;
                word_color.r = Integer.parseInt(r.toString()) / 255f;
                word_color.g = Integer.parseInt(g.toString()) / 255f;
                word_color.b = Integer.parseInt(b.toString()) / 255f;
                text_lines.get(text_lines.size() - 1).setColor(word_color);
            }
            setWidth(Math.max(getWidth(), font.getWidth(is_key ? game.bundle.get(keys[i]) : keys[i])));
        }
        for (TextLine t : text_lines) {
            t.setX(getX() + 0.5f*(getWidth() - t.getWidth()));
            x_offsets.add(t.getX());
        }
        for (int i : indents) y_offsets.add(1.5f*i*font.getHeight("A"));
    }

    private TextType getType() { return text_type; }

    /** Overwritten draw method. Draws each 'TextLine' separately. */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        for (TextLine t : text_lines) {
            t.setColor(t.getColor().r, t.getColor().g, t.getColor().b, getColor().a);
            t.draw(batch, parentAlpha);
        }
    }

    /** Private 'updateX' method for all types of text. */
    private void updateX(float x) {
        for (int i = 0; i < text_lines.size(); ++i) text_lines.get(i).setX(x + x_offsets.get(i));
        setX(x);
    }

    /** Private 'updateY' method for all types of text. */
    private void updateY(float y, float offset) {
        for (int i = 0; i < text_lines.size(); ++i) text_lines.get(i).setY(getHeight() + y - y_offsets.get(i) + offset);
        setY(y);
    }

    /** To call the position update manually, not in every frame */
    public void updatePos (float y_offset) {
        updateX(getX());
        if (getType() == TextType.PLAIN_TEXT)
            updateY(getY(), 1.5f * text_lines.get(0).getHeight());
        if (getType() == TextType.CENTERED_TEXT)
            updateY(getY(), y_offset);
    }

}
