package Screens;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kimdokja.orv.TheaterDungeon;



public class Level1Screen implements Screen, ContactListener {
    private TheaterDungeon game;
    private OrthographicCamera gamecam;
    private Viewport gameport;
    
    private SpriteBatch batch;
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private World world;
    private Box2DDebugRenderer b2dr;

    private Body playerBody; // Player's Box2D body
    private TextureAtlas atlas,jump,dash; 
    
    private Animation<TextureRegion> idleAnimation, walkAnimation, jumpAnimation , dashAnimation;
   
    private float stateTime;
    private boolean isWalking;
    private boolean isJumping;
    private boolean isDashing = false;
    
    // Player parameters
    private float jumpForce = 7f;
    private float dashSpeed = 10f;
    private float dashDuration = 0.3f;
    private float fallMultiplier = 2.5f;
    private float lowJumpMultiplier = 2f;
    private float lastDashTime = 0;
    private float dashCooldown = 0.5f;
    private float stepInterval = 0.3f; // Interval between footsteps
    private float timeSinceLastStep = 0;
    
    private float scale = 0.1f;
    private float zoom = 0.3f;
    private float gravityStrength = 8f;
    private boolean facingLeft;
    private boolean isGravityFlipped = false;
    private boolean resetPlayerPositionFlag = false;
    private boolean showBlackScreen = false; 
    private boolean wasInAirLastFrame = false;
    private float blackScreenTime = 0;      
    private float blackScreenDuration = 0.4f;   
    private Sound bg;
    private Sound dashh;
    private Sound jumpp;
    private Sound death;
    private Sound revive;
    private BitmapFont font;
    private Sound[] footstepSounds;
    private Random random;
    public Level1Screen(TheaterDungeon game) {
        this.game = game;
        gamecam = new OrthographicCamera();
        gameport = new FitViewport(TheaterDungeon.V_WIDTH / TheaterDungeon.PPM, TheaterDungeon.V_HEIGHT / TheaterDungeon.PPM, gamecam);
        gamecam.zoom = zoom;
        
        bg = Gdx.audio.newSound(Gdx.files.internal("gamebg.ogg"));
        dashh = Gdx.audio.newSound(Gdx.files.internal("sounds/dash.wav"));
        jumpp = Gdx.audio.newSound(Gdx.files.internal("sounds/jump.wav"));
        revive = Gdx.audio.newSound(Gdx.files.internal("sounds/revive.wav"));
        death = Gdx.audio.newSound(Gdx.files.internal("sounds/predeath.wav"));
        font = new BitmapFont(Gdx.files.internal("nier.fnt"));
        
        long soundid = bg.play();
        bg.setVolume(soundid, 0.2f);
        bg.loop();
        batch = new SpriteBatch();
        maploader = new TmxMapLoader();
        map = maploader.load("maps/map1/level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / TheaterDungeon.PPM);
        gamecam.position.set(gameport.getWorldWidth() / 2, gameport.getWorldHeight() / 2, 0);
        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();
        //gravityStrength = world.getGravity().y;
        
        initializePlayer();

        // Create ground and wall bodies
        createGroundAndWalls();
        world.setContactListener(this);
        footstepSounds = new Sound[3];
        for (int i = 0; i < 3; i++) {
            footstepSounds[i] = Gdx.audio.newSound(Gdx.files.internal("sounds/foot_00_dirt_0" + (i + 1) + ".wav"));
        }
        random = new Random();
        
        
        
        
    }
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        // Check if one of the fixtures is the player and the other is the reset surface
        if (isPlayer(fixtureA) && isResetSurface(fixtureB)) {
        	death.play();
            resetPlayerPositionFlag = true;// Set flag to reset position
        } else if (isPlayer(fixtureB) && isResetSurface(fixtureA)) {
            resetPlayerPositionFlag = true; // Set flag to reset position
        }
        if (isPlayer(fixtureA) && isExitSurface(fixtureB)) {
            // Change to the new screen (e.g., the next level)
        	game.setScreen(new FadeScreen(game, new CutsceneScreen2(game))); 
        	
        } else if (isPlayer(fixtureB) && isExitSurface(fixtureA)) {
            // Change to the new screen (e.g., the next level)
        	game.setScreen(new FadeScreen(game, new CutsceneScreen2(game)));
        	
        }
    }

    @Override
    public void endContact(Contact contact) {
        // triggered when two fixtures stop colliding
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // triggered before the collision is resolved 
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // triggered after the collision is resolved
    }

    // Helper methods to identify player and reset surface
    private boolean isPlayer(Fixture fixture) {
        return fixture.getBody() == playerBody; // Compare with the player body
    }

    private boolean isResetSurface(Fixture fixture) {
        // You can assign user data to the reset surface bodies to identify them
        return fixture.getBody().getUserData() != null && fixture.getBody().getUserData().equals("resetSurface");
    }

