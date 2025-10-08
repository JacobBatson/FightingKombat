package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import SpriteFont.SpriteFont;

import java.awt.*;

public class ControlsScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected KeyLocker keyLocker = new KeyLocker();

    // Text
    protected SpriteFont titleLabel;
    protected SpriteFont p1Header, p2Header;
    protected SpriteFont p1Move, p1Jump, p1Attack, p1Special;
    protected SpriteFont p2Move, p2Jump, p2Attack, p2Special;
    protected SpriteFont backHint;

    public ControlsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // Title
        titleLabel = new SpriteFont("CONTROLS", 270, 40, "Times New Roman", 36, Color.WHITE);
        titleLabel.setOutlineColor(Color.BLACK);
        titleLabel.setOutlineThickness(3);

        // Headers
        p1Header = new SpriteFont("Player 1", 60, 110, "Arial", 28, new Color(135, 206, 250));
        p1Header.setOutlineColor(Color.BLACK);
        p1Header.setOutlineThickness(2);

        p2Header = new SpriteFont("Player 2", 420, 110, "Arial", 28, new Color(250, 128, 114));
        p2Header.setOutlineColor(Color.BLACK);
        p2Header.setOutlineThickness(2);

        // Player 1 lines (W = jump; A/S/D move; E special; attack not assigned)
        p1Move    = new SpriteFont("Move:    A / S / D", 60, 150, "Arial", 24, Color.WHITE);
        p1Jump    = new SpriteFont("Jump:    W",          60, 180, "Arial", 24, Color.WHITE);
        p1Attack  = new SpriteFont("Attack:  F", 60, 210, "Arial", 24, Color.WHITE);
        p1Special = new SpriteFont("Special: E",          60, 240, "Arial", 24, Color.WHITE);

        // Player 2 lines (Up = jump; Left/Down/Right move; Enter special; attack not assigned)
        p2Move    = new SpriteFont("Move:    Left / Down / Right Arrows", 420, 150, "Arial", 24, Color.WHITE);
        p2Jump    = new SpriteFont("Jump:    Up Arrow",                   420, 180, "Arial", 24, Color.WHITE);
        p2Attack  = new SpriteFont("Attack:  L",               420, 210, "Arial", 24, Color.WHITE);
        p2Special = new SpriteFont("Special: Enter",                      420, 240, "Arial", 24, Color.WHITE);

        // Footer / hint
        backHint = new SpriteFont("Press SPACE to return to Menu", 15, 560, "Times New Roman", 24, Color.WHITE);
        backHint.setOutlineColor(Color.BLACK);
        backHint.setOutlineThickness(2);

        // Optional: prevent immediate repeat when holding SPACE
        keyLocker.lockKey(Key.SPACE);
    }

    @Override
    public void update() {
        // Return to menu on SPACE (debounced)
        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }
        if (!keyLocker.isKeyLocked(Key.SPACE) && Keyboard.isKeyDown(Key.SPACE)) {
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        // black background
        graphicsHandler.drawFilledRectangle(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, Color.BLACK);

        titleLabel.draw(graphicsHandler);

        p1Header.draw(graphicsHandler);
        p1Move.draw(graphicsHandler);
        p1Jump.draw(graphicsHandler);
        p1Attack.draw(graphicsHandler);
        p1Special.draw(graphicsHandler);

        p2Header.draw(graphicsHandler);
        p2Move.draw(graphicsHandler);
        p2Jump.draw(graphicsHandler);
        p2Attack.draw(graphicsHandler);
        p2Special.draw(graphicsHandler);

        backHint.draw(graphicsHandler);
    }
}
