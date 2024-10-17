	package com.kimdokja.orv;
	
	import com.badlogic.gdx.ApplicationAdapter;
	import com.badlogic.gdx.Game;
	import com.badlogic.gdx.Gdx;
	import com.badlogic.gdx.graphics.GL20;
	import com.badlogic.gdx.graphics.Texture;
	import com.badlogic.gdx.graphics.g2d.SpriteBatch;
	import com.badlogic.gdx.utils.ScreenUtils;
	
	import Screens.Level1Screen;
	import Screens.MenuScreen;
	import Screens.PlayScreen;
	
	/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
	public class TheaterDungeon extends Game {
		public static final int V_WIDTH = 1920;
		public static final int V_HEIGHT = 1080;
	    public SpriteBatch batch;
	    public static final float PPM = 50;
	    private boolean isCutsceneActive = false;
	    public static final short PLAYER_CATEGORY = 0x0001;
	    public static final short GROUND_CATEGORY = 0x0002; // Define ground category
	    public static final short WALL_CATEGORY = 0x0004; 
	    
	    
	
	    @Override
	    public void create() {
	        Gdx.graphics.setWindowedMode(V_WIDTH, V_HEIGHT); // Set the windowed mode to your desired resolution
	        batch = new SpriteBatch();
	        //setScreen(new MenuScreen(this)); // Start with the MenuScreen
	        setScreen(new Level1Screen(this));
	    }
	    public boolean isCutsceneActive() {
	        return isCutsceneActive;
	    }
	
	    public void setCutsceneActive(boolean active) {
	        this.isCutsceneActive = active;
	    }
	
	
	    @Override
	    public void render() {
	        super.render();
	    }
	
	    @Override
	    public void dispose() {
	        batch.dispose();
	        
	    }
	}