    // Method to reset the player's position
    private void resetPlayerPosition() {
    	revive.play();
        playerBody.setTransform(100 / TheaterDungeon.PPM, 300 / TheaterDungeon.PPM, 0); 
        playerBody.setLinearVelocity(0, 0); // Reset velocity
    }
    private boolean isExitSurface(Fixture fixture) {
        return fixture.getBody().getUserData() != null && fixture.getBody().getUserData().equals("exit");
    }

    

    private void initializePlayer() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(100 / TheaterDungeon.PPM, 300 / TheaterDungeon.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        playerBody = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(30 / 2 / TheaterDungeon.PPM, 40 / 2 / TheaterDungeon.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        playerBody.createFixture(fdef);

        // Load animations
        atlas = new TextureAtlas(Gdx.files.internal("Spritesheets/main_character/idle_walk.atlas"));
        jump = new TextureAtlas(Gdx.files.internal("Spritesheets/main_character/jump.atlas"));
        dash = new TextureAtlas(Gdx.files.internal("Spritesheets/main_character/dash.atlas"));
        jumpAnimation = new Animation<>(0.1f, jump.findRegion("jump1"), jump.findRegion("jump2"), jump.findRegion("jump3"), jump.findRegion("jump4"));
        idleAnimation = new Animation<>(0.1f, atlas.findRegion("idle1"), atlas.findRegion("idle2"), atlas.findRegion("idle3"), atlas.findRegion("idle4"));
        walkAnimation = new Animation<>(0.1f, atlas.findRegion("walk1"), atlas.findRegion("walk2"), atlas.findRegion("walk3"), atlas.findRegion("walk4"));
        dashAnimation = new Animation<>(0.1f,dash.findRegion("Kim dashnew2"),dash.findRegion("Kim dashnew3"),dash.findRegion("Kim dashnew4"));
        stateTime = 0f;
        isWalking = false;
        isJumping = false;
    }

    private void createGroundAndWalls() {
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        // Create ground bodies and fixtures
        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / TheaterDungeon.PPM, (rect.getY() + rect.getHeight() / 2) / TheaterDungeon.PPM);
            body = world.createBody(bdef);
            shape.setAsBox(rect.getWidth() / 2 / TheaterDungeon.PPM, rect.getHeight() / 2 / TheaterDungeon.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }
        // reset surface 
        for (MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle reset = ((RectangleMapObject) object).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((reset.getX() + reset.getWidth() / 2) / TheaterDungeon.PPM, (reset.getY() + reset.getHeight() / 2) / TheaterDungeon.PPM);
            Body body2 = world.createBody(bdef);
            shape.setAsBox(reset.getWidth() / 2 / TheaterDungeon.PPM, reset.getHeight() / 2 / TheaterDungeon.PPM);
            fdef.shape = shape;
            body2.createFixture(fdef);
            body2.setUserData("resetSurface"); // Assign user data
        }
        for (MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle reset = ((RectangleMapObject) object).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((reset.getX() + reset.getWidth() / 2) / TheaterDungeon.PPM, (reset.getY() + reset.getHeight() / 2) / TheaterDungeon.PPM);
            Body body3 = world.createBody(bdef);
            shape.setAsBox(reset.getWidth() / 2 / TheaterDungeon.PPM, reset.getHeight() / 2 / TheaterDungeon.PPM);
            fdef.shape = shape;
            body3.createFixture(fdef);
            body3.setUserData("exit"); // Assign user data
        }
        
        
    }
   
    private void playFootstepSound() {
        // Randomly select one of the 7 footstep sounds
        int soundIndex = random.nextInt(footstepSounds.length);
        footstepSounds[soundIndex].play();
    }    

