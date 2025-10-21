package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Level.PlayerListener;
import Maps.Map1;
import Maps.Map2;
import Players.Player1; // WASD/E controls
import Players.Player2; // Arrow/Enter controls
                        // ...existing code...
import SpriteFont.SpriteFont; // Importing SpriteFont for timer display
import UI.HeartsHUD;
import UI.HealthBar;
import UI.DamageBar;
import java.awt.Color;
import Enemies.Fireball;
import Engine.ScreenManager;

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
    private HeartsHUD p1HUD;
    private HeartsHUD p2HUD;
    private HealthBar p1HealthBar;
    private HealthBar p2HealthBar;
    private DamageBar p1DamageBar;
    private DamageBar p2DamageBar;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // Map
        String key = screenCoordinator.getSelectedMapKey();
        if ("WATER".equals(key)) {
            this.map = new Map2();
        } else {
            this.map = new Map1();
            map.getCamera().moveY(100);
        }
        // < bring the camera down to the platforms >>>
        
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

        // Initialize HUDs for players (visual only for now)
        this.p1HUD = new HeartsHUD(HeartsHUD.Anchor.LEFT, 12, 12);
        this.p2HUD = new HeartsHUD(HeartsHUD.Anchor.RIGHT, 12, 12);

        // Health bars (per-heart HP)
        this.p1HealthBar = new HealthBar(new Color(0, 192, 64), new Color(0, 0, 0, 160));
        this.p2HealthBar = new HealthBar(new Color(192, 32, 32), new Color(0, 0, 0, 160));

        // Damage bars
        this.p1DamageBar = new DamageBar(new Color(255, 165, 0), new Color(0, 0, 0, 160));
        this.p2DamageBar = new DamageBar(new Color(255, 100, 0), new Color(0, 0, 0, 160));

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

    // Map character name -> sprite file
    private String spriteFor(String name) {
        if ("Water Dude".equals(name))
            return "Water_Sprite.png";
        if ("Rock Dude".equals(name))
            return "Earth_Sprite.png";
        return "Fire_Sprite.png";
    }

    @Override
    public void update() {
        switch (playLevelScreenState) {
            case RUNNING:
                player1.update();
                player2.update();
                java.util.Iterator<Fireball> it1 = player1.getFireballs().iterator();
                while (it1.hasNext()) {
                    Fireball fb = it1.next();
                    try {
                        if (fb.getBounds() != null && player2.getCustomHitboxBounds() != null) {
                            if (fb.getBounds().intersects(player2.getCustomHitboxBounds())) {
                                player2.takeDamage(20);
                                fb.handleMapEntityCollision(player2);
                                it1.remove();
                            }
                        }
                    } catch (Exception ex) {
                    }
                }

                java.util.Iterator<Fireball> it2 = player2.getFireballs().iterator();
                while (it2.hasNext()) {
                    Fireball fb = it2.next();
                    try {
                        if (fb.getBounds() != null && player1.getCustomHitboxBounds() != null) {
                            if (fb.getBounds().intersects(player1.getCustomHitboxBounds())) {
                                player1.takeDamage(20);
                                fb.handleMapEntityCollision(player1);
                                it2.remove();
                            }
                        }
                    } catch (Exception ex) {
                    }
                }

                // Player 1 punching Player 2
                if (player1.getPlayerState() == Level.PlayerState.PUNCHING &&
                        player1.getPunchDuration() == 5) {
                    if (player1.getPunchHitbox() != null && player2.getCustomHitboxBounds() != null) {
                        if (player1.getPunchHitbox().intersects(player2.getCustomHitboxBounds())) {
                            player2.takeDamage(10);
                            player1.addDamageDealt(10);
                        }
                    }
                }

                // Player 2 punching Player 1
                if (player2.getPlayerState() == Level.PlayerState.PUNCHING &&
                        player2.getPunchDuration() == 5) {
                    if (player2.getPunchHitbox() != null && player1.getCustomHitboxBounds() != null) {
                        if (player2.getPunchHitbox().intersects(player1.getCustomHitboxBounds())) {
                            player1.takeDamage(10);
                            player2.addDamageDealt(10);
                        }
                    }
                }

                // Timer logic
                long now = System.currentTimeMillis();
                if (now - lastTimerUpdate >= 1000) {
                    timerSeconds--;
                    lastTimerUpdate = now;
                }
                if (timerSeconds <= 0) {
                    // Determine winner by comparing total remaining health for each player.
                    // Total health = (full hearts * per-heart max) + current heart HP.
                    int p1Total = player1.getHearts() * player1.getHeartHpMax() + player1.getHeartHP();
                    int p2Total = player2.getHearts() * player2.getHeartHpMax() + player2.getHeartHP();

                    String timeoutText;
                    if (p1Total > p2Total) {
                        timeoutText = "Game Winner: Player 1";
                    } else if (p2Total > p1Total) {
                        timeoutText = "Game Winner: Player 2";
                    } else {
                        timeoutText = "Draw!";
                    }

                    showGameOver = true;
                    playLevelScreenState = PlayLevelScreenState.GAME_OVER;
                    int centerXTimeout = ScreenManager.getScreenWidth() / 2;
                    gameOverFont.setText(timeoutText);
                    // approximate centering using characters; keep existing heuristic
                    gameOverFont.setX(centerXTimeout - timeoutText.length() * 18);
                }

                // Check for KO (player out of hearts and HP)
                if (player1.isKO() || player2.isKO()) {
                    String winner = player1.isKO() ? "Player 2" : "Player 1";
                    String winnerText = "Game Winner: " + winner;
                    System.out.println(winnerText);
                    playLevelScreenState = PlayLevelScreenState.GAME_OVER;
                    gameOverFrames = 0;
                    int centerX = ScreenManager.getScreenWidth() / 2;
                    gameOverFont.setText(winnerText);
                    gameOverFont.setX(centerX - winnerText.length() * 18);
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
                int screenW = ScreenManager.getScreenWidth();
                if (p1HUD != null)
                    p1HUD.draw(graphicsHandler, screenW, player1.getHearts(), player1.getMaxHearts(),
                            player1.getHeartHP(), player1.getHeartHpMax());
                if (p2HUD != null)
                    p2HUD.draw(graphicsHandler, screenW, player2.getHearts(), player2.getMaxHearts(),
                            player2.getHeartHP(), player2.getHeartHpMax());

                // Draw health bars under each player's hearts HUD
                if (p1HealthBar != null) {
                    int x = 12;
                    int y = 12 + 20 + 6; // HUD top + heart size estimate + padding
                    int w = 120;
                    int h = 12;
                    p1HealthBar.draw(graphicsHandler, x, y, w, h, player1.getHeartHP(), player1.getHeartHpMax(), false);
                }
                if (p2HealthBar != null) {
                    int w = 120;
                    int h = 12;
                    int x = screenW - 12 - w;
                    int y = 12 + 20 + 6;
                    p2HealthBar.draw(graphicsHandler, x, y, w, h, player2.getHeartHP(), player2.getHeartHpMax(), true);
                }

                if (p1DamageBar != null) {
                    int x = 12;
                    int y = 12 + 20 + 6 + 12 + 3;
                    int w = 120;
                    int h = 8;
                    p1DamageBar.draw(graphicsHandler, x, y, w, h, player1.getDamageDealt(), player1.getMaxDamage(),
                            false);
                }
                if (p2DamageBar != null) {
                    int w = 120;
                    int h = 8;
                    int x = screenW - 12 - w;
                    int y = 12 + 20 + 6 + 12 + 3;
                    p2DamageBar.draw(graphicsHandler, x, y, w, h, player2.getDamageDealt(), player2.getMaxDamage(),
                            true);
                }
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
                int screenW2 = ScreenManager.getScreenWidth();
                if (p1HUD != null)
                    p1HUD.draw(graphicsHandler, screenW2, player1.getHearts(), player1.getMaxHearts(),
                            player1.getHeartHP(), player1.getHeartHpMax());
                if (p2HUD != null)
                    p2HUD.draw(graphicsHandler, screenW2, player2.getHearts(), player2.getMaxHearts(),
                            player2.getHeartHP(), player2.getHeartHpMax());

                if (p1HealthBar != null) {
                    int x = 12;
                    int y = 12 + 20 + 6;
                    int w = 120;
                    int h = 12;
                    p1HealthBar.draw(graphicsHandler, x, y, w, h, player1.getHeartHP(), player1.getHeartHpMax(), false);
                }
                if (p2HealthBar != null) {
                    int w = 120;
                    int h = 12;
                    int x = screenW2 - 12 - w;
                    int y = 12 + 20 + 6;
                    p2HealthBar.draw(graphicsHandler, x, y, w, h, player2.getHeartHP(), player2.getHeartHpMax(), true);
                }

                
                if (p1DamageBar != null) {
                    int x = 12;
                    int y = 12 + 20 + 6 + 12 + 3;
                    int w = 120;
                    int h = 8;
                    p1DamageBar.draw(graphicsHandler, x, y, w, h, player1.getDamageDealt(), player1.getMaxDamage(),
                            false);
                }
                if (p2DamageBar != null) {
                    int w = 120;
                    int h = 8;
                    int x = screenW2 - 12 - w;
                    int y = 12 + 20 + 6 + 12 + 3;
                    p2DamageBar.draw(graphicsHandler, x, y, w, h, player2.getDamageDealt(), player2.getMaxDamage(),
                            true);
                }
                break;
        }
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    // Getter for Player UI
    public Player1 getPlayer1() {
        return player1;
    }

    public Player2 getPlayer2() {
        return player2;
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
