package ru.erked.beelife.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import ru.erked.beelife.Main;
import ru.erked.beelife.physics.AdvBody;
import ru.erked.beelife.physics.AdvBodyPart;
import ru.erked.beelife.systems.AdvCamera;
import ru.erked.beelife.systems.AdvScreen;

import java.util.ArrayList;

public class Field extends AdvScreen {

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private AdvBody player;

    private AdvCamera camera;

    private ArrayList<AdvBody> movingBodies;

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

        camera = new AdvCamera(g.w / 16f, g.h / 16f);
        camera.set(stage.getCamera());

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

        camera.setPosition(player.getX(), player.getY());

        stage.act(delta);
        debugRenderer.render(world, camera.get().combined);
        stage.draw();

        /* Controls */
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.getBody().applyForceToCenter(
                    new Vector2(10f * Main.s_meter, 10f * Main.s_meter),
                    true
            );
        }

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
                new Vector2(10f, 10f),
                BodyDef.BodyType.DynamicBody
        );
        player.createBody(world);

        CircleShape playerShape = new CircleShape();
        playerShape.setRadius(5f);

        FixtureDef fixDef = new FixtureDef();
        fixDef.shape = playerShape;
        fixDef.friction = 0f;
        fixDef.density = 0.1f;
        fixDef.restitution = 0.1f;

        AdvBodyPart mainPart = new AdvBodyPart(
                g.atlas.createSprite("bee_player"),
                -5f,
                -5f,
                10f,
                10f,
                playerShape,
                fixDef
        );

        player.addPart(mainPart);

        movingBodies.add(player);
        //
    }
    private void world_initialization() {
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawBodies(true);
    }
    private void text_initialization() {}
    private void texture_initialization() {}
    private void button_initialization() {}

    private void stage_addition() {
        stage.addActor(player);
        for (AdvBodyPart playerPart : player.getParts())
            stage.addActor(playerPart);
        stage.addActor(camera);
    }

    private void friction() {
        for (AdvBody body : movingBodies) {
            if (body.getBody().getLinearVelocity().len() > 0.01f) {
                body.getBody().setLinearVelocity(
                         new Vector2(
                                 body.getBody().getLinearVelocity().x * 0.9f,
                                 body.getBody().getLinearVelocity().y * 0.9f
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