    public void update(float dt) {
        handleInput(dt);
        world.step(1 / 60f, 6, 2);
        if (resetPlayerPositionFlag && !showBlackScreen) {
            showBlackScreen = true; 
            						// Start the black screen
            blackScreenTime = 0;               // Reset the timer
            resetPlayerPositionFlag = false;   
            // Clear the reset flag
        }
        System.out.println(world.getGravity());
        // If the black screen is showing, update the timer
        if (showBlackScreen) {
            blackScreenTime += dt;
            if (blackScreenTime >= blackScreenDuration) {
                resetPlayerPosition();          // Reset the player's position after the black screen
                showBlackScreen = false;  
                if(isGravityFlipped) {flipGravity();}
                // Stop showing the black screen
            }
        }
        if (isGrounded()) {
        	Vector2 velocity = playerBody.getLinearVelocity();
        	playerBody.setLinearVelocity(velocity.x,0);
        }
        if (isGrounded() && wasInAirLastFrame) {
            playerBody.setLinearVelocity(0, playerBody.getLinearVelocity().y); // Stop horizontal velocity on landing
            wasInAirLastFrame = false;
        } else if (!isGrounded()) {
            wasInAirLastFrame = true;
        }
        // Update player position
        stateTime += dt;
        isWalking = Math.abs(playerBody.getLinearVelocity().x) > 0;
        if (isWalking) {
            timeSinceLastStep += dt;
            if (timeSinceLastStep >= stepInterval) {
                playFootstepSound();
                timeSinceLastStep = 0; // Reset the timer for the next step
            }
        } else {
            // Reset the step timer when not moving
            timeSinceLastStep = 0;
        }
        
    
        if (playerBody.getLinearVelocity().y > 0) {
            isJumping = true; // Set jumping state
        } else if (playerBody.getLinearVelocity().y < 0) {
            isJumping = false; // Reset when falling
        }
        if (isGravityFlipped) {
            // Apply an upward force (inverted gravity)
            playerBody.applyForceToCenter(new Vector2(0, gravityStrength), true);
        } else {
            // Apply normal downward gravity
            playerBody.applyForceToCenter(new Vector2(0, -3), true);
        }
        
        // Better jumping mechanics
        if (isGravityFlipped) {
            if (playerBody.getLinearVelocity().y < 0) {
                // Apply upward fall multiplier when falling
                playerBody.applyLinearImpulse(new Vector2(0, fallMultiplier * 9.8f * dt), playerBody.getWorldCenter(), true);
            } else if (playerBody.getLinearVelocity().y > 0 && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                // Apply low jump multiplier if jump button is released
                playerBody.applyLinearImpulse(new Vector2(0, lowJumpMultiplier * 9.8f * dt), playerBody.getWorldCenter(), true);
            }
        } else {
            if (playerBody.getLinearVelocity().y < 0) {
                // Apply downward fall multiplier when falling
                playerBody.applyLinearImpulse(new Vector2(0, -1f * 9.8f * dt), playerBody.getWorldCenter(), true);
            } else if (playerBody.getLinearVelocity().y > 0 && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                // Apply low jump multiplier if jump button is released
                playerBody.applyLinearImpulse(new Vector2(0, -lowJumpMultiplier * 9.8f * dt), playerBody.getWorldCenter(), true);
            }
        }

        // Manage dashing
        if (isDashing) {
        	
            dashDuration -= dt;
            if (dashDuration <= 0) {
                isDashing = false; // End the dash
                dashDuration = 0.3f; // Reset dash duration
                playerBody.setLinearVelocity(0, playerBody.getLinearVelocity().y); 
            }
        }

        lastDashTime += dt;

        // Get the player's current position
        gamecam.position.set(playerBody.getPosition().x,playerBody.getPosition().y,0);

        
        gamecam.update();
        
        renderer.setView(gamecam);
       
    }

    private void handleInput(float deltaTime) {
        // jumppppp
    	 if (isDashing) {
    		 if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
    	    		flipGravity(); // Toggle gravity
    	        }
    	        return; // Ignore input if dashing
    	    }
    	if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (isGrounded())  {
                jump(); // Jump if on the ground
                
            }else {
            	System.out.println("not grounded");
            }
        }
    	if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
    		flipGravity(); // Toggle gravity
        }
    	
        // Better jumping mechanics
        if (playerBody.getLinearVelocity().y < 0) {
            playerBody.applyLinearImpulse(new Vector2(0, -fallMultiplier * 9.8f * deltaTime), playerBody.getWorldCenter(), true);
        } else if (playerBody.getLinearVelocity().y > 0 && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            playerBody.applyLinearImpulse(new Vector2(0, -lowJumpMultiplier * 9.8f * deltaTime), playerBody.getWorldCenter(), true);
        }
       
       

     
        if (Gdx.input.isKeyPressed(Input.Keys.D) && (playerBody.getLinearVelocity().x <= 5)) {
            playerBody.applyForce(new Vector2(5, 0), playerBody.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && (playerBody.getLinearVelocity().x >= -5)) {
            playerBody.applyForce(new Vector2(-5, 0), playerBody.getWorldCenter(), true);
        }

        // Dash
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && canDash()) {
            dash();
        }
    }
    
    private class GroundRaycastCallback implements RayCastCallback {
        public boolean isGrounded = false;

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            // Check if the fixture belongs to a static body (ground)
            if (fixture.getBody().getType() == BodyDef.BodyType.StaticBody) {
                isGrounded = true; // We hit the ground
                return fraction; // Stop raycasting on the first hit
            }
            return -1; // Continue raycasting
        }
    }
    private void flipGravity() {
        isGravityFlipped = !isGravityFlipped;  // Toggle gravity state

        // Invert the player's current vertical velocity
        Vector2 velocity = playerBody.getLinearVelocity();
        playerBody.setLinearVelocity(velocity.x, -velocity.y);

        // Apply a vertical impulse to break the player away from the ground
        if (isGravityFlipped) {
            playerBody.applyLinearImpulse(new Vector2(0, 10f), playerBody.getWorldCenter(), true); // Apply upward impulse
            world.setGravity(new Vector2(0, gravityStrength)); // Set gravity upwards
        } else {
            playerBody.applyLinearImpulse(new Vector2(0, -10f), playerBody.getWorldCenter(), true); // Apply downward impulse
            world.setGravity(new Vector2(0, -10)); // Set gravity downwards
        }
    }


