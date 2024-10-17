package Screens;
import Screens.Level1Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kimdokja.orv.TheaterDungeon;

import java.util.ArrayList;
import java.util.List;

public class CutsceneScreen implements Screen {
    private TheaterDungeon game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Sound typingSound;
    

    private String[][] dialogues; // 2D array for multiple dialogue sets
    private List<String> displayedLines; // Lines currently displayed on the screen
    private int currentSet = 0; // Current dialogue set index
    private int currentLine = 0; // Current line index for the current set
    private String currentText = ""; // Current text being displayed
    private float timeSinceLastLetter = 0; // Timer for letter typing
    private float typingSpeed = 0.05f; // Time between letters (in seconds)
    
    private static final float LINE_SPACING = 40;
    private static final float FINISHED_LINE_SPACING = 20;// Spacing between lines

    public CutsceneScreen(TheaterDungeon game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("nier.fnt"), Gdx.files.internal("nier.png"), false);
        typingSound = Gdx.audio.newSound(Gdx.files.internal("typingsound.ogg"));
        

        // Initialize dialogue sets
        dialogues = new String[][] {
            {
                "[There are three ways to survive in a ruined world.",
                "Now, I have forgotten a few, but one thing is certain.",
                "The fact that you who are reading this now will survive.",
                "-Three ways to survive in a ruined world]"
            },
            {
            	"The novel I read for 10 years was over.",
                "After 3149 chapters, The story was over.",
                "It was a story about a man who regressed 1863 times.",
                
            },
            {
            	"The story that once saved me, became real",
            	"Fiction and reality overlapped precisely.",
            	"The first of many hellish scenarios to began."
            }
        };
        
        displayedLines = new ArrayList<>(); // Initialize the list of displayed lines
    }

    @Override
    public void show() {
        game.setCutsceneActive(true);
        currentText = ""; // Clear current text
        currentLine = 0; // Start with the first line
        displayedLines.clear(); // Clear displayed lines when the cutscene starts
    }

    @Override
    public void render(float delta) {
        // Clear the screen with a black color
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        
        // Set the text color (e.g., white)
        font.setColor(1, 1, 1, 1); // RGBA, values between 0 and 1
        font.getData().setScale(1.2f);

        // Calculate the starting Y position for the displayed lines
        float totalHeight = (displayedLines.size() * LINE_SPACING) + 
                            ((displayedLines.size() - 1) * FINISHED_LINE_SPACING); // Total height of displayed lines including additional spacing
        float startYPosition = (Gdx.graphics.getHeight() - totalHeight) / 2 + 50; // Centered position for existing lines

        // Draw all currently displayed lines with proper spacing
        for (int i = 0; i < displayedLines.size(); i++) {
            String line = displayedLines.get(i);
            float lineYPosition = startYPosition - (i * LINE_SPACING) - (i * FINISHED_LINE_SPACING); // Position for each displayed line
            float textWidth = font.getRegion().getTexture().getWidth(); // Get text width
            float textXPosition = (Gdx.graphics.getWidth() - textWidth) / 2; // Center the text horizontally
            font.draw(batch, line, textXPosition, lineYPosition);
        }

        // Draw the current text for the current line
        if (currentLine < dialogues[currentSet].length) {
            // Position for the current text is above the previously displayed lines
            float currentTextYPosition = startYPosition - (displayedLines.size() * LINE_SPACING) - (displayedLines.size() * FINISHED_LINE_SPACING) - LINE_SPACING; // Position for current text above existing lines
            float textWidth = font.getRegion().getTexture().getWidth(); // Get text width
            float textXPosition = (Gdx.graphics.getWidth() - textWidth) / 2; // Center the text horizontally
            font.draw(batch, currentText, textXPosition, currentTextYPosition);
        }

        batch.end();

        // Update logic for typing effect
        if (currentLine < dialogues[currentSet].length) {
            timeSinceLastLetter += delta;

            // Typing effect logic
            if (timeSinceLastLetter >= typingSpeed) {
                timeSinceLastLetter = 0;

                // If the current text is not fully displayed, keep adding characters
                if (currentText.length() < dialogues[currentSet][currentLine].length()) {
                    currentText += dialogues[currentSet][currentLine].charAt(currentText.length());
                    typingSound.play(); // Play typing sound
                } else {
                    // Check for user input to advance to the next line
                    if (Gdx.input.justTouched()) {
                        // Skip to the full line display
                        currentText = dialogues[currentSet][currentLine]; // Show full line
                        displayedLines.add(currentText); // Add completed line to displayed lines
                        currentLine++; // Move to the next line
                        currentText = ""; // Clear text for the next line
                    }
                }
            } else {
                // Allow skipping to full text with a click
                if (Gdx.input.justTouched()) {
                    // Skip to the full line display
                    currentText = dialogues[currentSet][currentLine]; // Show full line
                    displayedLines.add(currentText); // Add completed line to displayed lines
                    currentLine++; // Move to the next line
                    currentText = ""; // Clear text for the next line
                }
            }
        } else {
            // Transition to the next dialogue set or back to gameplay
            if (Gdx.input.justTouched()) {
                if (currentSet < dialogues.length - 1) {
                    currentSet++; // Move to the next dialogue set
                    currentLine = 0; // Reset line index for new set
                    currentText = ""; // Clear text for new set
                    displayedLines.clear(); // Clear previously displayed lines
                } else {
                	dispose();
                    FadeScreen fadeScreen = new FadeScreen(game, new Level1Screen(game));
                    game.setScreen(fadeScreen); // Transition to the gameplay screen after all dialogues
                }
            }
        }
    }
    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }
    
    @Override
    public void hide() {
        // When this screen is hidden, reset cutscene status
        game.setCutsceneActive(false);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        typingSound.dispose(); // Dispose of the typing sound
    }
}
