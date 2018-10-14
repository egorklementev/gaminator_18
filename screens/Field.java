package ru.erked.beelife.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import ru.erked.beelife.systems.AdvSprite;
import ru.erked.beelife.systems.TextLine;

import java.util.ArrayList;

public class Field extends AdvScreen {

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private AdvBody player;
    private AdvBody queen;
    private Animation<TextureRegion> a_player;

    private ArrayList<AdvBody> movingBodies;

    private ArrayList<AdvSprite> hive;
    private ArrayList<AdvSprite> ground;

    private AdvCamera camera;

    private TextLine test_text;

    private float state_time = 0f;

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
        animation_initialization();
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

        state_time += delta;

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

        /* TEXT */
        test_text.setText(Gdx.graphics.getFramesPerSecond() + "");
        test_text.setPosition(
                camera.getX() - .475f * g.w,
                camera.getY() - .475f * g.h + g.fonts.f_5.getHeight("A")
        );

        stage.act(delta);
        stage.draw();
        //debugRenderer.render(world, camera.get().combined.cpy().scl(Main.METER));

        playerControls();
        playerRotation();
        if (player.getBody().getLinearVelocity().len() > 10f) {
            player.getParts().get(0).getSprite().setRegion(a_player.getKeyFrame(state_time, true));
        }
        friction();