private boolean isGrounded() {
    // This method checks for the ground collision using raycasting, but we need to adjust the direction of the ray based on gravity
    Vector2 start = new Vector2(playerBody.getPosition().x, playerBody.getPosition().y);
    Vector2 end = new Vector2(start.x, isGravityFlipped ? start.y + 0.5f : start.y - 0.5f);  // Adjust direction based on gravity

    GroundRaycastCallback callback = new GroundRaycastCallback();
    world.rayCast(callback, start, end);

    return callback.isGrounded;
}
    private void jump() {
    	jumpp.play();
    	float jumpForceApplied = isGravityFlipped ? -jumpForce  : jumpForce;
        playerBody.applyLinearImpulse(new Vector2(0, jumpForceApplied), playerBody.getWorldCenter(), true);
        isJumping = true; // Set jumping state
    }

    private boolean canDash() {
        return lastDashTime >= dashCooldown;
       
        
    }

    private void dash() {
    	dashh.play();
        Vector2 dashDirection = new Vector2(0, 0);

        // Check for input to determine dash direction
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dashDirection.y = 1; // Up
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dashDirection.y = -1; // Down
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dashDirection.x = -1; // Left
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dashDirection.x = 1; // Right
        }

        // Normalize direction vector to avoid faster diagonal dashes
        if (dashDirection.len() > 0) {
            dashDirection.nor();
        }

        // Apply dash speed in the calculated direction
        playerBody.setLinearVelocity(dashDirection.scl(dashSpeed));

        isDashing = true;
        lastDashTime = 0;  // Reset dash cooldown
    }

   
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(161 / 255f, 242 / 255f, 236 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        update(delta);
        
        renderer.render();
        
        batch.setProjectionMatrix(gamecam.combined);
        batch.begin();
        if(!showBlackScreen) {
        // Draw player
        TextureRegion currentFrame;
        if (isDashing) {
            currentFrame = dashAnimation.getKeyFrame(stateTime, true);
        } else if (!isGrounded()) {
            currentFrame = jumpAnimation.getKeyFrame(stateTime, false);
        } else if (isWalking) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }
        boolean isMovingLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean isMovingRight = Gdx.input.isKeyPressed(Input.Keys.D);
        

        // Handle flipping logic based on movement direction
        if (isMovingLeft) {
            if (!facingLeft) {
                facingLeft = true; // Update facing direction
            }
        } else if (isMovingRight) {
            if (facingLeft) {
                facingLeft = false; // Update facing direction
            }
        }
       
        if (currentFrame != null ) {
        	 if(isGravityFlipped) {
             	currentFrame.flip(false,true);
             }
        // Render the player sprite with flipping logic
        if (facingLeft) {
        	currentFrame.flip(true, false);
            batch.draw(currentFrame, 
                (playerBody.getPosition().x - 60 / 2 / TheaterDungeon.PPM), 
                (playerBody.getPosition().y - 60 / 2 / TheaterDungeon.PPM), 
                15 * scale, 
                15 * scale);
            currentFrame.flip(true, false); // Flip back for future frames
        } else {
            batch.draw(currentFrame, 
                (playerBody.getPosition().x - 60 / 2 / TheaterDungeon.PPM), 
                (playerBody.getPosition().y - 60 / 2 / TheaterDungeon.PPM), 
                15 * scale, 
                15 * scale);
        }
        if (isGravityFlipped) {
            currentFrame.flip(false, true); // Flip back to normal for next frame
        }
        }else {
        	Gdx.app.log("Animation Error", "Current frame is null!");}
        
    }
            
        
        if (showBlackScreen) {
            Gdx.gl.glClearColor(0, 0, 0, 1);  // Set black color
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // Clear the screen to black
        }
        batch.end();
        //b2dr.render(world, gamecam.combined);
    }

    @Override
    public void resize(int width, int height) {
        gameport.update(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
    	batch.dispose();
        map.dispose();
        renderer.dispose();
        world.dispose();
        atlas.dispose();
        jump.dispose();
        dash.dispose();
        bg.dispose();
        dashh.dispose();
        jumpp.dispose();
        death.dispose();
        revive.dispose();
        font.dispose();
        for (Sound sound : footstepSounds) {
            sound.dispose();
        }
    }
}
