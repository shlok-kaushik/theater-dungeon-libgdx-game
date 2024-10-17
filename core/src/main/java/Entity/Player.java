package Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.kimdokja.orv.TheaterDungeon;

public class Player extends Sprite {
    private World world;
    public Body b2body;

    private boolean isDashing = false;
  

    // Adjust these parameters as necessary
    private float jumpForce = 5f; // Jump force
    private float dashSpeed = 5f; // Speed during a dash
    private float dashDuration = 0.3f; // Duration of the dash
   
    private float fallMultiplier = 2.5f; // Fall multiplier
    private float lowJumpMultiplier = 2f; // Low jump multiplier

    private float lastDashTime = 0; // Cooldown timer for dashing
    private float dashCooldown = 1f; // Cooldown period for dashes
    
    private TextureAtlas atlas;
    private TextureAtlas jump;
    private TextureAtlas dash;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> dashAnimation;
    private float stateTime;
    private boolean isWalking;
    private boolean isJumping = false;
    

    private float scale = 0.1f;
    
    
    public Player(World world) {
        this.world = world;
        definePlayer();
        atlas = new TextureAtlas(Gdx.files.internal("Spritesheets/main_character/idle_walk.atlas"));
        jump = new TextureAtlas(Gdx.files.internal("Spritesheets/main_character/jump.atlas"));
        //dash = new TextureAtlas(Gdx.files.internal("Spritesheets/main_character/dash.atlas"));
        // Load idle and walk animations (assuming you have regions named "idle" and "walk" in your atlas)
        idleAnimation = new Animation<TextureRegion>(0.1f, atlas.findRegion("idle1"),atlas.findRegion("idle1"),atlas.findRegion("idle2"),atlas.findRegion("idle3"),atlas.findRegion("idle4"));
        walkAnimation = new Animation<TextureRegion>(0.1f, atlas.findRegion("walk1"),atlas.findRegion("walk2"),atlas.findRegion("walk3"),atlas.findRegion("walk4"));
        jumpAnimation = new Animation<TextureRegion>(0.1f, jump.findRegion("jump1"),jump.findRegion("jump2"),jump.findRegion("jump3"),jump.findRegion("jump4"));
        //dashAnimation = new Animation<TextureRegion>(0.1f, dash.findRegion("dash1"),dash.findRegion("dash2"),dash.findRegion("dash3"),dash.findRegion("dash4"),dash.findRegion("dash5"),dash.findRegion("dash6"));
        // Initialize stateTime and isWalking
        
        stateTime = 0f;
        isWalking = false;
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
    

    private void definePlayer() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(100 / TheaterDungeon.PPM, 100 / TheaterDungeon.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Rectangle rect = new Rectangle();
        rect.setSize(5);
        
        shape.setAsBox(rect.getWidth() / 2 / TheaterDungeon.PPM, rect.getHeight() / 2 / TheaterDungeon.PPM);
        fdef.shape = shape;
        b2body.createFixture(fdef);
    }

    public void update(float deltaTime) {
        handleInput(deltaTime);
        stateTime += deltaTime;
        setPosition(b2body.getPosition().x -getWidth()/2, b2body.getPosition().y -getHeight()/2);
        isWalking = Math.abs(b2body.getLinearVelocity().x) > 0;
        if (b2body.getLinearVelocity().y > 0) {
            isJumping = true; // Set jumping state
        } else if (b2body.getLinearVelocity().y < 0) {
            isJumping = false; // Reset when falling
        }
        // Better jumping mechanics
        if (b2body.getLinearVelocity().y < 0) {
            b2body.applyLinearImpulse(new Vector2(0, -fallMultiplier * 9.8f * deltaTime), b2body.getWorldCenter(), true);
        } else if (b2body.getLinearVelocity().y > 0 && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            b2body.applyLinearImpulse(new Vector2(0, -lowJumpMultiplier * 9.8f * deltaTime), b2body.getWorldCenter(), true);
        }

        // Manage dashing
        if (isDashing) {
            dashDuration -= deltaTime;
            if (dashDuration <= 0) {
                isDashing = false; // End the dash
                dashDuration = 0.3f; // Reset dash duration
            }
        }

        // Update last dash time
        lastDashTime += deltaTime;
    }

    private void handleInput(float deltaTime) {
        // Check for jumping off the wall or sticking to the wall
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (isGrounded()) {
                jump(); // Jump if on the ground
            }
        }

        
            if (Gdx.input.isKeyPressed(Input.Keys.D) && (b2body.getLinearVelocity().x <= 2)) {
                b2body.applyForce(new Vector2(5, 0), b2body.getWorldCenter(), true);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.A) && (b2body.getLinearVelocity().x >= -2)) {
                b2body.applyForce(new Vector2(-5, 0), b2body.getWorldCenter(), true);
            }
        

        // Dash Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && canDash()) {
            dash();
        }
    }

    private boolean isGrounded() {
        Vector2 start = new Vector2(b2body.getPosition().x, b2body.getPosition().y);
        Vector2 end = new Vector2(start.x, start.y - 0.2f); // Increased length

        GroundRaycastCallback callback = new GroundRaycastCallback();
        world.rayCast(callback, start, end);

        
        
        return callback.isGrounded; 
    }

   
    





    private void jump() {
        if (isGrounded()) {
            b2body.applyLinearImpulse(new Vector2(0, jumpForce), b2body.getWorldCenter(), true);
            
        }
    }

    private void dash() {
        if (canDash()) {
            Vector2 dashDirection = new Vector2(0, 0);

            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                dashDirection.x = dashSpeed;
            } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                dashDirection.x = -dashSpeed;
            }

            b2body.setLinearVelocity(dashDirection);
            isDashing = true; // Set dashing state
            lastDashTime = 0; // Reset last dash time to track cooldown
        }
    }

    private boolean canDash() {
        return lastDashTime >= dashCooldown; // Allow dashing if cooldown is complete
    }
 

    public void render(SpriteBatch batch) {
    	
        
        // Get the current frame from the animation (idle or walk)
        TextureRegion currentFrame;
        if (isWalking) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);  // Loop walk animation
        } else if (isJumping){currentFrame = jumpAnimation.getKeyFrame(stateTime, false);
        } else{
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);  // Loop idle animation
        }

        // Draw the current frame at the player's position
        float x = b2body.getPosition().x * TheaterDungeon.PPM  ;
        float y = b2body.getPosition().y * TheaterDungeon.PPM ;
        
        // Make sure to center the frame at the body's position
        batch.draw(currentFrame, 
                x - (currentFrame.getRegionWidth() * scale) / 2, 
                y - (currentFrame.getRegionHeight() * scale) / 2,
                currentFrame.getRegionWidth() * scale, 
                currentFrame.getRegionHeight() * scale);
        setOrigin(currentFrame.getRegionWidth() * scale / 2, currentFrame.getRegionHeight() * scale / 2);
    }
    

    public void dispose() {
    	atlas.dispose();
    }
}
