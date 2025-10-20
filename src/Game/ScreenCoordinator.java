package Game;

import Engine.DefaultScreen;
import Engine.GraphicsHandler;
import Engine.MusicManager;
import Engine.Screen;
import Screens.CreditsScreen;
import Screens.MenuScreen;
import Screens.ControlsScreen;
import Screens.PlayLevelScreen;
import Screens.CharacterSelectionScreen;
import Screens.MapSelectionScreen;

public class ScreenCoordinator extends Screen {
    // currently shown Screen
    protected Screen currentScreen = new DefaultScreen();

    // keep track of gameState so ScreenCoordinator knows which Screen to show
    protected GameState gameState;
    protected GameState previousGameState;

    // Music manager for handling background music
    protected MusicManager musicManager;

    private String selectedMapKey = "FIRE";
    public void setSelectedMapKey(String key) { this.selectedMapKey = key; }
    public String getSelectedMapKey() { return selectedMapKey; }

    public GameState getGameState() {
        return gameState;
    }

    // Other Screens can set the gameState of this class to force it to change the
    // currentScreen
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // Get the music manager for screens that need to control music
    public MusicManager getMusicManager() {
        return musicManager;
    }

    @Override
    public void initialize() {
        // Initialize music manager
        musicManager = MusicManager.getInstance();

        // start game off with Menu Screen
        gameState = GameState.MENU;
    }

    @Override
    public void update() {
        do {
            if (previousGameState != gameState) {
                switch (gameState) {
                    case MENU:
                        currentScreen = new MenuScreen(this);
                        musicManager.playMusicForScreen("MENU");
                        break;
                    case CHARACTER_SELECT:
                        currentScreen = new CharacterSelectionScreen(this);
                        musicManager.playMusicForScreen("CHARACTER_SELECT");
                        break;
                    case MAP_SELECT:
                        currentScreen = new MapSelectionScreen(this);
                        musicManager.playMusicForScreen("CHARACTER_SELECT");
                        break;
                    case LEVEL:
                        currentScreen = new PlayLevelScreen(this);
                        musicManager.playMusicForScreen("LEVEL");
                        break;
                    case CREDITS:
                        currentScreen = new CreditsScreen(this);
                        musicManager.playMusicForScreen("CREDITS");
                        break;
                    case Controls:
                        currentScreen = new ControlsScreen(this);
                        musicManager.playMusicForScreen("CONTROLS");
                        break;
                }
                currentScreen.initialize();
            }
            previousGameState = gameState;

            currentScreen.update();
        } while (previousGameState != gameState);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        currentScreen.draw(graphicsHandler);
    }
}
