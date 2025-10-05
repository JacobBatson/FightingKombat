package Game;


import Engine.DefaultScreen;
import Engine.GraphicsHandler;
import Engine.Screen;
import Screens.CreditsScreen;
import Screens.MenuScreen;
import Screens.PlayLevelScreen;
import Screens.CharacterSelectionScreen;
import Screens.ControlsScreen; // <— added

public class ScreenCoordinator extends Screen {
    // currently shown Screen
    protected Screen currentScreen = new DefaultScreen();

    // keep track of gameState so ScreenCoordinator knows which Screen to show
    protected GameState gameState;
    protected GameState previousGameState;

    public GameState getGameState() {
        return gameState;
    }

    // Other Screens can set the gameState of this class to force it to change the currentScreen
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void initialize() {
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
                        break;
                    case CHARACTER_SELECT:
                        currentScreen = new CharacterSelectionScreen(this);
                        break;
                    case LEVEL:
                        currentScreen = new PlayLevelScreen(this);
                        break;
                    case CREDITS:
                        currentScreen = new CreditsScreen(this);
                        break;
                    case Controls:   // <--- new case
                        currentScreen = new ControlsScreen(this);
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