        world.step(1/60f, 6, 2);

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            this.dispose(); Gdx.app.exit();
        }
        //
    }

    private void animation_initialization() {
        //
        a_player = new Animation<>(
                0.05f,
                g.atlas.findRegions("bee_player"),
                Animation.PlayMode.LOOP
        );
        //
    }
    private void bodies_initialization() {
        //
        movingBodies = new ArrayList<>();

        // PLAYER
        player = new AdvBody(
                new Vector2(5f * Main.METER, 10f * Main.METER),
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
                g.atlas.createSprite("bee_player", 1),
                -5f * Main.METER,
                -5f * Main.METER,
                10f * Main.METER,
                10f * Main.METER,
                playerShape,
                player_fd
        );

        player.addPart(playerPart);
        player.setSize(5f * Main.METER, 5f * Main.METER);

        //QUEEN
        queen = new AdvBody(
                new Vector2(12.5f * Main.METER, 6f * Main.METER),
                BodyDef.BodyType.StaticBody
        );
        queen.createBody(world);
        CircleShape queenShape = new CircleShape();
        queenShape.setRadius(20f);
        FixtureDef queen_fd = new FixtureDef();
        queen_fd.shape = queenShape;
        AdvBodyPart queenPart = new AdvBodyPart(
                g.atlas.createSprite("bee_queen"),
                -20f * Main.METER,
                -20f * Main.METER,
                40f * Main.METER,
                40f * Main.METER,
                queenShape,
                queen_fd
        );
        queen.addPart(queenPart);

        // WALLS
        AdvBody walls = new AdvBody(
                new Vector2(),
                BodyDef.BodyType.StaticBody
        );
        walls.createBody(world);

        ChainShape wallsShape = new ChainShape();
        wallsShape.createChain(
                new float[]{
                        0f, 0f,
                        0f, 100f * Main.METER,
                        100f * Main.METER, 100f * Main.METER,
                        100f * Main.METER, 0f,
                        0f, 0f,
                }
        );

        FixtureDef walls_fd = new FixtureDef();
        walls_fd.shape = wallsShape;

        walls.getBody().createFixture(walls_fd);

        movingBodies.add(player);
        //
    }
    private void world_initialization() {
        world = new World(new Vector2(0f, 0f), true);
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
    private void texture_initialization() {
        //
        hive_bound();
        ground_bound();
        //
    }
    private void button_initialization() {}
    private void ground_bound() {
        // GROUND & BOUND
        ground = new ArrayList<>();
        for (int x = 0; x < 20; ++x) {
            for (int y = 0; y < 20; ++y) {
                ground.add(new AdvSprite(
                        g.atlas.createSprite("ground"),
                        x * 50f * Main.METER,
                        y * 50f * Main.METER,
                        50f * Main.METER,
                        50f * Main.METER
                ));
            }
        }
        for (int x = 0; x < 40; ++x) {
            // Down
            ground.add(new AdvSprite(
                    g.atlas.createSprite("bound_down"),
                    x * 25f * Main.METER,
                    0f,
                    25f * Main.METER,
                    25f * Main.METER
            ));
            // Up
            ground.add(new AdvSprite(
                    g.atlas.createSprite("bound_up"),
                    x * 25f * Main.METER,
                    39f * 25f * Main.METER,
                    25f * Main.METER,
                    25f * Main.METER
            ));
            // Left
            ground.add(new AdvSprite(
                    g.atlas.createSprite("bound_left"),
                    0f,
                    x * 25f * Main.METER,
                    25f * Main.METER,
                    25f * Main.METER
            ));
            // Right
            ground.add(new AdvSprite(
                    g.atlas.createSprite("bound_right"),
                    39f * 25f * Main.METER,
                    x * 25f * Main.METER,
                    25f * Main.METER,
                    25f * Main.METER
            ));
        }
        for (int l = 0; l < 40; ++l) {
            // Up
            for (int w = 0; w < 4; ++w) {
                ground.add(new AdvSprite(
                        g.atlas.createSprite("bound"),
                        l * 25f * Main.METER,
                        (w + 40) * 25f * Main.METER,
                        25f * Main.METER,
                        25f * Main.METER
                ));
            }
            // Down
            for (int w = 0; w < 4; ++w) {
                ground.add(new AdvSprite(
                        g.atlas.createSprite("bound"),
                        l * 25f * Main.METER,
                        (-w - 1) * 25f * Main.METER,
                        25f * Main.METER,
                        25f * Main.METER
                ));
            }
        }
        for (int l = 0; l < 48; ++l) {
            // Left
            for (int w = 0; w < 5; ++w) {
                ground.add(new AdvSprite(
                        g.atlas.createSprite("bound"),
                        (-w - 1) * 25f * Main.METER,
                        (l - 4) * 25f * Main.METER,
                        25f * Main.METER,
                        25f * Main.METER
                ));
            }
            // Right
            for (int w = 0; w < 5; ++w) {
                ground.add(new AdvSprite(
                        g.atlas.createSprite("bound"),
                        (w + 40) * 25f * Main.METER,
                        (l - 4) * 25f * Main.METER,
                        25f * Main.METER,
                        25f * Main.METER
                ));
            }
        }
    }
    private void hive_bound() {
        //
        hive = new ArrayList<>();
        for (int x = 0; x < 9; ++x) {
            for (int y = 0; y < 6; ++y) {
                hive.add(new AdvSprite(
                        g.atlas.createSprite("honeycomb"),
                        50f * Main.METER + x * 16f * Main.METER,
                        50f * Main.METER + y * 24f * Main.METER,
                        16f * Main.METER,
                        24f * Main.METER
                ));
            }
        }
        for (int x = 0; x < 9; ++x) {
            hive.add(new AdvSprite(
                    g.atlas.createSprite("honeycomb_down"),
                    50f * Main.METER + x * 16f * Main.METER,
                    26f * Main.METER,
                    16f * Main.METER,
                    24f * Main.METER
            ));
            hive.add(new AdvSprite(
                    g.atlas.createSprite("honeycomb_up"),
                    50f * Main.METER + x * 16f * Main.METER,
                    194f * Main.METER,
                    16f * Main.METER,
                    24f * Main.METER
            ));
        }
        for (int y = 0; y < 6; ++y) {
            hive.add(new AdvSprite(
                    g.atlas.createSprite("honeycomb_left"),
                    34f * Main.METER,
                    50f * Main.METER + y * 24f * Main.METER,
                    16f * Main.METER,
                    24f * Main.METER
            ));
            hive.add(new AdvSprite(
                    g.atlas.createSprite("honeycomb_right"),
                    194f * Main.METER,
                    50f * Main.METER + y * 24f * Main.METER,
                    16f * Main.METER,
                    24f * Main.METER
            ));
        }
        hive.add(new AdvSprite(
                g.atlas.createSprite("honeycomb_left_down"),
                34f * Main.METER,
                26f * Main.METER,
                16f * Main.METER,
                24f * Main.METER
        ));
        hive.add(new AdvSprite(
                g.atlas.createSprite("honeycomb_left_up"),
                34f * Main.METER,
                194f * Main.METER,
                16f * Main.METER,
                24f * Main.METER
        ));
        hive.add(new AdvSprite(
                g.atlas.createSprite("honeycomb_right_down"),
                194f * Main.METER,
                26f * Main.METER,
                16f * Main.METER,
                24f * Main.METER
        ));
        hive.add(new AdvSprite(
                g.atlas.createSprite("honeycomb_right_up"),
                194f * Main.METER,
                194f * Main.METER,
                16f * Main.METER,
                24f * Main.METER
        ));
        //
    }

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

            player.getBody().applyForceToCenter(
                    new Vector2(
                            x,
                            y
                    ).nor().scl(150f * Main.METER),
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

        // Queen Rotation
        float p_x = player.getX() - queen.getX();
        float p_y = player.getY() - queen.getY();
        float q_angle = (float)(Math.atan2(p_x, p_y));

        queen.getBody().setTransform(queen.getBody().getPosition(), -q_angle);

        //
    }

    private void stage_addition() {
        for (AdvSprite s : ground)
            stage.addActor(s);
        for (AdvSprite s : hive)
            stage.addActor(s);
        stage.addActor(player);
        for (AdvBodyPart part : player.getParts())
            stage.addActor(part);
        stage.addActor(queen);
        for (AdvBodyPart part : queen.getParts())
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
            if (Math.abs(body.getBody().getAngularVelocity()) > 0.01f) {
                body.getBody().setAngularVelocity(
                        body.getBody().getAngularVelocity() * 0.975f
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
