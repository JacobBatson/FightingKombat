package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import SpriteFont.SpriteFont;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicSelectionScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected KeyLocker keyLocker = new KeyLocker();

    private List<String> musicFiles;
    private List<String> musicNames;
    private int selectedIndex = 0;
    private int keyPressTimer = 0;
    private String currentlyPlaying = null;

    // Text elements
    protected SpriteFont titleLabel;
    protected List<SpriteFont> musicLabels;
    protected SpriteFont backHint;
    protected SpriteFont playHint;

    public MusicSelectionScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
        initializeMusicList();
    }

    private void initializeMusicList() {
        musicFiles = new ArrayList<>();
        musicNames = new ArrayList<>();

        musicFiles.add("Resources/Headlines (Clean) - Drake.wav");
        musicNames.add("Headlines - Drake");

        musicFiles.add("Resources/Baby Keem, Kendrick Lamar - family ties (Clean - Lyrics) (2).wav");
        musicNames.add("family ties - Baby Keem & Kendrick Lamar");

        musicFiles.add("Resources/All of the Lights (Clean) - Kanye West (feat. Rihanna & Kid Cudi).wav");
        musicNames.add("All of the Lights - Kanye West");

        musicFiles.add("Resources/Don Toliver - No Pole (CLEAN).wav");
        musicNames.add("No Pole - Don Toliver");

        musicFiles.add("Resources/Rihanna - Don't Stop The Music.wav");
        musicNames.add("Don't Stop The Music - Rihanna");

        musicFiles.add("Resources/[CLEAN] J. Cole - m y . l i f e (with 21 Savage & Morray).wav");
        musicNames.add("m y . l i f e - J. Cole");

        musicLabels = new ArrayList<>();
    }

    @Override
    public void initialize() {
        // Title
        titleLabel = new SpriteFont("MUSIC SELECTION", 200, 40, "Times New Roman", 36, Color.WHITE);
        titleLabel.setOutlineColor(Color.BLACK);
        titleLabel.setOutlineThickness(3);

        // Create labels for each music file
        musicLabels.clear();
        int startY = 100;
        int lineHeight = 35;

        for (int i = 0; i < musicNames.size(); i++) {
            SpriteFont label = new SpriteFont((i + 1) + ". " + musicNames.get(i), 100, startY + (i * lineHeight),
                    "Arial", 24, Color.WHITE);
            label.setOutlineColor(Color.BLACK);
            label.setOutlineThickness(2);
            musicLabels.add(label);
        }

        // Hints
        backHint = new SpriteFont("Press SPACE to return to Menu", 15, 560, "Times New Roman", 24, Color.WHITE);
        backHint.setOutlineColor(Color.BLACK);
        backHint.setOutlineThickness(2);

        playHint = new SpriteFont("Press ENTER to play selected song", 15, 520, "Times New Roman", 24, Color.WHITE);
        playHint.setOutlineColor(Color.BLACK);
        playHint.setOutlineThickness(2);

        // Lock keys to prevent immediate input
        keyLocker.lockKey(Key.SPACE);
        keyLocker.lockKey(Key.ENTER);
        keyPressTimer = 0;
        selectedIndex = 0;

        // Check if there's a manually selected song and update currentlyPlaying
        MusicManager musicManager = screenCoordinator.getMusicManager();
        String selectedSong = musicManager.getManuallySelectedSong();
        if (selectedSong != null) {
            // Find the index of the selected song
            for (int i = 0; i < musicFiles.size(); i++) {
                if (musicFiles.get(i).equals(selectedSong)) {
                    currentlyPlaying = musicNames.get(i);
                    break;
                }
            }
        }
    }

    @Override
    public void update() {
        if (Keyboard.isKeyDown(Key.UP) && keyPressTimer == 0) {
            keyPressTimer = 14;
            selectedIndex--;
            if (selectedIndex < 0) {
                selectedIndex = musicFiles.size() - 1;
            }
        } else if (Keyboard.isKeyDown(Key.DOWN) && keyPressTimer == 0) {
            keyPressTimer = 14;
            selectedIndex++;
            if (selectedIndex >= musicFiles.size()) {
                selectedIndex = 0;
            }
        } else {
            if (keyPressTimer > 0)
                keyPressTimer--;
        }

        for (int i = 0; i < musicLabels.size(); i++) {
            if (i == selectedIndex) {
                musicLabels.get(i).setColor(new Color(255, 215, 0));
            } else {
                musicLabels.get(i).setColor(Color.WHITE);
            }
        }

        if (Keyboard.isKeyUp(Key.ENTER)) {
            keyLocker.unlockKey(Key.ENTER);
        }
        if (!keyLocker.isKeyLocked(Key.ENTER) && Keyboard.isKeyDown(Key.ENTER)) {
            playSelectedMusic();
            keyLocker.lockKey(Key.ENTER);
        }

        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }
        if (!keyLocker.isKeyLocked(Key.SPACE) && Keyboard.isKeyDown(Key.SPACE)) {
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    private void playSelectedMusic() {
        if (selectedIndex >= 0 && selectedIndex < musicFiles.size()) {
            String musicFile = musicFiles.get(selectedIndex);
            // Verify file exists
            File file = new File(musicFile);
            if (file.exists()) {
                MusicManager musicManager = screenCoordinator.getMusicManager();
                // Stop current music before playing new one
                musicManager.stopCurrentMusic();
                // Set as manually selected song so it persists across screens
                musicManager.setManuallySelectedSong(musicFile);
                musicManager.playMusic(musicFile);
                currentlyPlaying = musicNames.get(selectedIndex);
                System.out.println("Now playing: " + currentlyPlaying);
            } else {
                System.out.println("Music file not found: " + musicFile);
            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        graphicsHandler.drawFilledRectangle(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, Color.BLACK);

        titleLabel.draw(graphicsHandler);

        for (SpriteFont label : musicLabels) {
            label.draw(graphicsHandler);
        }

        if (selectedIndex >= 0 && selectedIndex < musicLabels.size()) {
            int indicatorX = 70;
            int indicatorY = (int) musicLabels.get(selectedIndex).getY() + 5;
            graphicsHandler.drawFilledRectangleWithBorder(
                    indicatorX, indicatorY, 20, 20,
                    new Color(255, 215, 0), Color.black, 2);
        }

        if (currentlyPlaying != null) {
            SpriteFont playingLabel = new SpriteFont("Now Playing: " + currentlyPlaying, 100, 350,
                    "Arial", 20, new Color(0, 255, 0));
            playingLabel.setOutlineColor(Color.BLACK);
            playingLabel.setOutlineThickness(2);
            playingLabel.draw(graphicsHandler);
        }

        backHint.draw(graphicsHandler);
        playHint.draw(graphicsHandler);
    }
}
