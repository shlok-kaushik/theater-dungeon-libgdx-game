package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kimdokja.orv.TheaterDungeon;
@SuppressWarnings("unused")
public class MenuScreen implements Screen {

    
	private TheaterDungeon game;
    private Stage stage;
    private OrthographicCamera camera;
    private Texture background;
    private TextButton startButton;
    private TextButton exitButton;
    private SpriteBatch batch;
    private Sound Mtheme;
    private Sound exit;
    private Sound start;
    private BitmapFont font;
    private Label titleLabel;  // Declare a Label for the title

    public MenuScreen(TheaterDungeon game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, TheaterDungeon.V_WIDTH, TheaterDungeon.V_HEIGHT);

        // Stage for UI components
        stage = new Stage(new FitViewport(TheaterDungeon.V_WIDTH, TheaterDungeon.V_HEIGHT, camera), game.batch);
        Gdx.input.setInputProcessor(stage); // Process input via the stage

        // Load background
        batch = new SpriteBatch();
        background = new Texture(Gdx.files.internal("menu.jpg"));

        // Main menu audio sfx
        Mtheme = Gdx.audio.newSound(Gdx.files.internal("mainmenutheme.ogg"));
        exit = Gdx.audio.newSound(Gdx.files.internal("exit.ogg"));
        start = Gdx.audio.newSound(Gdx.files.internal("play.ogg"));
        Mtheme.play();
        Mtheme.loop();

        // Load the bitmap font
        font = new BitmapFont(Gdx.files.internal("nier.fnt"), Gdx.files.internal("nier.png"), false);
        font.getData().setScale(2.0f);

        // Create a TextButtonStyle for the buttons
        TextButtonStyle textButtonStyle = new TextButtonStyle();
        textButtonStyle.font = font;

        // Create buttons
        startButton = new TextButton("Start", textButtonStyle);
        exitButton = new TextButton("Exit", textButtonStyle);

        // Create a Label for the title with the same BitmapFont
        titleLabel = new Label("Theater;Dungeon", new Label.LabelStyle(font, null));
        titleLabel.setFontScale(2.5f); // Set a larger font size for the title

        // Position buttons and title
        positionButtons(); // Position buttons for the first time
        positionTitle();   // Position the title for the first time

        // Add button listeners
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.isCutsceneActive()) {
                    Mtheme.stop();
                    start.play();
                    game.setCutsceneActive(true); // Set cutscene active
                    game.setScreen(new FadeScreen(game, new CutsceneScreen(game)));
                    dispose();
                }
                
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Mtheme.stop();
                exit.play();

                // Use a fade effect before exiting
                FadeScreen fadeScreen = new FadeScreen(game, null); // Pass null since we are exiting
                game.setScreen(fadeScreen); // Set the fade screen as the current screen
            }
        });

        // Add UI components to the stage
        stage.addActor(titleLabel);  // Add the title label to the stage
        stage.addActor(startButton);
        stage.addActor(exitButton);
    }

    @Override
    public void show() {
        // Called when the screen becomes the current screen for the game.
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawBackground(); // Call to draw the background image

        batch.end();

        // Draw UI components
        stage.act(delta);
        stage.draw();
    }

    private void drawBackground() {
        float bgWidth = background.getWidth();
        float bgHeight = background.getHeight();
        float screenWidth = stage.getViewport().getWorldWidth();
        float screenHeight = stage.getViewport().getWorldHeight();

        // Calculate aspect ratios
        float bgAspect = bgWidth / bgHeight;
        float screenAspect = screenWidth / screenHeight;

        // Determine dimensions for background to maintain aspect ratio
        float drawWidth, drawHeight;
        if (bgAspect > screenAspect) {
            // Background is wider than screen
            drawWidth = screenWidth; // Match screen width
            drawHeight = screenWidth / bgAspect; // Calculate height to maintain aspect ratio
        } else {
            // Background is taller than screen
            drawHeight = screenHeight; // Match screen height
            drawWidth = screenHeight * bgAspect; // Calculate width to maintain aspect ratio
        }

        // Calculate position to center the background
        float x = (screenWidth - drawWidth) / 2;
        float y = (screenHeight - drawHeight) / 2;

        // Draw the background image
        batch.draw(background, x, y, drawWidth, drawHeight);
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport to maintain the aspect ratio
        stage.getViewport().update(width, height, true);

        // Resize and reposition buttons and title
        positionButtons();
        positionTitle();
    }

    private void positionButtons() {
        // Get the screen width and height from the viewport
        float screenWidth = stage.getViewport().getWorldWidth();
        float screenHeight = stage.getViewport().getWorldHeight();

        // Position buttons dynamically based on the viewport size
        startButton.setPosition(screenWidth / 2 - startButton.getWidth() / 2,
                                screenHeight / 2 - startButton.getHeight() - 10);
        exitButton.setPosition(screenWidth / 2 - exitButton.getWidth() / 2,
                               screenHeight / 2 - exitButton.getHeight() - 10 - startButton.getHeight());
    }

    private void positionTitle() {
        // Get the screen width and height from the viewport
        float screenWidth = stage.getViewport().getWorldWidth();
        float screenHeight = stage.getViewport().getWorldHeight();

        // Position the title label in the center of the screen and above the buttons
        titleLabel.setPosition(screenWidth / 2 - titleLabel.getWidth() / 2, 
                               screenHeight / 2 + 25); // Adjust 25 pixels above the buttons
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        // Dispose resources
        stage.dispose();
        Mtheme.dispose();
        start.dispose();
        exit.dispose();
        background.dispose();
        batch.dispose();
        font.dispose();
    }
}
