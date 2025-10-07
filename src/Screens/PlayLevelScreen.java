package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import Engine.ScreenManager;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Level.PlayerListener;
import Maps.TestMap;
import Players.Player1;   // WASD/E controls
import Players.Player2;   // Arrow/Enter controls
// ...existing code...
import SpriteFont.SpriteFont; // Importing SpriteFont for timer display
import Utils.HealthHUD;
import Level.MapEntityStatus;
import Enemies.Fireball;

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
    protected SpriteFont player1Label;
    protected SpriteFont player2Label;
    protected HealthHUD healthHUD = new HealthHUD();
    protected String gameOverMessage = "Time's Up!";

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
        levelLoseScreen = new LevelLoseScreen(this);

        this.playLevelScreenState = PlayLevelScreenState.RUNNING;
        timerSeconds = 120;
        lastTimerUpdate = System.currentTimeMillis();
        showGameOver = false;
        gameOverFrames = 0;

        timerFont = new SpriteFont("", 0, 10, "Arial", 44, java.awt.Color.YELLOW);
        timerFont.setOutlineColor(java.awt.Color.BLACK);
        timerFont.setOutlineThickness(2f);

        player1Label = new SpriteFont("P1", 20, 8, "Arial", 20, java.awt.Color.WHITE);
        player1Label.setOutlineColor(java.awt.Color.BLACK);
        player1Label.setOutlineThickness(2f);

        player2Label = new SpriteFont("P2", ScreenManager.getScreenWidth() - 70, 8, "Arial", 20, java.awt.Color.WHITE);
        player2Label.setOutlineColor(java.awt.Color.BLACK);
        player2Label.setOutlineThickness(2f);

        gameOverFont = new SpriteFont("", 0, 180, "Arial", 64, java.awt.Color.RED);
        gameOverFont.setOutlineColor(java.awt.Color.BLACK);
        gameOverFont.setOutlineThickness(3f);
        updateGameOverMessage("Time's Up!");
    }

    // Map character name -> sprite file
    private String spriteFor(String name) {
        if ("Water Dude".equals(name))
            return "Water_Sprite.png";
        // default/fallback
        return "Fire_Sprite.png";
    }

    @Override
    public void update() {
        switch (playLevelScreenState) {
            case RUNNING:
                player1.update();
                player2.update();
                handleProjectileCollisions();
                // Timer logic
                long now = System.currentTimeMillis();
                if (now - lastTimerUpdate >= 1000) {
                    timerSeconds--;
                    lastTimerUpdate = now;
                }
                if (timerSeconds <= 0) {
                    setGameOver("Time's Up!");
                }

                if (player1.isDead() || player2.isDead()) {
                    if (player1.isDead() && player2.isDead()) {
                        setGameOver("Draw!");
                    } else if (player1.isDead()) {
                        setGameOver("Player 2 Wins!");
                    } else {
                        setGameOver("Player 1 Wins!");
                    }
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
        int centerX = ScreenManager.getScreenWidth() / 2;
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
                drawHealthBars(graphicsHandler);
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
                drawHealthBars(graphicsHandler);
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

    private void handleProjectileCollisions() {
        for (Fireball fb : player1.getFireballs()) {
            if (fb.getMapEntityStatus() == MapEntityStatus.REMOVED) {
                continue;
            }
            if (!player2.isInvincible() && fb.intersects(player2)) {
                player2.takeDamage(1);
                fb.setMapEntityStatus(MapEntityStatus.REMOVED);
            }
        }

        for (Fireball fb : player2.getFireballs()) {
            if (fb.getMapEntityStatus() == MapEntityStatus.REMOVED) {
                continue;
            }
            if (!player1.isInvincible() && fb.intersects(player1)) {
                player1.takeDamage(1);
                fb.setMapEntityStatus(MapEntityStatus.REMOVED);
            }
        }
    }

    private void drawHealthBars(GraphicsHandler graphicsHandler) {
        int startY = 20;
        int leftStartX = 60;
        healthHUD.drawHearts(graphicsHandler, player1.getCurrentHealth(), player1.getMaxHealth(), leftStartX, startY, true);
        player1Label.setX(20);
        player1Label.setY(8);
        player1Label.draw(graphicsHandler);

        int heartWidth = player2.getMaxHealth() * healthHUD.getHeartSize()
                + (player2.getMaxHealth() - 1) * healthHUD.getHeartSpacing();
        int rightEdge = ScreenManager.getScreenWidth() - 20;
        int rightStartX = rightEdge - healthHUD.getHeartSize();
        healthHUD.drawHearts(graphicsHandler, player2.getCurrentHealth(), player2.getMaxHealth(), rightStartX, startY, false);
        float labelX = Math.max(20, rightEdge - heartWidth - 60);
        player2Label.setX(labelX);
        player2Label.setY(8);
        player2Label.draw(graphicsHandler);
    }

    private void setGameOver(String message) {
        if (playLevelScreenState == PlayLevelScreenState.GAME_OVER && message.equals(gameOverMessage)) {
            return;
        }
        updateGameOverMessage(message);
        showGameOver = true;
        playLevelScreenState = PlayLevelScreenState.GAME_OVER;
        gameOverFrames = 0;
    }

    private void updateGameOverMessage(String message) {
        this.gameOverMessage = message;
        gameOverFont.setText(message);
        int approxWidth = (int) (message.length() * (gameOverFont.getFont().getSize() * 0.55f));
        int centerX = ScreenManager.getScreenWidth() / 2;
        gameOverFont.setX(centerX - approxWidth / 2f);
    }
    // This enum represents the different states this screen can be in
    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED, LEVEL_LOSE, GAME_OVER
    }
}
