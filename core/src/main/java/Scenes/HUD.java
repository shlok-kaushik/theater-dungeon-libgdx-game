package Scenes;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.Gdx;
import com.kimdokja.orv.TheaterDungeon;

public class HUD implements Disposable {
    public Stage stage;
    private Viewport viewport;

    private float hp;     // Current health
    private float stamina; // Current stamina

    // Textures for the HP and Stamina bars
    private Texture hpBar;
    private Texture staminaBar;

    private Texture emptyBar; // Empty bar texture to display the background

    private TheaterDungeon game;

    public HUD(SpriteBatch sb) {
        // Create a camera and viewport for HUD (using pixels)
        OrthographicCamera hudCam = new OrthographicCamera();
        viewport = new FitViewport(TheaterDungeon.V_WIDTH, TheaterDungeon.V_HEIGHT, hudCam);

        // Set up the stage for the HUD
        stage = new Stage(viewport, sb);

        // Initialize HP and Stamina (can be max values to start)
        hp = 100;     // Assuming max health is 100
        stamina = 50; // Assuming max stamina is 50

        // Load textures for bars
        hpBar = new Texture("hp_bar.png");         // Replace with the actual path of your HP bar image
        staminaBar = new Texture("stamina_bar.png"); // Replace with actual stamina bar image
        emptyBar = new Texture("empty_bar.png");     // Background of the bars

        // Optional: Set up fonts for any text labels (if desired)
        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);
        GlyphLayout layout = new GlyphLayout(font, "HP");  // Example label
    }

    public void update(float dt, float newHP, float newStamina) {
        // Update the current values for health and stamina (can be tied to game logic)
        hp = newHP;
        stamina = newStamina;
    }

    public void render(SpriteBatch sb) {
        // Render the HUD components (draw on top of the game world)

        sb.setProjectionMatrix(stage.getCamera().combined);
        sb.begin();

        // Draw the empty background bar first
        sb.draw(emptyBar, 10, 180, 100, 10);  // Position and size for the empty HP bar background
        sb.draw(emptyBar, 10, 160, 100, 10);  // Position and size for the empty Stamina bar background

        // Draw the HP and Stamina bars (scaled based on current values)
        sb.draw(hpBar, 10, 180, hp, 10);  // HP bar scales horizontally based on current health
        sb.draw(staminaBar, 10, 160, stamina, 10); // Stamina bar scales horizontally based on current stamina

        sb.end();
    }

    @Override
    public void dispose() {
        // Dispose textures and stage to avoid memory leaks
        hpBar.dispose();
        staminaBar.dispose();
        emptyBar.dispose();
        stage.dispose();
    }
}
