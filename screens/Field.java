package ru.erked.beelife.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ru.erked.beelife.Main;
import ru.erked.beelife.physics.AdvBody;
import ru.erked.beelife.physics.AdvBodyPart;
import ru.erked.beelife.systems.AdvCamera;
import ru.erked.beelife.systems.AdvScreen;
import ru.erked.beelife.systems.TextLine;

import java.util.ArrayList;

public class Field extends AdvScreen {

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private AdvBody player;
    private AdvBody wall;

    private AdvCamera camera;

    private TextLine test_text;

    private ArrayList<AdvBody> movingBodies;

    private final float METER = 10f;

    Field(Main game) {
        //
        super(game);
        //
    }

    @Override
    public void show() {
        //
        super.show();

        Box2D.init();

        if (g.is_music && !change_screen) {
            g.sounds.music_2.setLooping(true);
            g.sounds.music_2.setVolume(g.music_volume);
            g.sounds.music_2.play();
        }

        camera = new AdvCamera(g.w, g.h);
        stage = new Stage(new FitViewport(g.w, g.h, camera.get()), new SpriteBatch());

        world_initialization();
        bodies_initialization();
        text_initialization();
        texture_initialization();
        button_initialization();
        stage_addition();

        /* For smooth transition btw screens */
        for (Actor act : stage.getActors()) {
            act.getColor().set(act.getColor().r, act.getColor().g, act.getColor().b, 0f);
        }
        //
    }

    @Override
    public void render(float delta) {
        //
        super.render(delta);
        Gdx.gl.glClearColor(0.0625f, 0.5f, 0.125f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        /* Transition btw screens */
        if (stage.getActors().get(0).getColor().a == 0f) {
            if (change_screen) {
                this.dispose();
                g.setScreen(next_screen);
            } else {
                for (Actor act : stage.getActors()) {
                    act.addAction(Actions.alpha(1f, .5f));
                }
            }
        }

        /* TEXT */
        // Set some text here
        test_text.setPosition(camera.getX() - .475f * g.w, camera.getY() - .475f * g.h + g.fonts.f_5.getHeight("A"));

        camera.setPosition(player.getX(), player.getY());

        stage.act(delta);
        debugRenderer.render(world, camera.get().combined.cpy().scl(10f));
        stage.draw();

        playerControls();
        playerRotation();

        friction();

        world.step(1/60f, 6, 2);

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            this.dispose(); Gdx.app.exit();
        }
        //
    }

    private void bodies_initialization() {
        //
        movingBodies = new ArrayList<>();

        player = new AdvBody(
                new Vector2(1f, 1f),
                BodyDef.BodyType.DynamicBody
        );
        player.createBody(world);

        CircleShape playerShape = new CircleShape();
        playerShape.setRadius(5f);

        FixtureDef player_fd = new FixtureDef();
        player_fd.shape = playerShape;
        player_fd.friction = .1f;
        player_fd.density = .1f;
        player_fd.restitution = .1f;

        AdvBodyPart playerPart = new AdvBodyPart(
                g.atlas.createSprite("bee_player"),
                -5f * METER,
                -5f * METER,
                10f * METER,
                10f * METER,
                playerShape,
                player_fd
        );

        player.addPart(playerPart);
        player.setSize(5f * METER, 5f * METER);

        // WALL
        wall = new AdvBody(
                new Vector2(5f, 5f),
                BodyDef.BodyType.DynamicBody
        );
        wall.createBody(world);

        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(20f, 10f);

        FixtureDef wall_fd = new FixtureDef();
        wall_fd.shape = wallShape;
        wall_fd.friction = .1f;

        AdvBodyPart wallPart = new AdvBodyPart(
                g.atlas.createSprite("button_up"),
                -20f * METER,
                -10f * METER,
                40f * METER,
                20f * METER,
                wallShape,
                wall_fd
        );

        wall.addPart(wallPart);

        movingBodies.add(player);
        movingBodies.add(wall);
        //
    }
    private void world_initialization() {
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawBodies(true);
    }
    private void text_initialization() {
        //
        test_text = new TextLine(
                g.fonts.f_5,
                "test",
                0.025f * g.w,
                g.fonts.f_5.getHeight("A") + 0.025f * g.w
        );
        //
    }
    private void texture_initialization() {}
    private void button_initialization() {}

    private void playerControls() {
        //
        if (Gdx.input.isTouched()) {
            float x = Gdx.input.getX() - .5f * g.w;
            float y = (g.h - Gdx.input.getY()) - .5f * g.h;

            if (Math.abs(x) < player.getWidth()) {
                x = 0f;
            }
            if (Math.abs(y) < player.getHeight()) {
                y = 0f;
            }

            test_text.setText("X: " + x + " __ Y: " + y);

            player.getBody().applyForceToCenter(
                    new Vector2(
                            x,
                            y
                    ).nor().scl(150f * METER),
                    true
            );
        }
        //
    }
    private void playerRotation() {
        //
        float x = Gdx.input.getX() - .5f * g.w;
        float y = Gdx.input.getY() - .5f * g.h;
        float angle = (float)(Math.atan2(x, y) + Math.PI);

        player.getBody().setTransform(player.getBody().getPosition(), angle);
        //
    }

    private void stage_addition() {
        stage.addActor(player);
        for (AdvBodyPart part : player.getParts())
            stage.addActor(part);
        stage.addActor(wall);
        for (AdvBodyPart part : wall.getParts())
            stage.addActor(part);
        stage.addActor(test_text);
        stage.addActor(camera);
    }

    private void friction() {
        for (AdvBody body : movingBodies) {
            if (body.getBody().getLinearVelocity().len() > 0.01f) {
                body.getBody().setLinearVelocity(
                         new Vector2(
                                 body.getBody().getLinearVelocity().x * 0.975f,
                                 body.getBody().getLinearVelocity().y * 0.975f
                         )
                );
            }
        }
    }

    @Override
    public void resume() {
        if (g.is_music && !change_screen) {
            g.sounds.music_2.setLooping(true);
            g.sounds.music_2.setVolume(g.music_volume);
            g.sounds.music_2.play();
        }
    }

}
