package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kimdokja.orv.TheaterDungeon;

public class FadeScreen implements Screen {
    private TheaterDungeon game;
    private Screen nextScreen;
    private float alpha = 0; // Transparency
    private boolean fadingOut = true; // Whether to fade out or fade in
    private ShapeRenderer shapeRenderer;

    public FadeScreen(TheaterDungeon game, Screen nextScreen) {
        this.game = game;
        this.nextScreen = nextScreen;
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
        // Optionally you can set the next screen here or in the constructor
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update alpha for fading
        if (fadingOut) {
            alpha += delta; // Fade out
            if (alpha >= 1) {
                alpha = 1;
                fadingOut = false; // Start fading in
            }
        } else {
            alpha -= delta; // Fade in
            if (alpha <= 0) {
                alpha = 0;
                if (nextScreen == null) {
                    Gdx.app.exit(); // Exit the application
                } else {
                    game.setScreen(nextScreen); // Set the next screen
                }
                return; // End render early
            }
        }

        // Draw a black rectangle with varying alpha
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, alpha); // Set the color to black with current alpha
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
    }


    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
