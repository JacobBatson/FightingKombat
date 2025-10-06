package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Level.PlayerListener;
import Maps.TestMap;
import Players.Fighter1;
import Players.Fighter2;
import Screens.CharacterSelectionScreen; // lets us read the selected character
import SpriteFont.SpriteFont; // Importing SpriteFont for timer display

// This class is for when the platformer game is actually being played
public class PlayLevelScreen extends Screen implements PlayerListener {
    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Fighter1 fighter1;
    protected Fighter2 fighter2;
    protected PlayLevelScreenState playLevelScreenState;
    protected int screenTimer;
    protected LevelClearedScreen levelClearedScreen;
    protected LevelLoseScreen levelLoseScreen;
    protected boolean levelCompletedStateChangeStart;
    protected int timerSeconds = 120; // 2 minutes
    protected long lastTimerUpdate = System.currentTimeMillis();
    protected boolean showGameOver = false;
    protected int gameOverFrames = 0;
    protected SpriteFont timerFont;
    protected SpriteFont gameOverFont;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // define/setup map
        this.map = new TestMap();

        // get character choice from selection screen
        String pick = CharacterSelectionScreen.getLastSelectedCharacter();
        System.out.println("Selected character: " + pick);

        // setup fighter1 based on selected character
        if ("Fire Dude".equals(pick)) {
            this.fighter1 = new Fighter1(100, 200);
        } else {
            this.fighter1 = new Fighter1(100, 200); // fallback for now
        }
        this.fighter1.setMap(map);

        this.fighter2 = new Fighter2(300, 200); // Position Fighter2
        this.fighter2.setMap(map);

        levelClearedScreen = new LevelClearedScreen();
        levelLoseScreen = new LevelLoseScreen(this);

        this.playLevelScreenState = PlayLevelScreenState.RUNNING;
        timerSeconds = 120;
        lastTimerUpdate = System.currentTimeMillis();
        showGameOver = false;
        gameOverFrames = 0;
    timerFont = new SpriteFont("", 0, 10, "Arial", 44, java.awt.Color.YELLOW);
    timerFont.setOutlineColor(java.awt.Color.BLACK);
    timerFont.setOutlineThickness(2f);
    int gameOverTextLength = "Time's Up!".length();
    int gameOverFontSize = 64;
    int gameOverCenterX = 400 - (gameOverTextLength * gameOverFontSize / 4); // estimate 32px per char
    gameOverFont = new SpriteFont("Time's Up!", gameOverCenterX, 180, "Arial", 64, java.awt.Color.RED);
    gameOverFont.setOutlineColor(java.awt.Color.BLACK);
    gameOverFont.setOutlineThickness(3f);
    }

    @Override
    public void update() {
        // based on screen state, perform specific actions
        switch (playLevelScreenState) {
            // if level is "running" update player and map to keep game logic for the
            // platformer level going
            case RUNNING:
                fighter1.update();
                fighter2.update();
                // Timer logic
                long now = System.currentTimeMillis();
                if (now - lastTimerUpdate >= 1000) {
                    timerSeconds--;
                    lastTimerUpdate = now;
                }
                if (timerSeconds <= 0) {
                    showGameOver = true;
                    playLevelScreenState = PlayLevelScreenState.GAME_OVER;
                }
                break;

            // if level has been completed, bring up level cleared screen
            case LEVEL_COMPLETED:
                if (levelCompletedStateChangeStart) {
                    screenTimer = 130;
                    levelCompletedStateChangeStart = false;
                } else {
                    levelClearedScreen.update();
                    screenTimer--;
                    if (screenTimer == 0) {
                        goBackToMenu();
                    }
                }
                break;

            // wait on level lose screen to make a decision (either resets level or sends
            // player back to main menu)
            case LEVEL_LOSE:
                levelLoseScreen.update();
                break;
            case GAME_OVER:
                gameOverFrames++;
                if (gameOverFrames >= 180) { // ~3 seconds at 60fps
                    goBackToCharacterSelect();
                }
                break;
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
    // based on screen state, draw appropriate graphics
    int centerX = 400; // screen center (assuming 800px width)
    switch (playLevelScreenState) {
            case RUNNING:
                map.draw(graphicsHandler);
                fighter1.draw(graphicsHandler);
                fighter2.draw(graphicsHandler);
                // Draw timer at top center
                    String timerText = String.format("%02d:%02d", timerSeconds / 60, timerSeconds % 60);
                    timerFont.setText(timerText);
                    timerFont.setX(centerX - timerText.length() * 14); // better centering for larger font
                timerFont.draw(graphicsHandler);
                break;
            case LEVEL_COMPLETED:
                levelClearedScreen.draw(graphicsHandler);
                break;
            case LEVEL_LOSE:
                levelLoseScreen.draw(graphicsHandler);
                break;
            case GAME_OVER:
                map.draw(graphicsHandler);
                fighter1.draw(graphicsHandler);
                fighter2.draw(graphicsHandler);
                timerFont.setText("00:00");
                    timerFont.setX(centerX - 5 * 14); // "00:00" is 5 chars, larger font
                timerFont.draw(graphicsHandler);
                gameOverFont.draw(graphicsHandler);
                break;
        }
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    @Override
    public void onLevelCompleted() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_COMPLETED) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
            levelCompletedStateChangeStart = true;
        }
    }

    @Override
    public void onDeath() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_LOSE) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_LOSE;
        }
    }

    public void resetLevel() {
        initialize();
    }

    public void goBackToMenu() {
        screenCoordinator.setGameState(GameState.MENU);
    }

    public void goBackToCharacterSelect() {
        screenCoordinator.setGameState(GameState.CHARACTER_SELECT);
    }
    // This enum represents the different states this screen can be in
    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED, LEVEL_LOSE, GAME_OVER
    }
}
