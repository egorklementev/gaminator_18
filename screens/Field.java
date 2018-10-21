package ru.erked.beelife.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ru.erked.beelife.Main;
import ru.erked.beelife.ai.*;
import ru.erked.beelife.physics.AdvBody;
import ru.erked.beelife.physics.AdvBodyPart;
import ru.erked.beelife.physics.AdvContactListener;
import ru.erked.beelife.systems.*;

import java.util.ArrayList;

public class Field extends AdvScreen {

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private AdvBody queen;
    private AdvBody[] q_guards;
    private Animation<TextureRegion> a_player;
    private ArrayList<AdvBody> movingBodies;
    private ArrayList<AdvSprite> hive;
    private ArrayList<AdvSprite> ground;
    private AdvCamera camera;
    private TextLine test_text;
    private TextLine score_text;
    private TextLine goal_text;
    private ArrayList<Ant> ants;
    private ArrayList<Bug> bugs;
    private AdvSprite map;
    private AdvSprite map_player;
    private AdvSprite map_queen;
    private ArrayList<AdvSprite> map_pollen;
    private ArrayList<AdvSprite> flowers;
    private ArrayList<AdvSprite> ui_hp;
    private ArrayList<AdvSprite> queen_clouds;
    private boolean is_map_big = false;
    private int goal;
    private int hp = 3;
    private int score = 0;
    private float hit_timer = 0f;

    public static AdvBody player;
    public static ArrayList<AdvBody> pollen;
    public static ArrayList<Bug> ladybugs;
    public static ArrayList<WaspN> waspsN;
    public static ArrayList<WaspP> waspsP;
    public static ArrayList<WaspF> waspsF;
    public static ArrayList<Bullet> bullets;
    public static float state_time = 0f;

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

        goal = (g.level + 1) * 100;

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
        particle_initialization();
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
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        state_time += delta;

        // region Transition btw screens
        if (stage.getActors().get(0).getColor().a == 0f) {
            if (change_screen) {
                this.dispose();
                g.setScreen(next_screen);
            } else {
                for (Actor act : stage.getActors()) {
                    if (act instanceof AdvSprite) {
                        if (!queen_clouds.contains(act)) {
                            act.addAction(Actions.alpha(1f, .5f));
                        }
                    } else {
                        act.addAction(Actions.alpha(1f, .5f));
                    }
                }
            }
        }
        // endregion

