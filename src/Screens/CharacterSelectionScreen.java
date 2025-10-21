package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectionScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected int currentCharacterHovered = 0;
    protected int characterSelected = -1;
    protected List<String> characterNames;
    protected Map background;
    protected int keyPressTimer;
    protected KeyLocker keyLocker = new KeyLocker();
    protected SpriteFont titleText;
    protected SpriteFont backText;
    protected SpriteFont selectText;
    protected int animationTimer;

    // ===== 2P selection state (kept) =====
    private static String p1SelectedCharacter = "";
    private static String p2SelectedCharacter = "";
    public static String getP1SelectedCharacter() { return p1SelectedCharacter; }
    public static String getP2SelectedCharacter() { return p2SelectedCharacter; }

    private boolean pickingP1 = true;     // P1 picks first, then P2
    private int p1SelectedIndex = -1;     // blue lock ring
    private int p2SelectedIndex = -1;     // red lock ring
    private int selectCooldown = 0;       // so one press can't select for both

    // Hover/lock colors
    private static final Color P1_HOVER = new Color(70, 160, 255);
    private static final Color P2_HOVER = new Color(255, 80, 80);
    private static final Color P1_LOCK  = new Color(70, 160, 255);
    private static final Color P2_LOCK  = new Color(255, 80, 80);
    private boolean useRedHover = false;  // after P1 locks, P2 hover turns red

    // ===== Grid layout =====
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 4;
    private static final int CARD_WIDTH = 136;
    private static final int CARD_HEIGHT = 96;
    private static final int CARD_SPACING_X = 14;
    private static final int CARD_SPACING_Y = 20;
    private static final int GRID_START_X = 28;
    private static final int GRID_START_Y = 95;

    // ===== Thumbnails (new) =====
    // Size of each frame on your sheets:
    private static final int TILE_W = 64;
    private static final int TILE_H = 64;
    // Which frame to show as a mini preview (col,row):
    private static final int FIRE_THUMB_COL = 0, FIRE_THUMB_ROW = 0;
    private static final int WATER_THUMB_COL = 0, WATER_THUMB_ROW = 0;
    // On-card display size:
    private static final int THUMB_SIZE = 70;

    private BufferedImage fireDudeThumb;
    private BufferedImage waterDudeThumb;
    private BufferedImage rockDudeThumb;

    public CharacterSelectionScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
        initializeCharacters();
    }

    private void initializeCharacters() {
        characterNames = new ArrayList<>();
        // 4 × 5 = 20 slots; first three are real, rest are placeholders
        characterNames.add("Fire Dude");
        characterNames.add("Water Dude");   // directly under Fire Dude
        characterNames.add("Rock Dude");    // third character
        characterNames.add("Bug");
        characterNames.add("Dinosaur");
        characterNames.add("Walrus");
        characterNames.add("Fireball");
        characterNames.add("Gold Box");
        characterNames.add("Bug");
        characterNames.add("Dinosaur");
        characterNames.add("Walrus");
        characterNames.add("Fireball");
        characterNames.add("Gold Box");
        characterNames.add("Bug");
        characterNames.add("Dinosaur");
        characterNames.add("Walrus");
        characterNames.add("Fireball");
        characterNames.add("Gold Box");
        characterNames.add("Bug");
        characterNames.add("Dinosaur");
        characterNames.add("Walrus");
    }

    @Override
    public void initialize() {
        titleText = new SpriteFont("SELECT CHARACTER", 200, 50, "Arial", 40, new Color(49, 207, 240));
        titleText.setOutlineColor(Color.black);
        titleText.setOutlineThickness(3);

        backText = new SpriteFont("BACK", 50, 50, "Arial", 30, new Color(49, 207, 240));
        backText.setOutlineColor(Color.black);
        backText.setOutlineThickness(3);

        selectText = new SpriteFont("", 0, 0, "Arial", 18, Color.white);
        selectText.setOutlineColor(Color.black);
        selectText.setOutlineThickness(2);

        background = null;
        keyPressTimer = 0;
        characterSelected = -1;
        animationTimer = 0;

        // reset 2P flow each time
        p1SelectedCharacter = "";
        p2SelectedCharacter = "";
        pickingP1 = true;
        useRedHover = false;
        p1SelectedIndex = -1;
        p2SelectedIndex = -1;
        selectCooldown = 0;

        // lock keys on entry so held key from previous screen can't auto-select
        keyLocker.lockKey(Key.SPACE);
        keyLocker.lockKey(Key.ENTER);
        keyLocker.lockKey(Key.ESC);

        // Load thumbnails from your sprite sheets
        BufferedImage fireSheet = ImageLoader.load("Fire_Sprite.png");
        if (fireSheet != null) {
            fireDudeThumb = cropFrame(fireSheet, FIRE_THUMB_COL, FIRE_THUMB_ROW, TILE_W, TILE_H);
        }
        BufferedImage waterSheet = ImageLoader.load("Water_Sprite.png");
        if (waterSheet != null) {
            waterDudeThumb = cropFrame(waterSheet, WATER_THUMB_COL, WATER_THUMB_ROW, TILE_W, TILE_H);
        }
        BufferedImage earthSheet = ImageLoader.load("Earth_Sprite.png");
        if (earthSheet != null) {
            rockDudeThumb = cropFrame(earthSheet, FIRE_THUMB_COL, FIRE_THUMB_ROW, TILE_W, TILE_H);
        }
    }

    private BufferedImage cropFrame(BufferedImage sheet, int col, int row, int w, int h) {
        return sheet.getSubimage(col * w, row * h, w, h);
    }

    public void update() {
        animationTimer++;
        if (selectCooldown > 0) selectCooldown--;

        // navigation (cooldown style)
        if (Keyboard.isKeyDown(Key.RIGHT) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentCharacterHovered++;
        } else if (Keyboard.isKeyDown(Key.LEFT) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentCharacterHovered--;
        } else if (Keyboard.isKeyDown(Key.DOWN) && keyPressTimer == 0) {
            keyPressTimer = 14;
            int r = currentCharacterHovered / GRID_COLS;
            int c = currentCharacterHovered % GRID_COLS;
            int nextRow = (r + 1) % GRID_ROWS;
            currentCharacterHovered = nextRow * GRID_COLS + c;
            if (currentCharacterHovered >= characterNames.size()) currentCharacterHovered = c;
        } else if (Keyboard.isKeyDown(Key.UP) && keyPressTimer == 0) {
            keyPressTimer = 14;
            int r = currentCharacterHovered / GRID_COLS;
            int c = currentCharacterHovered % GRID_COLS;
            int prevRow = (r - 1 + GRID_ROWS) % GRID_ROWS;
            currentCharacterHovered = prevRow * GRID_COLS + c;
            if (currentCharacterHovered >= characterNames.size()) currentCharacterHovered = (GRID_ROWS - 1) * GRID_COLS + c;
        } else {
            if (keyPressTimer > 0) keyPressTimer--;
        }

        if (currentCharacterHovered >= characterNames.size()) currentCharacterHovered = 0;
        else if (currentCharacterHovered < 0) currentCharacterHovered = characterNames.size() - 1;

        // edge unlocks
        if (Keyboard.isKeyUp(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);
        if (Keyboard.isKeyUp(Key.ENTER)) keyLocker.unlockKey(Key.ENTER);
        if (Keyboard.isKeyUp(Key.ESC))   keyLocker.unlockKey(Key.ESC);

        // detect a fresh select press
        boolean selectPressed = false;
        if (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE)) {
            keyLocker.lockKey(Key.SPACE);
            selectPressed = true;
        }
        if (Keyboard.isKeyDown(Key.ENTER) && !keyLocker.isKeyLocked(Key.ENTER)) {
            keyLocker.lockKey(Key.ENTER);
            selectPressed = true;
        }

        if (selectCooldown == 0 && selectPressed) {
            if (currentCharacterHovered < characterNames.size()
                    && isSelectable(characterNames.get(currentCharacterHovered))) {

                characterSelected = currentCharacterHovered;

                if (pickingP1) {
                    // Player 1 pick (stay here)
                    p1SelectedIndex = characterSelected;
                    p1SelectedCharacter = characterNames.get(characterSelected);
                    pickingP1 = false;
                    useRedHover = true;      // now P2 hover is red
                    selectCooldown = 12;     // avoid double-pick on same press
                    System.out.println("[Select] P1 chose: " + p1SelectedCharacter);
                    return;
                } else {
                    // Player 2 pick (go start level)
                    p2SelectedIndex = characterSelected;
                    p2SelectedCharacter = characterNames.get(characterSelected);
                    System.out.println("[Select] P2 chose: " + p2SelectedCharacter);
                    screenCoordinator.setGameState(GameState.MAP_SELECT);
                    return;
                }
            }
        }

        // back to menu
        if (!keyLocker.isKeyLocked(Key.ESC) && Keyboard.isKeyDown(Key.ESC)) {
            keyLocker.lockKey(Key.ESC);
            screenCoordinator.setGameState(GameState.MENU);
        }

        // hover hint
        if (isSelectable(characterNames.get(currentCharacterHovered))) {
            String who = pickingP1 ? "P1" : "P2";
            selectText.setText(who + ": Press ENTER/SPACE to select");
            int row = currentCharacterHovered / GRID_COLS;
            int col = currentCharacterHovered % GRID_COLS;
            int x = GRID_START_X + col * (CARD_WIDTH + CARD_SPACING_X);
            int y = GRID_START_Y + row * (CARD_HEIGHT + CARD_SPACING_Y);
            selectText.setX(x + 6);
            selectText.setY(y + CARD_HEIGHT + 18);
        } else {
            selectText.setText("");
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        drawElementBackground(graphicsHandler, 800, 600);
        drawAnimatedTitle(graphicsHandler);
        backText.draw(graphicsHandler);

        for (int i = 0; i < characterNames.size(); i++) {
            String characterName = characterNames.get(i);
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;
            int x = GRID_START_X + col * (CARD_WIDTH + CARD_SPACING_X);
            int y = GRID_START_Y + row * (CARD_HEIGHT + CARD_SPACING_Y);

            boolean isHovered = (i == currentCharacterHovered);
            boolean isLockedByP1 = (i == p1SelectedIndex);
            boolean isLockedByP2 = (i == p2SelectedIndex);

            drawCharacterCard(graphicsHandler, characterName, x, y, isHovered, isLockedByP1, isLockedByP2);
        }

        if (selectText.getText() != null && !selectText.getText().isEmpty()) {
            selectText.draw(graphicsHandler);
        }
    }

    private void drawElementBackground(GraphicsHandler g, int screenWidth, int screenHeight) {
        for (int y = 0; y < screenHeight; y++) {
            float ratio = (float) y / screenHeight;
            int r = (int) (30 + ratio * 30);
            int gg = (int) (40 + ratio * 40);
            int b = (int) (80 + ratio * 60);
            g.drawFilledRectangle(0, y, screenWidth, 1, new Color(r, gg, b));
        }
    }

    private void drawAnimatedTitle(GraphicsHandler graphicsHandler) {
        int baseX = 180;
        int baseY = 35;
        int jitterX = (int) (Math.sin(animationTimer * 0.25) * 2) + (int) (Math.sin(animationTimer * 0.6) * 1);
        int jitterY = (int) (Math.cos(animationTimer * 0.35) * 1) + (int) (Math.sin(animationTimer * 0.5) * 1);
        SpriteFont animatedTitle = new SpriteFont("SELECT CHARACTER",
                baseX + jitterX, baseY + jitterY, "Arial", 34, new Color(49, 207, 240));
        animatedTitle.setOutlineColor(Color.black);
        animatedTitle.setOutlineThickness(4);
        animatedTitle.draw(graphicsHandler);
    }

    private void drawCharacterCard(GraphicsHandler g, String characterName, int x, int y,
                                   boolean isHovered, boolean isLockedByP1, boolean isLockedByP2) {

        boolean selectable = isSelectable(characterName);

        // hover ring (blue for P1, red for P2)
        if (isHovered) {
            Color tint = useRedHover ? P2_HOVER : P1_HOVER;
            Color glowFill = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 70);
            g.drawFilledRectangleWithBorder(x - 6, y - 6, CARD_WIDTH + 12, CARD_HEIGHT + 12, glowFill, tint, 3);
        }

        // card body
        Color cardColor;
        Color borderColor;
        if (selectable) {
            cardColor = isHovered ? new Color(255, 215, 0, 200) : new Color(49, 207, 240, 200);
            borderColor = isHovered ? Color.white : Color.black;
        } else {
            cardColor = new Color(150, 150, 150, 150);
            borderColor = new Color(100, 100, 100);
        }
        g.drawFilledRectangleWithBorder(x, y, CARD_WIDTH, CARD_HEIGHT, cardColor, borderColor, 3);

        // locked rings
        if (isLockedByP1) {
            Color lockFill = new Color(P1_LOCK.getRed(), P1_LOCK.getGreen(), P1_LOCK.getBlue(), 50);
            g.drawFilledRectangleWithBorder(x - 8, y - 8, CARD_WIDTH + 16, CARD_HEIGHT + 16, lockFill, P1_LOCK, 4);
        }
        if (isLockedByP2) {
            Color lockFill = new Color(P2_LOCK.getRed(), P2_LOCK.getGreen(), P2_LOCK.getBlue(), 50);
            g.drawFilledRectangleWithBorder(x - 8, y - 8, CARD_WIDTH + 16, CARD_HEIGHT + 16, lockFill, P2_LOCK, 4);
        }

        // tiny preview bottom-right
        int drawX = x + CARD_WIDTH - THUMB_SIZE - 8;
        int drawY = y + CARD_HEIGHT - THUMB_SIZE - 8;
        if ("Fire Dude".equals(characterName) && fireDudeThumb != null) {
            g.drawImage(fireDudeThumb, drawX, drawY, THUMB_SIZE, THUMB_SIZE);
        }
        if ("Water Dude".equals(characterName) && waterDudeThumb != null) {
            g.drawImage(waterDudeThumb, drawX, drawY, THUMB_SIZE, THUMB_SIZE);
        }
        if ("Rock Dude".equals(characterName) && rockDudeThumb != null) {
            g.drawImage(rockDudeThumb, drawX, drawY, THUMB_SIZE, THUMB_SIZE);
        }

        // labels
        int textX = x + 8;
        int textY = y + 12;
        Color nameColor = selectable ? Color.black : new Color(100, 100, 100);
        new SpriteFont(characterName, textX, textY, "Arial", 12, nameColor).draw(g);

        String role = roleFor(characterName);
        Color roleColor = selectable ? new Color(100, 100, 100) : new Color(150, 150, 150);
        new SpriteFont(role, textX, textY + 16, "Arial", 10, roleColor).draw(g);

        if (!selectable) {
            g.drawFilledRectangle(x + 8, y + 34, CARD_WIDTH - 16, 14, new Color(0, 0, 0, 150));
            new SpriteFont("COMING SOON", x + 11, y + 45, "Arial", 9, Color.white).draw(g);
        }
    }

    private boolean isSelectable(String name) {
        return "Fire Dude".equals(name) || "Water Dude".equals(name) || "Rock Dude".equals(name);
    }

    private String roleFor(String name) {
        if ("Fire Dude".equals(name))  return "Fire User";
        if ("Water Dude".equals(name)) return "Water User";
        if ("Rock Dude".equals(name))  return "Earth User";
        return "Coming Soon";
    }

    public String getSelectedCharacter() {
        if (characterSelected >= 0 && characterSelected < characterNames.size()) {
            return characterNames.get(characterSelected);
        }
        return characterNames.get(0);
    }
}
