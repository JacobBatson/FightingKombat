package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Level.PlayerListener;
import Maps.TestMap;
import Players.Player1;   // WASD/E controls
import Players.Player2;   // Arrow/Enter controls
// ...existing code...
import SpriteFont.SpriteFont; // Importing SpriteFont for timer display

public class PlayLevelScreen extends Screen implements PlayerListener {
    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player1 player1;
    protected Player2 player2;
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
        // Map
        this.map = new TestMap();

        // Read both picks from the selection screen
        String p1Pick = CharacterSelectionScreen.getP1SelectedCharacter();
        String p2Pick = CharacterSelectionScreen.getP2SelectedCharacter();
        System.out.println("[Level] P1=" + p1Pick + "  P2=" + p2Pick);

        // Spawn positions
        int p1StartX = 100, p1StartY = 200;
        int p2StartX = 300, p2StartY = 200;

        // Build each player from their pick (both can be Fire or Water)
        String p1Sprite = spriteFor(p1Pick);
        String p2Sprite = spriteFor(p2Pick);

        this.player1 = new Player1(p1StartX, p1StartY, p1Sprite, 64, 64);
        this.player1.setMap(map);
        System.out.println("[Spawn] P1 -> " + p1Sprite);

        this.player2 = new Player2(p2StartX, p2StartY, p2Sprite, 64, 64);
        this.player2.setMap(map);
        System.out.println("[Spawn] P2 -> " + p2Sprite);

        levelClearedScreen = new LevelClearedScreen();
        levelLoseScreen   = new LevelLoseScreen(this);

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

    // Map character name -> sprite file
    private String spriteFor(String name) {
        if ("Water Dude".equals(name)) return "Water_Sprite.png";
        // default/fallback
        return "Fire_Sprite.png";
    }

    @Override
    public void update() {
        switch (playLevelScreenState) {
            case RUNNING:
                player1.update();
                player2.update();
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
        int centerX = 400; // screen center (assuming 800px width)
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(graphicsHandler);
                player1.draw(graphicsHandler);
                player2.draw(graphicsHandler);
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
                player1.draw(graphicsHandler);
                player2.draw(graphicsHandler);
                timerFont.setText("00:00");
                timerFont.setX(centerX - 5 * 14); // "00:00" is 5 chars, larger font
                timerFont.draw(graphicsHandler);
                gameOverFont.draw(graphicsHandler);
                break;
        }
    }

    public PlayLevelScreenState getPlayLevelScreenState() { return playLevelScreenState; }

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

    public void resetLevel() { initialize(); }

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
