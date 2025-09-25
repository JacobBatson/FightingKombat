package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.awt.image.BufferedImage; // <-- added
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

    // Grid layout constants - 4 rows × 5 columns
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 4;
    private static final int CARD_WIDTH = 136;
    private static final int CARD_HEIGHT = 96;
    private static final int CARD_SPACING_X = 14;
    private static final int CARD_SPACING_Y = 20;
    private static final int GRID_START_X = 28;
    private static final int GRID_START_Y = 95;

    private static String lastSelectedCharacter = "Fire Dude";
    public static String getLastSelectedCharacter() { return lastSelectedCharacter; }

    // --- minimal image fields ---
    private BufferedImage fireDudeSheet;   // full sprite sheet
    private BufferedImage fireDudeThumb;   // 1 standing frame

    public CharacterSelectionScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
        initializeCharacters();
    }

    private void initializeCharacters() {
        characterNames = new ArrayList<>();
        // 4 rows × 5 columns = 20 total characters
        characterNames.add("Fire Dude");
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
        characterNames.add("Fireball");
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

        keyLocker.lockKey(Key.SPACE);
        keyLocker.lockKey(Key.ENTER);
        keyLocker.lockKey(Key.ESC);

        // --- load Fire Dude & crop one standing frame (keep it SIMPLE) ---
        // Adjust the path if your engine expects "Resources/New Piskel(1).png"
        fireDudeSheet = ImageLoader.load("New Piskel(1).png");
        if (fireDudeSheet != null) {
            // assume frames are 16x16 and standing frame is at (0,0). Change if needed.
            int fx = 0, fy = 0, fw = 16, fh = 16;
            fireDudeThumb = fireDudeSheet.getSubimage(fx, fy, fw, fh);
        }
    }

    public void update() {
        animationTimer++;

        // Arrow navigation with simple key-repeat cooldown
        if (Keyboard.isKeyDown(Key.RIGHT) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentCharacterHovered++;
        } else if (Keyboard.isKeyDown(Key.LEFT) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentCharacterHovered--;
        } else if (Keyboard.isKeyDown(Key.DOWN) && keyPressTimer == 0) {
            keyPressTimer = 14;
            int currentRow = currentCharacterHovered / GRID_COLS;
            int currentCol = currentCharacterHovered % GRID_COLS;
            int nextRow = (currentRow + 1) % GRID_ROWS;
            currentCharacterHovered = nextRow * GRID_COLS + currentCol;
            if (currentCharacterHovered >= characterNames.size()) {
                currentCharacterHovered = currentCol; // Wrap to first row
            }
        } else if (Keyboard.isKeyDown(Key.UP) && keyPressTimer == 0) {
            keyPressTimer = 14;
            int currentRow = currentCharacterHovered / GRID_COLS;
            int currentCol = currentCharacterHovered % GRID_COLS;
            int prevRow = (currentRow - 1 + GRID_ROWS) % GRID_ROWS;
            currentCharacterHovered = prevRow * GRID_COLS + currentCol;
            if (currentCharacterHovered >= characterNames.size()) {
                currentCharacterHovered = (GRID_ROWS - 1) * GRID_COLS + currentCol; // Wrap to last row
            }
        } else {
            if (keyPressTimer > 0) keyPressTimer--;
        }

        if (currentCharacterHovered >= characterNames.size()) currentCharacterHovered = 0;
        else if (currentCharacterHovered < 0) currentCharacterHovered = characterNames.size() - 1;

        if (Keyboard.isKeyUp(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);
        if (Keyboard.isKeyUp(Key.ENTER)) keyLocker.unlockKey(Key.ENTER);
        if (Keyboard.isKeyUp(Key.ESC))   keyLocker.unlockKey(Key.ESC);

        // Select (only "Fire Dude" is selectable)
        if ((!keyLocker.isKeyLocked(Key.SPACE) && Keyboard.isKeyDown(Key.SPACE)) ||
            (!keyLocker.isKeyLocked(Key.ENTER) && Keyboard.isKeyDown(Key.ENTER))) {

            if (currentCharacterHovered < characterNames.size()
                && characterNames.get(currentCharacterHovered).equals("Fire Dude")) {

                characterSelected = currentCharacterHovered;
                lastSelectedCharacter = getSelectedCharacter();
                screenCoordinator.setGameState(GameState.LEVEL);
            }
        }

        if (!keyLocker.isKeyLocked(Key.ESC) && Keyboard.isKeyDown(Key.ESC)) {
            screenCoordinator.setGameState(GameState.MENU);
        }

        boolean hoverIsSelectable = characterNames.get(currentCharacterHovered).equals("Fire Dude");
        if (hoverIsSelectable) {
            selectText.setText("Press ENTER/SPACE to select");
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
            drawCharacterCard(graphicsHandler, characterName, x, y, i == currentCharacterHovered);
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
        g.drawFilledRectangle(0, 0, 200, 3, new Color(100, 150, 255, 150));
        g.drawFilledRectangle(screenWidth - 200, 0, 200, 3, new Color(100, 150, 255, 150));
        g.drawFilledRectangle(0, screenHeight - 3, 200, 3, new Color(100, 150, 255, 150));
        g.drawFilledRectangle(screenWidth - 200, screenHeight - 3, 200, 3, new Color(100, 150, 255, 150));
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

    private void drawCharacterCard(GraphicsHandler g, String characterName, int x, int y, boolean isHovered) {
        boolean isSelectable = characterName.equals("Fire Dude");

        if (isHovered) {
            Color glowFill = isSelectable ? new Color(255, 255, 255, 100) : new Color(200, 200, 200, 80);
            Color glowBorder = isSelectable ? Color.white : new Color(150, 150, 150);
            g.drawFilledRectangleWithBorder(x - 5, y - 5, CARD_WIDTH + 10, CARD_HEIGHT + 10, glowFill, glowBorder, 3);
        }

        Color cardColor;
        Color borderColor;
        if (isSelectable) {
            cardColor = isHovered ? new Color(255, 215, 0, 200) : new Color(49, 207, 240, 200);
            borderColor = isHovered ? Color.white : Color.black;
        } else {
            cardColor = new Color(150, 150, 150, 150);
            borderColor = new Color(100, 100, 100);
        }
        g.drawFilledRectangleWithBorder(x, y, CARD_WIDTH, CARD_HEIGHT, cardColor, borderColor, 3);

        // --- tiny Fire Dude thumbnail (bottom-right), keeps labels readable ---
        if (characterName.equals("Fire Dude") && fireDudeThumb != null) {
            int size = 32; // thumbnail size on screen
            int drawX = x + CARD_WIDTH - size - 8; // 8px margin from right
            int drawY = y + CARD_HEIGHT - size - 8; // 8px margin from bottom
            g.drawImage(fireDudeThumb, drawX, drawY, size, size);
        }

        int textX = x + 8;
        int textY = y + 12;
        Color nameColor = isSelectable ? Color.black : new Color(100, 100, 100);
        new SpriteFont(characterName, textX, textY, "Arial", 12, nameColor).draw(g);
        String role = isSelectable ? "Fire User" : "Coming Soon";
        Color roleColor = isSelectable ? new Color(100, 100, 100) : new Color(150, 150, 150);
        new SpriteFont(role, textX, textY + 16, "Arial", 10, roleColor).draw(g);

        if (!isSelectable) {
            g.drawFilledRectangle(x + 8, y + 34, CARD_WIDTH - 16, 14, new Color(0, 0, 0, 150));
            new SpriteFont("COMING SOON", x + 11, y + 45, "Arial", 9, Color.white).draw(g);
        }
    }

    public String getSelectedCharacter() {
        if (characterSelected >= 0 && characterSelected < characterNames.size()) {
            return characterNames.get(characterSelected);
        }
        return characterNames.get(0);
    }
}
