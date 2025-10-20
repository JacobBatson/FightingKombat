package Screens;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Keyboard;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Screen;
import Game.GameState;
import Game.ScreenCoordinator;
import SpriteFont.SpriteFont;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class MapSelectionScreen extends Screen {
    private final ScreenCoordinator screenCoordinator;

    private static final int CARD_W = 136;
    private static final int CARD_H = 96;
    private static final int GAP = 16;
    private static final int SCREEN_W = 800;
    private static final int SCREEN_H = 600;
    private static final int IMAGE_PADDING = 8;

    private static final String[] MAP_LABELS = {"Air", "Earth", "Water", "Fire", "Random"};
    private static final String[] MAP_KEYS    = {"AIR",  "EARTH", "WATER", "FIRE", "RANDOM"};

    private static final boolean[] LOCKED = { true, true, false, false, false };

    private int hovered = 0;

    private final Color bgDim       = new Color(10, 10, 16);
    private final Color cardBG      = new Color(20, 20, 28);
    private final Color cardBorder  = new Color(60, 60, 80);
    private final Color hoverBorder = new Color(255, 200, 80);
    private final Color disabled    = new Color(90, 90, 110);
    private final Color hintColor   = new Color(200, 200, 220);

    private SpriteFont title;
    private SpriteFont hint;

    private final HashMap<String, BufferedImage> mapImages = new HashMap<>();

    private final KeyLocker keyLocker = new KeyLocker();

    public MapSelectionScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        title = new SpriteFont("Select a Map", 260, 96, "Comic Sans", 30, Color.WHITE);
        hint  = new SpriteFont("←/→ move  •  Space select  •  Esc back",
                180, 520, "Comic Sans", 18, hintColor);

        if (!Keyboard.isKeyDown(Key.LEFT))  keyLocker.unlockKey(Key.LEFT);
        if (!Keyboard.isKeyDown(Key.RIGHT)) keyLocker.unlockKey(Key.RIGHT);
        if (!Keyboard.isKeyDown(Key.SPACE)) keyLocker.unlockKey(Key.SPACE);
        if (!Keyboard.isKeyDown(Key.ESC))   keyLocker.unlockKey(Key.ESC);

        loadMapImage("AIR",    "/maps/air.png");
        loadMapImage("EARTH",  "/maps/earth.png");
        putMapImageFromResources("WATER",  "water pic.png");
        putMapImageFromResources("FIRE",   "Fire pic.png");
        putMapImageFromResources("RANDOM", "dice pic.png");
    }

    private void loadMapImage(String key, String resourcePath) {
        try {
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream(resourcePath));
            if (img != null) mapImages.put(key, img);
        } catch (Exception e) {
            System.err.println("Failed to load map image: " + resourcePath + " -> " + e.getMessage());
        }
    }

    private void putMapImageFromResources(String key, String fileName) {
        try {
            BufferedImage img = ImageLoader.load(fileName);
            if (img != null) mapImages.put(key, img);
        } catch (RuntimeException e) {
            System.err.println("Failed to load map image from Resources: " + fileName + " -> " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (Keyboard.isKeyDown(Key.LEFT) && !keyLocker.isKeyLocked(Key.LEFT)) {
            keyLocker.lockKey(Key.LEFT);
            hovered = (hovered + MAP_LABELS.length - 1) % MAP_LABELS.length;
        } else if (!Keyboard.isKeyDown(Key.LEFT)) {
            keyLocker.unlockKey(Key.LEFT);
        }

        if (Keyboard.isKeyDown(Key.RIGHT) && !keyLocker.isKeyLocked(Key.RIGHT)) {
            keyLocker.lockKey(Key.RIGHT);
            hovered = (hovered + 1) % MAP_LABELS.length;
        } else if (!Keyboard.isKeyDown(Key.RIGHT)) {
            keyLocker.unlockKey(Key.RIGHT);
        }

        if (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE)) {
            keyLocker.lockKey(Key.SPACE);

            String key = MAP_KEYS[hovered];

            if ("RANDOM".equals(key)) {
                String[] pool = unlockedChoices();
                if (pool.length > 0) {
                    key = pool[(int)(Math.random() * pool.length)];
                }
            }

            if (!isLockedKey(key)) {
                screenCoordinator.setSelectedMapKey(key);
                screenCoordinator.setGameState(GameState.LEVEL);
            }
        } else if (!Keyboard.isKeyDown(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }

        if (Keyboard.isKeyDown(Key.ESC) && !keyLocker.isKeyLocked(Key.ESC)) {
            keyLocker.lockKey(Key.ESC);
            screenCoordinator.setGameState(GameState.CHARACTER_SELECT);
        } else if (!Keyboard.isKeyDown(Key.ESC)) {
            keyLocker.unlockKey(Key.ESC);
        }
    }

    @Override
    public void draw(GraphicsHandler g) {
        g.drawFilledRectangle(0, 0, SCREEN_W, SCREEN_H, bgDim);

        title.draw(g);
        hint.draw(g);

        int totalWidth = (CARD_W * MAP_LABELS.length) + (GAP * (MAP_LABELS.length - 1));
        int startX = (SCREEN_W - totalWidth) / 2;
        int y = 200;

        for (int i = 0; i < MAP_LABELS.length; i++) {
            int x = startX + i * (CARD_W + GAP);

            boolean locked = LOCKED[i];
            Color border = (i == hovered) ? hoverBorder : cardBorder;
            int borderWidth = (i == hovered && !locked) ? 4 : 2;

            g.drawFilledRectangleWithBorder(
                    x, y, CARD_W, CARD_H,
                    cardBG,
                    locked ? disabled : border,
                    borderWidth
            );

            BufferedImage img = mapImages.get(MAP_KEYS[i]);
            int imgX = x + IMAGE_PADDING;
            int imgY = y + IMAGE_PADDING;
            int imgW = CARD_W - (IMAGE_PADDING * 2);
            int imgH = CARD_H - (IMAGE_PADDING * 2);

            if (img != null) {
                BufferedImage scaled = scaleToFit(img, imgW, imgH);
                g.drawImage(scaled, imgX + (imgW - scaled.getWidth()) / 2, imgY + (imgH - scaled.getHeight()) / 2);
            } else {
                g.drawFilledRectangle(imgX, imgY, imgW, imgH,
                        locked ? disabled.darker() : cardBG.brighter());
            }

            if (locked) {
                g.drawFilledRectangle(imgX, imgY, imgW, imgH, new Color(0, 0, 0, 120));
                SpriteFont lock = new SpriteFont("LOCKED", x + 20, y + (CARD_H / 2) + 6, "Comic Sans", 14, disabled);
                lock.draw(g);
            }

            int labelX = x + (CARD_W / 2) - 20;
            SpriteFont label = new SpriteFont(MAP_LABELS[i], labelX, y + CARD_H + 22, "Comic Sans", 18,
                    locked ? disabled : Color.WHITE);
            label.draw(g);
        }
    }

    private BufferedImage scaleToFit(BufferedImage img, int maxW, int maxH) {
        double ratio = Math.min((double)maxW / img.getWidth(), (double)maxH / img.getHeight());
        int newW = (int)(img.getWidth() * ratio);
        int newH = (int)(img.getHeight() * ratio);
        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(img, 0, 0, newW, newH, null);
        g2.dispose();
        return scaled;
    }

    private boolean isLockedKey(String key) {
        for (int i = 0; i < MAP_KEYS.length; i++) {
            if (MAP_KEYS[i].equals(key)) {
                return LOCKED[i];
            }
        }
        return false;
    }

    private String[] unlockedChoices() {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        for (int i = 0; i < MAP_KEYS.length; i++) {
            if (!LOCKED[i] && !"RANDOM".equals(MAP_KEYS[i])) list.add(MAP_KEYS[i]);
        }
        return list.toArray(new String[0]);
    }
}
