package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import GameObject.Sprite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class MenuScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected int currentMenuItemHovered = 0;
    protected int menuItemSelected = -1;
    protected SpriteFont playGame;
    protected SpriteFont credits;
    protected SpriteFont controls; // <-- added

    // Keep original map to preserve engine/camera/input state, but we will NOT draw it.
    protected Map backgroundMap;

    // Our PNG background as a sprite (pre-scaled once)
    protected Sprite backgroundSprite;

    // New title text
    protected SpriteFont titleText;

    protected int keyPressTimer;
    protected int pointerLocationX, pointerLocationY;
    protected KeyLocker keyLocker = new KeyLocker();

    // Fallback if panel constants are tiny
    private static final int FALLBACK_W = 1280;
    private static final int FALLBACK_H = 720;

    public MenuScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // --- Title text (cursive-like) ---
        titleText = new SpriteFont("Fighting Kombat", 120, 60, "Brush Script MT", 100, new Color(255, 215, 0));
        titleText.setOutlineColor(Color.black);
        titleText.setOutlineThickness(4);

        // --- Menu entries (original) ---
        playGame = new SpriteFont("PLAY GAME", 300, 200, "Arial", 30, new Color(49, 207, 240));
        playGame.setOutlineColor(Color.black);
        playGame.setOutlineThickness(3);

        credits = new SpriteFont("CREDITS", 300, 253, "Arial", 30, new Color(49, 207, 240));
        credits.setOutlineColor(Color.black);
        credits.setOutlineThickness(3);

        // --- NEW: Controls, right under Credits ---
        controls = new SpriteFont("CONTROLS", 300, 306, "Arial", 30, new Color(49, 207, 240));
        controls.setOutlineColor(Color.black);
        controls.setOutlineThickness(3);

        // --- Create original TitleScreenMap ONLY to preserve engine state ---
        backgroundMap = new TitleScreenMap();
        backgroundMap.setAdjustCamera(false);

        // --- Load and pre-scale the PNG once; we will draw this instead of the map ---
        try {
            BufferedImage src = ImageIO.read(new File("Resources/mainpage.png"));

            int w = (GamePanel.WIDTH  >= 320) ? GamePanel.WIDTH  : FALLBACK_W;
            int h = (GamePanel.HEIGHT >= 240) ? GamePanel.HEIGHT : FALLBACK_H;

            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(src, 0, 0, w, h, null);
            g2.dispose();

            backgroundSprite = new Sprite(scaled, 0, 0); // draw at 0,0; no runtime scaling needed
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load Resources/mainpage.png");
        }

        keyPressTimer = 0;
        menuItemSelected = -1;
        keyLocker.lockKey(Key.SPACE);
    }

    public void update() {
        // Preserve engine state updates from the original map (animations, camera/input setup, etc.)
        if (backgroundMap != null) {
            backgroundMap.update(null);
        }

        // --- Original menu logic, extended to 3 items (Play, Credits, Controls) ---
        if (Keyboard.isKeyDown(Key.DOWN) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentMenuItemHovered++;
        } else if (Keyboard.isKeyDown(Key.UP) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentMenuItemHovered--;
        } else {
            if (keyPressTimer > 0) keyPressTimer--;
        }

        // wrap over 3 items (0,1,2)
        if (currentMenuItemHovered > 2) currentMenuItemHovered = 0;
        else if (currentMenuItemHovered < 0) currentMenuItemHovered = 2;

        // highlight hovered item + pointer position
        if (currentMenuItemHovered == 0) {
            playGame.setColor(new Color(255, 215, 0));
            credits.setColor(new Color(49, 207, 240));
            controls.setColor(new Color(49, 207, 240));
            pointerLocationX = 270; pointerLocationY = 205;
        } else if (currentMenuItemHovered == 1) {
            playGame.setColor(new Color(49, 207, 240));
            credits.setColor(new Color(255, 215, 0));
            controls.setColor(new Color(49, 207, 240));
            pointerLocationX = 270; pointerLocationY = 260;
        } else { // 2 = CONTROLS
            playGame.setColor(new Color(49, 207, 240));
            credits.setColor(new Color(49, 207, 240));
            controls.setColor(new Color(255, 215, 0));
            pointerLocationX = 270; pointerLocationY = 315; // aligned with CONTROLS line
        }

        // selection behavior unchanged (Controls does nothing yet)
        if (Keyboard.isKeyUp(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);
        if (!keyLocker.isKeyLocked(Key.SPACE) && Keyboard.isKeyDown(Key.SPACE)) {
            menuItemSelected = currentMenuItemHovered;
            if (menuItemSelected == 0) {
                screenCoordinator.setGameState(GameState.CHARACTER_SELECT);
            } else if (menuItemSelected == 1) {
                screenCoordinator.setGameState(GameState.CREDITS);
            } } else if (menuItemSelected == 2) {           // <-- add this block
                screenCoordinator.setGameState(GameState.Controls);
            }
           
        }
    

    public void draw(GraphicsHandler graphicsHandler) {
        // *** IMPORTANT: draw ONLY our PNG background, NOT the TitleScreenMap. ***
        if (backgroundSprite != null) {
            backgroundSprite.draw(graphicsHandler);
        }

        // Title on top of background
        titleText.draw(graphicsHandler);

        // Original menu UI + new CONTROLS
        playGame.draw(graphicsHandler);
        credits.draw(graphicsHandler);
        controls.draw(graphicsHandler); // new line

        graphicsHandler.drawFilledRectangleWithBorder(
                pointerLocationX, pointerLocationY, 20, 20,
                new Color(49, 207, 240), Color.black, 2
        );
    }
}
