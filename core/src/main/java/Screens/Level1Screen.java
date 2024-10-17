package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kimdokja.orv.TheaterDungeon;

import Entity.Player;
import Scenes.HUD;

public class Level1Screen implements Screen {
    private TheaterDungeon game;
    private OrthographicCamera gamecam;
    private Viewport gameport;
    private HUD hud;
    private SpriteBatch batch;
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private World world;
    private Box2DDebugRenderer b2dr;
    private Player player;
 // Add these variables to your Level1Screen class
    private int levelWidth;
    private int levelHeight;

    // Inside the constructor after loading the map, calculate the level dimensions
    

    public Level1Screen(TheaterDungeon game) {
        this.game = game;
        gamecam = new OrthographicCamera();
        
        gameport = new FitViewport(TheaterDungeon.V_WIDTH / TheaterDungeon.PPM, TheaterDungeon.V_HEIGHT / TheaterDungeon.PPM, gamecam);
        hud = new HUD(game.batch);
        batch = new SpriteBatch();
        maploader = new TmxMapLoader();
        map = maploader.load("maps/map1/level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / TheaterDungeon.PPM);
        gamecam.position.set(gameport.getWorldWidth() / 2, gameport.getWorldHeight() / 2, 0);
        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();
        levelWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        levelHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);
        
        // Initialize the player
        player = new Player(world);

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        // Creating the ground bodies and fixtures
        for (MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / TheaterDungeon.PPM, (rect.getY() + rect.getHeight() / 2) / TheaterDungeon.PPM);
            body = world.createBody(bdef);
            shape.setAsBox(rect.getWidth() / 2 / TheaterDungeon.PPM, rect.getHeight() / 2 / TheaterDungeon.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        // Creating wall
        for (MapObject object : map.getLayers().get(8).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / TheaterDungeon.PPM, (rect.getY() + rect.getHeight() / 2) / TheaterDungeon.PPM);
            body = world.createBody(bdef);
            shape.setAsBox(rect.getWidth() / 2 / TheaterDungeon.PPM, rect.getHeight() / 2 / TheaterDungeon.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
            body.setUserData(8);
        }
    }

    public void update(float dt) {
        player.update(dt); // Delegate input handling to the player

        // Update Box2D world
        world.step(1 / 60f, 6, 2);
        
        gamecam.position.x = player.b2body.getPosition().x;
        gamecam.position.y = player.b2body.getPosition().y;

        gamecam.position.x = Math.max(gameport.getWorldWidth() / 2, Math.min(player.b2body.getPosition().x, (levelWidth - gameport.getWorldWidth() / 2)));
        gamecam.position.y = Math.max(gameport.getWorldHeight() / 2, Math.min(player.b2body.getPosition().y, (levelHeight - gameport.getWorldHeight() / 2)));
        
        gamecam.update();
        renderer.setView(gamecam);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameport.apply();
        batch.setProjectionMatrix(gamecam.combined);
        // Render the map
        renderer.render();
        b2dr.render(world, gamecam.combined);
        
        batch.begin();
        player.render(batch);
        hud.stage.draw(); // Assuming HUD uses batch internally.
        batch.end();
        // Render the HUD
        
        batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameport.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
    }
}
