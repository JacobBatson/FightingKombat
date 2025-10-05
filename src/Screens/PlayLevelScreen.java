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
import Screens.CharacterSelectionScreen;

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
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(graphicsHandler);
                player1.draw(graphicsHandler);
                player2.draw(graphicsHandler);
                break;
            case LEVEL_COMPLETED:
                levelClearedScreen.draw(graphicsHandler);
                break;
            case LEVEL_LOSE:
                levelLoseScreen.draw(graphicsHandler);
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

    private enum PlayLevelScreenState { RUNNING, LEVEL_COMPLETED, LEVEL_LOSE }
}