        // region WINNING CONDITION
        if (score >= goal && !change_screen) {
            g.level++;
            for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 5f));
            stage.addActor(new TextNotification(
                    g.fonts.f_0S,
                    g.bundle.get("level_complete"),
                    camera.getX() - .5f * g.fonts.f_0S.getWidth(g.bundle.get("level_complete")),
                    camera.getY(),
                    4f
            ));
            change_screen = true;
            next_screen = new Field(g);
        }
        // endregion
        // region END OF THE GAME
        if (hp == 0 && !change_screen) {
            for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, 5f));
            stage.addActor(new TextNotification(
                    g.fonts.f_0S,
                    g.bundle.get("game_over"),
                    camera.getX() - .5f * g.fonts.f_0S.getWidth(g.bundle.get("game_over")),
                    camera.getY(),
                    4f
            ));
            change_screen = true;
            next_screen = new Field(g);
        }
        // endregion

        camera.setPosition(player.getX(), player.getY());

        // region UI update
        /* TEXT */
        test_text.setText(
                "FPS: " + Gdx.graphics.getFramesPerSecond() + "\n"
                        + "Stage actors: " + stage.getActors().size
        );
        test_text.setPosition(
                camera.getX() - .475f * g.w,
                camera.getY() - .475f * g.h + 2f * g.fonts.f_5.getHeight("A")
        );
        score_text.setText(g.bundle.get("score") + ": " + score);
        score_text.setPosition(
                camera.getX() + 0.475f * g.w - g.fonts.f_5.getWidth(g.bundle.get("score") + ": " + score),
                camera.getY() + 0.475f * g.h
        );
        score_text.toFront();
        goal_text.setPosition(
                camera.getX() + 0.475f * g.w - g.fonts.f_5.getWidth(g.bundle.get("goal") + ": " + goal),
                camera.getY() + 0.475f * g.h - 1.5f * g.fonts.f_5.getHeight("A")
        );
        goal_text.toFront();

        /* MAP */
        map.setPosition(
                camera.getX() - .475f * g.w,
                camera.getY() + .475f * g.h - map.getHeight()
        );
        map.toFront();
        map_queen.setPosition(
                map.getX() + 0.15f * map.getWidth(),
                map.getY() + 0.1f * map.getHeight()
        );
        map_queen.toFront();
        float x, y; // For map objects
        for (int i = 0; i < pollen.size(); ++i) {
            x = .8125f * map.getWidth() * (pollen.get(i).getX() / (1000f * Main.METER));
            y = .8125f * map.getHeight() * (pollen.get(i).getY() / (1000f * Main.METER));
            map_pollen.get(i).setPosition(
                    camera.getX() - .475f * g.w + .09375f * map.getWidth() + x - .5f * map_pollen.get(i).getWidth(),
                    camera.getY() + .475f * g.h - .90625f * map.getHeight() + y - .5f * map_pollen.get(i).getHeight()
            );
            map_pollen.get(i).toFront();
        }
        x = .8125f * map.getWidth() * (player.getX() / (1000f * Main.METER));
        y = .8125f * map.getHeight() * (player.getY() / (1000f * Main.METER));
        map_player.setPosition(
                camera.getX() - .475f * g.w + .09375f * map.getWidth() + x - .5f * map_player.getWidth(),
                camera.getY() + .475f * g.h - .90625f * map.getHeight() + y - .5f * map_player.getHeight()
        );
        map_player.toFront();

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (is_map_big) {
                is_map_big = false;
                map.addAction(Actions.parallel(
                        Actions.sizeBy(-.1f * g.w, -.1f * g.w, 0.25f),
                        Actions.moveBy(0f, .05f * g.w, 0.25f)
                ));
                map_player.addAction(Actions.sizeBy(-.005f * g.w, -.005f * g.w, .25f));
                map_queen.addAction(Actions.sizeBy(-.01f * g.w, -.01f * g.w, .25f));
                for (AdvSprite s : map_pollen) {
                    s.addAction(Actions.sizeBy(-.0025f * g.w, -.0025f * g.w, .25f));
                }
            } else {
                is_map_big = true;
                map.addAction(Actions.parallel(
                        Actions.sizeBy(.1f * g.w, .1f * g.w, .25f),
                        Actions.moveBy(0f, -.05f * g.w, .25f)
                ));
                map_player.addAction(Actions.sizeBy(.005f * g.w, .005f * g.w, .25f));
                map_queen.addAction(Actions.sizeBy(.01f * g.w, .01f * g.w, .25f));
                for (AdvSprite s : map_pollen) {
                    s.addAction(Actions.sizeBy(.0025f * g.w, .0025f * g.w, .25f));
                }
            }
        }

        /* HEALTH BAR */
        for (int i = 0; i < hp; i++) {
            ui_hp.get(i).getSprite().setRegion(g.atlas.findRegion("heart"));
            ui_hp.get(i).setPosition(
                    camera.getX() + .3725f * g.w + i * 0.0425f * g.w,
                    camera.getY() - .485f * g.h
            );
            ui_hp.get(i).toFront();
        }
        for (int i = hp; i < 3; i++) {
            ui_hp.get(i).getSprite().setRegion(g.atlas.findRegion("broken_heart"));
            ui_hp.get(i).setPosition(
                    camera.getX() + .3725f * g.w + i * 0.0425f * g.w,
                    camera.getY() - .485f * g.h
            );
            ui_hp.get(i).toFront();
        }
        // endregion
        // region Entities spawn
        if (MathUtils.random() > 0.9900f) {
            spawnBug();
        }
        if (g.level > 1 && MathUtils.random() > 0.99875f) {
            spawnLadybug();
        }
        if ((g.level == 2 || g.level == 3) && MathUtils.random() > 0.9950f) {
            spawnWaspP();
        }
        if (g.level > 3 && MathUtils.random() > 0.9950f) {
            spawnWaspN();
        }
        if (g.level > 5 && MathUtils.random() > 0.9975f) {
            spawnWaspF();
        }
        if (MathUtils.random() > 0.9925f) {
            spawnPollen();
        }
        // endregion

        stage.act(delta);
        stage.draw();
        if (g.is_dev_mode) {
            debugRenderer.render(world, camera.get().combined.cpy().scl(Main.METER));
        }

        rotations();
        playerControls();
        if (player.getBody().getLinearVelocity().len() > 10f) {
            player.getParts().get(0).getSprite().setRegion(a_player.getKeyFrame(state_time, true));
        }

        // region WORLD
        friction();
        if (hp > 0 && score < goal) {
            world.step(1 / 60f, 6, 2);
        }
        // endregion

        collectPollen();
        collectLadybugs();
        fatShooting();

        checkToRemove(bugs);
        checkToRemove(waspsN);
        checkToRemove(waspsP);
        checkToRemove(waspsF);
        checkToRemove(bullets);
        checkToRemove(ladybugs);

        // region PLAYER HIT
        float hit_time = 3f;
        if (hit_timer > hit_time && player.getName() != null && player.getName().equals("was_hit")) {
            hit_timer = 0f;
            player.setName(null);
            hitPlayer();
        } else {
            hit_timer += delta;
        }
        if (hit_timer < hit_time) {
            player.setName(null);
            if (!player.getParts().get(0).hasActions()) {
                player.getParts().get(0).addAction(Actions.sequence(
                        Actions.alpha(.25f, .25f),
                        Actions.alpha(1f, .25f)
                ));
            }
        }
        // endregion

        /* ESC listener */
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            change_screen = true;
            next_screen = new Menu(g);
            g.sounds.music_2.stop();
            for (Actor act : stage.getActors()) act.addAction(Actions.alpha(0f, .5f));
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
        ants = new ArrayList<>();
        bugs = new ArrayList<>();
        waspsN = new ArrayList<>();
        waspsP = new ArrayList<>();
        waspsF = new ArrayList<>();
        pollen = new ArrayList<>();
        bullets = new ArrayList<>();
        ladybugs = new ArrayList<>();
        movingBodies = new ArrayList<>();

        // region PLAYER_INIT
        player = new AdvBody(
                new Vector2(60f, 100f),
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
        // endregion
        // region QUEEN_INIT
        queen = new AdvBody(
                new Vector2(125f, 60f),
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
        // endregion
        // region QUEEN_GUARD_INIT
        q_guards = new AdvBody[2];
        q_guards[0] = new AdvBody(
                new Vector2(150f, 50f),
                BodyDef.BodyType.StaticBody
        );
        q_guards[0].createBody(world);
        q_guards[1] = new AdvBody(
                new Vector2(100f, 50f),
                BodyDef.BodyType.StaticBody
        );
        q_guards[1].createBody(world);

        CircleShape guardShape = new CircleShape();
        guardShape.setRadius(5f);

        FixtureDef guard_fd = new FixtureDef();
        guard_fd.shape = playerShape;
        guard_fd.friction = .1f;
        guard_fd.density = .1f;
        guard_fd.restitution = .1f;

        AdvBodyPart guardPart_1 = new AdvBodyPart(
                g.atlas.createSprite("bee_player", 1),
                -5f * Main.METER,
                -5f * Main.METER,
                10f * Main.METER,
                10f * Main.METER,
                guardShape,
                guard_fd
        );
        AdvBodyPart guardPart_2 = new AdvBodyPart(
                g.atlas.createSprite("bee_player", 1),
                -5f * Main.METER,
                -5f * Main.METER,
                10f * Main.METER,
                10f * Main.METER,
                guardShape,
                guard_fd
        );

        q_guards[0].addPart(guardPart_1);
        q_guards[1].addPart(guardPart_2);
        // endregion
        // region WALLS_INIT
        AdvBody walls = new AdvBody(
                new Vector2(),
                BodyDef.BodyType.StaticBody
        );
        walls.createBody(world);

        ChainShape wallsShape = new ChainShape();
        wallsShape.createChain(
                new float[]{
                        0f, 0f,
                        0f, 1000f,
                        1000f, 1000f,
                        1000f, 0f,
                        0f, 0f,
                }
        );

        FixtureDef walls_fd = new FixtureDef();
        walls_fd.shape = wallsShape;

        walls.getBody().createFixture(walls_fd);
        // endregion
        // region HIVE_INIT
        AdvBody hive = new AdvBody(
                new Vector2(),
                BodyDef.BodyType.StaticBody
        );
        hive.createBody(world);

        ChainShape hiveShape = new ChainShape();
        hiveShape.createChain(
                new float[]{
                        110f, 212.5f,
                        50f, 212.5f,
                        50f, 33.5f,
                        193.75f, 33.5f,
                        193.75f, 212.5f,
                        133.75f, 212.5f
                }
        );

        FixtureDef hive_fd = new FixtureDef();
        hive_fd.shape = hiveShape;

        hive.getBody().createFixture(hive_fd);
        // endregion

        movingBodies.add(player);
        //
    }
    private void world_initialization() {
        world = new World(new Vector2(0f, 0f), true);
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawBodies(true);
        world.setContactListener(new AdvContactListener());
    }
    private void text_initialization() {
        //
        test_text = new TextLine(
                g.fonts.f_5S,
                "test",
                0.025f * g.w,
                g.fonts.f_5.getHeight("A") + 0.025f * g.w
        );
        score_text = new TextLine(
                g.fonts.f_5S,
                g.bundle.get("score") + ": " + score,
                0.975f * g.w - g.fonts.f_5.getWidth(g.bundle.get("score") + ": " + score),
                0.975f * g.h
        );
        goal_text = new TextLine(
                g.fonts.f_5S,
                g.bundle.get("goal") + ": " + goal,
                0.975f * g.w - 1.5f * g.fonts.f_5.getWidth(g.bundle.get("goal") + ": " + goal),
                0.975f * g.h
        );
        //
    }
    private void texture_initialization() {
        hive_bound();
        ground_bound();
        // region UI
        map = new AdvSprite(
                g.atlas.createSprite("map"),
                0f,
                0f,
                20f * Main.METER,
                20f * Main.METER
        );
        map_player = new AdvSprite(
                g.atlas.createSprite("map_player"),
                0f,
                0f,
                Main.METER,
                Main.METER
        );
        map_queen = new AdvSprite(
                g.atlas.createSprite("map_queen"),
                0f,
                0f,
                2f * Main.METER,
                2f * Main.METER
        );
        map_pollen = new ArrayList<>();
        ui_hp = new ArrayList<>();
        for (int i = 0; i < hp; i++) {
            ui_hp.add(new AdvSprite(
                    g.atlas.createSprite("heart"),
                    0f,
                    0f,
                    0.0375f * g.w,
                    0.0375f * g.w
            ));
        }
        queen_clouds = new ArrayList<>();
        queen_clouds.add(new AdvSprite(
                g.atlas.createSprite("cloud", 1),
                90f * Main.METER,
                70f * Main.METER,
                .2f * g.w,
                .2f * g.w
        ));
        queen_clouds.get(0).getColor().a = 0;
        queen_clouds.get(0).addAction(Actions.forever(Actions.sequence(
                Actions.alpha(1f, 1f),
                Actions.delay(4f),
                Actions.alpha(0f,1f),
                Actions.delay(12f)
                )));
        queen_clouds.add(new AdvSprite(
                g.atlas.createSprite("cloud", 2),
                90f * Main.METER,
                70f * Main.METER,
                .2f * g.w,
                .2f * g.w
        ));
        queen_clouds.get(1).getColor().a = 0;
        queen_clouds.get(1).addAction(Actions.sequence(
                Actions.delay(6f),
                Actions.forever(Actions.sequence(
                    Actions.alpha(1f, 1f),
                    Actions.delay(4f),
                    Actions.alpha(0f,1f),
                    Actions.delay(12f)
                ))));
        queen_clouds.add(new AdvSprite(
                g.atlas.createSprite("cloud", 3),
                90f * Main.METER,
                70f * Main.METER,
                .2f * g.w,
                .2f * g.w
        ));
        queen_clouds.get(2).getColor().a = 0;
        queen_clouds.get(2).addAction(Actions.sequence(
                Actions.delay(12f),
                Actions.forever(Actions.sequence(
                        Actions.alpha(1f, 1f),
                        Actions.delay(4f),
                        Actions.alpha(0f,1f),
                        Actions.delay(12f)
                ))));
        // endregion
        // region Flowers init
        flowers = new ArrayList<>();
        int n_flower = MathUtils.random(15, 25);
        float x, y;
        for (int i = 0; i < n_flower; ++i) {
            do {
                x = 900f * Main.METER * MathUtils.random();
                y = 900f * Main.METER * MathUtils.random();
            } while (x < 300f * Main.METER && y < 300f * Main.METER);
            float f_size = MathUtils.random() * 75f * Main.METER + 75f * Main.METER;
            flowers.add(new AdvSprite(
                    g.atlas.createSprite("flower", MathUtils.random(3) + 1),
                    x,
                    y,
                    f_size,
                    f_size
            ));
            flowers.get(flowers.size() - 1).rotateBy(MathUtils.random(359f));
        }
        // endregion
        // region Ants
        int n_ants = MathUtils.random(60, 90);
        for (int i = 0; i < n_ants; ++i) {
            spawnAnt();
        }
        // endregion
    }
    private void button_initialization() {}
    private void particle_initialization() {}
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
            if (x != 4) {
                hive.add(new AdvSprite(
                        g.atlas.createSprite("honeycomb_up"),
                        50f * Main.METER + x * 16f * Main.METER,
                        194f * Main.METER,
                        16f * Main.METER,
                        24f * Main.METER
                ));
            } else {
                hive.add(new AdvSprite(
                        g.atlas.createSprite("honeycomb_entrance"),
                        50f * Main.METER + x * 16f * Main.METER - 8f * Main.METER,
                        194f * Main.METER,
                        32f * Main.METER,
                        24f * Main.METER
                ));
            }
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
                    ).nor().scl(1500f),
                    true
            );
        }
        //
    }
    private void rotations() {
        //
        // region Player Rotation
        float x = Gdx.input.getX() - .5f * g.w;
        float y = Gdx.input.getY() - .5f * g.h;
        float angle = (float)(Math.atan2(x, y) + Math.PI);
        player.getBody().setTransform(player.getBody().getPosition(), angle);
        // endregion
        // region Queen Rotation
        x = player.getX() - queen.getX();
        y = player.getY() - queen.getY();
        angle = (float)(Math.atan2(x, y));
        queen.getBody().setTransform(queen.getBody().getPosition(), -angle);
        // endregion
        // region Guard Rotation
        x = player.getX() - q_guards[0].getX();
        y = player.getY() - q_guards[0].getY();
        angle = (float)(Math.atan2(x, y));
        q_guards[0].getBody().setTransform(q_guards[0].getBody().getPosition(), -angle);
        x = player.getX() - q_guards[1].getX();
        y = player.getY() - q_guards[1].getY();
        angle = (float)(Math.atan2(x, y));
        q_guards[1].getBody().setTransform(q_guards[1].getBody().getPosition(), -angle);
        // endregion
        //
    }

    private void spawnBug() {
        float x;
        float y;
        do {
            x = 1000f * MathUtils.random();
            y = 1000f * MathUtils.random();
        } while (x < 200f && y < 200f);

        Bug bug = new Bug(
                g,
                world,
                x,
                y,
                50f,
                2f + MathUtils.random(),
                MathUtils.random(1, 4)
        );
        bug.setStateTimer(2f + (3f * MathUtils.random()));

        bugs.add(bug);
        movingBodies.add(bug.getAdvBody());

        stage.addActor(bug);
        stage.addActor(bug.getAdvBody());
        for (AdvBodyPart part : bug.getAdvBody().getParts()) {
            stage.addActor(part);
            part.addAction(Actions.sequence(
                    Actions.alpha(0f),
                    Actions.sizeTo(0f, 0f),
                    Actions.parallel(
                            Actions.alpha(1f, 1f),
                            Actions.sizeTo(bug.getWidth(), bug.getHeight(), 1f)
                    )
            ));
        }
    }
    private void spawnAnt() {
        float x;
        float y;
        do {
            x = 750f * Main.METER * MathUtils.random();
            y = 750f * Main.METER * MathUtils.random();
        } while (x < 300f * Main.METER && y < 300f * Main.METER);

        Ant ant = new Ant(
                g,
                x,
                y,
                2f * Main.METER + MathUtils.random() * 2f * Main.METER
        );

        ants.add(ant);
    }
    private void spawnPollen() {
        int rand_flower = MathUtils.random(flowers.size() - 1);
        float x = flowers.get(rand_flower).getX() + .5f * flowers.get(rand_flower).getWidth();
        float y = flowers.get(rand_flower).getY() + .5f * flowers.get(rand_flower).getHeight();
        pollen.add(
                new AdvBody(
                        new Vector2(x / Main.METER, y / Main.METER),
                        BodyDef.BodyType.DynamicBody
                )
        );
        pollen.get(pollen.size() - 1).createBody(world);
        CircleShape pollenShape = new CircleShape();
        pollenShape.setRadius(2f);

        FixtureDef pollen_fd = new FixtureDef();
        pollen_fd.shape = pollenShape;
        pollen_fd.friction = .1f;
        pollen_fd.density = .1f;
        pollen_fd.restitution = .1f;

        AdvBodyPart pollenPart = new AdvBodyPart(
                g.atlas.createSprite("pollen"),
                -2f * Main.METER,
                -2f * Main.METER,
                4f * Main.METER,
                4f * Main.METER,
                pollenShape,
                pollen_fd
        );

        pollen.get(pollen.size() - 1).addPart(pollenPart);
        pollen.get(pollen.size() - 1).setSize(4f * Main.METER, 4f * Main.METER);
        pollen.get(pollen.size() - 1).getBody().applyForceToCenter(1f,1f, true);

        movingBodies.add(pollen.get(pollen.size() - 1));

        stage.addActor(pollen.get(pollen.size() - 1));
        for (AdvBodyPart part : pollen.get(pollen.size() - 1).getParts())
            stage.addActor(part);

        map_pollen.add(new AdvSprite(
                g.atlas.createSprite("map_pollen"),
                0f,
                0f,
                is_map_big ? .0025f * g.w + .5f * Main.METER : .5f * Main.METER,
                is_map_big ? .0025f * g.w + .5f * Main.METER : .5f * Main.METER
        ));
        map_pollen.get(map_pollen.size() - 1).addAction(Actions.sequence(
                Actions.alpha(0f),
                Actions.alpha(1f, 1f)
        ));
        stage.addActor(map_pollen.get(map_pollen.size() - 1));
    }
    private void spawnLadybug() {
        float x;
        float y;
        do {
            x = 1000f * MathUtils.random();
            y = 1000f * MathUtils.random();
        } while (x < 200f && y < 200f);

        Bug bug = new Bug(
                g,
                world,
                x,
                y,
                100f,
                2f + MathUtils.random(),
                10
        );
        bug.setStateTimer(2f + (3f * MathUtils.random()));

        ladybugs.add(bug);
        movingBodies.add(bug.getAdvBody());

        stage.addActor(bug);
        stage.addActor(bug.getAdvBody());
        for (AdvBodyPart part : bug.getAdvBody().getParts()) {
            stage.addActor(part);
            part.addAction(Actions.sequence(
                    Actions.alpha(0f),
                    Actions.sizeTo(0f, 0f),
                    Actions.parallel(
                            Actions.alpha(1f, 1f),
                            Actions.sizeTo(bug.getWidth(), bug.getHeight(), 1f)
                    )
            ));
        }
    }
    private void spawnWaspP() {
        float x;
        float y;
        do {
            x = 1000f * MathUtils.random();
            y = 1000f * MathUtils.random();
        } while (x < 300f && y < 300f);

        WaspP wasp = new WaspP(
                g,
                world,
                x,
                y,
                100f,
                5f + MathUtils.random()
        );
        wasp.setStateTimer(2f + (3f * MathUtils.random()));

        waspsP.add(wasp);
        movingBodies.add(wasp.getAdvBody());

        stage.addActor(wasp);
        stage.addActor(wasp.getAdvBody());
        for (AdvBodyPart part : wasp.getAdvBody().getParts()) {
            stage.addActor(part);
            part.addAction(Actions.sequence(
                    Actions.alpha(0f),
                    Actions.sizeTo(0f, 0f),
                    Actions.parallel(
                            Actions.alpha(1f, 1f),
                            Actions.sizeTo(wasp.getWidth(), wasp.getHeight(), 1f)
                    )
            ));
        }
    }
    private void spawnWaspN() {
        float x;
        float y;
        do {
            x = 1000f * MathUtils.random();
            y = 1000f * MathUtils.random();
        } while (x < 300f && y < 300f);

        WaspN wasp = new WaspN(
                g,
                world,
                x,
                y,
                100f,
                5f + MathUtils.random()
        );
        wasp.setStateTimer(2f + (3f * MathUtils.random()));

        waspsN.add(wasp);
        movingBodies.add(wasp.getAdvBody());

        stage.addActor(wasp);
        stage.addActor(wasp.getAdvBody());
        for (AdvBodyPart part : wasp.getAdvBody().getParts()) {
            stage.addActor(part);
            part.addAction(Actions.sequence(
                    Actions.alpha(0f),
                    Actions.sizeTo(0f, 0f),
                    Actions.parallel(
                            Actions.alpha(1f, 1f),
                            Actions.sizeTo(wasp.getWidth(), wasp.getHeight(), 1f)
                    )
            ));
        }
    }
    private void spawnWaspF() {
        float x;
        float y;
        do {
            x = 1000f * MathUtils.random();
            y = 1000f * MathUtils.random();
        } while (x < 300f && y < 300f);

        WaspF wasp = new WaspF(
                g,
                world,
                x,
                y,
                100f,
                7f + MathUtils.random()
        );
        wasp.setStateTimer(2f + (3f * MathUtils.random()));

        waspsF.add(wasp);
        movingBodies.add(wasp.getAdvBody());

        stage.addActor(wasp);
        stage.addActor(wasp.getAdvBody());
        for (AdvBodyPart part : wasp.getAdvBody().getParts()) {
            stage.addActor(part);
            part.addAction(Actions.sequence(
                    Actions.alpha(0f),
                    Actions.sizeTo(0f, 0f),
                    Actions.parallel(
                            Actions.alpha(1f, 1f),
                            Actions.sizeTo(wasp.getWidth(), wasp.getHeight(), 1f)
                    )
            ));
        }
    }
    private void spawnBullet(WaspF wasp) {
        float c_x = wasp.getAdvBody().getX() + .5f * wasp.getAdvBody().getWidth();
        float c_y = wasp.getAdvBody().getY() + .5f * wasp.getAdvBody().getHeight();
        float radius = .5f * wasp.getAdvBody().getHeight();

        Vector2 direction = new Vector2(
                Field.player.getX() - c_x,
                Field.player.getY() - c_y
        );
        Vector2 position = new Vector2(c_x, c_y).add(
                direction.nor().scl(1.1f * radius)
        );

        Bullet bullet = new Bullet(
                g,
                world,
                position.x / Main.METER,
                position.y / Main.METER,
                3f,
                2f
        );

        bullets.add(bullet);
        movingBodies.add(bullet.getAdvBody());

        direction.nor().scl(30000f);
        bullet.getAdvBody().getBody().applyForceToCenter(
                direction,
                true
        );
        float angle = (float)(Math.atan2(direction.x, direction.y));
        bullet.getAdvBody().getBody().setTransform(bullet.getAdvBody().getBody().getPosition(), -angle);

        stage.addActor(bullet);
        stage.addActor(bullet.getAdvBody());
        for (AdvBodyPart part : bullet.getAdvBody().getParts()) {
            stage.addActor(part);
        }
    }

    private void stage_addition() {
        for (AdvSprite s : ground)
            stage.addActor(s);
        for (AdvSprite s : hive)
            stage.addActor(s);
        for (Ant ant : ants)
            stage.addActor(ant);
        for (AdvSprite s : flowers)
            stage.addActor(s);

        stage.addActor(player);
        for (AdvBodyPart part : player.getParts())
            stage.addActor(part);

        stage.addActor(q_guards[0]);
        stage.addActor(q_guards[1]);
        for (AdvBodyPart part : q_guards[0].getParts())
            stage.addActor(part);
        for (AdvBodyPart part : q_guards[1].getParts())
            stage.addActor(part);

        stage.addActor(queen);
        for (AdvBodyPart part : queen.getParts())
            stage.addActor(part);
        for (AdvSprite s : queen_clouds)
            stage.addActor(s);

        stage.addActor(map);
        stage.addActor(map_queen);
        stage.addActor(map_player);

        for (AdvSprite s : ui_hp)
            stage.addActor(s);

        if (g.is_dev_mode) {
            stage.addActor(test_text);
        }
        stage.addActor(score_text);
        stage.addActor(goal_text);
        stage.addActor(camera);
    }

    private void friction() {
        for (AdvBody body : movingBodies) {
            if (body.getBody().getLinearVelocity().len() > .01f) {
                body.getBody().setLinearVelocity(
                         new Vector2(
                                 body.getBody().getLinearVelocity().x * .975f,
                                 body.getBody().getLinearVelocity().y * .975f
                         )
                );
            }
            if (Math.abs(body.getBody().getAngularVelocity()) > .01f) {
                body.getBody().setAngularVelocity(
                        body.getBody().getAngularVelocity() * .975f
                );
            }
        }
    }
    private void hitPlayer() {
        if (hp > 0) {
            ui_hp.get(hp - 1).addAction(Actions.sequence(
                    Actions.sizeBy(0f, -.0125f * g.w, .1f),
                    Actions.sizeBy(0f, .0125f * g.w, .1f)
            ));
            if (g.is_sound) {
                g.sounds.hurt.play(g.sound_volume);
            }
            hp--;
        }
    }
    private void checkToRemove(ArrayList<? extends Entity> list) {
        for(int i = list.size() - 1; i >= 0; --i) {
            // region Deletion of the bodies
            // It is the safe way to delete physical bodies
            Body body = list.get(i).getAdvBody().getBody();
            if (body != null) {
                if (!list.get(i).isAlive()) {
                    final Array<JointEdge> joint_list = body.getJointList();
                    while (joint_list.size > 0) {
                        world.destroyJoint(joint_list.get(0).joint);
                    }
                    body.setUserData(null);
                    world.destroyBody(body);
                    list.get(i).getAdvBody().remove();
                    movingBodies.remove(list.get(i).getAdvBody());
                    list.get(i).remove();
                    list.remove(i);
                }
            }
            // endregion
        }

    }
    private void collectPollen() {
        for (int i = pollen.size() - 1; i >= 0; --i) {
            Body body = pollen.get(i).getBody();
            if (body != null) {
                if (pollen.get(i).getName() != null && pollen.get(i).getName().equals("for_delete")) {
                    if (g.is_sound) {
                        g.sounds.coin.play(g.sound_volume);
                    }
                    score += 10;
                    // deletion
                    final Array<JointEdge> list = body.getJointList();
                    while (list.size > 0) {
                        world.destroyJoint(list.get(0).joint);
                    }
                    body.setUserData(null);
                    world.destroyBody(body);
                    pollen.get(i).getParts().get(pollen.get(i).getParts().size() - 1).addAction(Actions.sequence(
                            Actions.alpha(0f, 1f),
                            Actions.removeActor()
                    ));
                    pollen.get(i).remove();
                    map_pollen.get(i).remove();
                    map_pollen.remove(i);
                    movingBodies.remove(pollen.get(i));
                    pollen.get(i).remove();
                    pollen.remove(i);
                }
            }
        }
    }
    private void collectLadybugs() {
        for (Bug ladybug : ladybugs) {
            if (ladybug.getName() != null && ladybug.getName().equals("consumed")) {
                ladybug.setName(null);
                ladybug.kill();
                if (g.is_sound) {
                    g.sounds.healing.play(g.sound_volume);
                }
                if (hp < 3) {
                    hp++;
                }
            }
        }
    }
    private void fatShooting() {
        for (WaspF wasp : waspsF) {
            if (wasp.getName() != null && wasp.getName().equals("shooting")) {
                wasp.setName(null);
                spawnBullet(wasp);
                if (g.is_sound) {
                    g.sounds.shoot.play(g.sound_volume);
                }
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
