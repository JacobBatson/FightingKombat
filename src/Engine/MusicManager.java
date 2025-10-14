package Engine;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MusicManager {
    private static MusicManager instance;
    private Clip currentClip;
    private String currentTrack;
    private boolean isMusicEnabled = true;
    private float volume = 0.7f;

    private Map<String, String> screenMusicMap;

    private MusicManager() {
        screenMusicMap = new HashMap<>();
        initializeScreenMusic();
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    private void initializeScreenMusic() {
        String defaultMusic = "Resources/Baby Keem, Kendrick Lamar - family ties (Lyrics).wav";

        screenMusicMap.put("MENU", defaultMusic);
        screenMusicMap.put("CHARACTER_SELECT", defaultMusic);
        screenMusicMap.put("CREDITS", defaultMusic);
        screenMusicMap.put("CONTROLS", defaultMusic);

        // Add fighting music - Super Smash Bros. Brawl theme
        screenMusicMap.put("LEVEL", "Resources/Main Theme - Super Smash Bros. Brawl Music.wav");

    }

    public void playMusicForScreen(String screenName) {
        if (!isMusicEnabled) {
            return;
        }

        String musicFile = screenMusicMap.get(screenName);
        if (musicFile != null && !musicFile.equals(currentTrack)) {
            stopCurrentMusic();
            playMusic(musicFile);
            currentTrack = musicFile;
        }
    }

    public void playMusic(String musicFilePath) {
        if (!isMusicEnabled) {
            return;
        }

        try {
            File file = new File(musicFilePath);
            if (!file.exists()) {
                System.out.println("Music file not found: " + musicFilePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Set volume
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

            currentClip = clip;
            System.out.println("Playing music: " + musicFilePath);

        } catch (Exception e) {
            System.out.println("Error playing music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopCurrentMusic() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
            currentTrack = null;
        }
    }

    public void pauseMusic() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
    }

    /**
     * Resume the current music
     */
    public void resumeMusic() {
        if (currentClip != null && !currentClip.isRunning()) {
            currentClip.start();
        }
    }

    /**
     * Set the volume (0.0 to 1.0)
     * 
     * @param volume Volume level
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(this.volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    /**
     * Enable or disable music
     * 
     * @param enabled Whether music should be enabled
     */
    public void setMusicEnabled(boolean enabled) {
        this.isMusicEnabled = enabled;
        if (!enabled) {
            stopCurrentMusic();
        }
    }

    /**
     * Check if music is currently playing
     * 
     * @return True if music is playing
     */
    public boolean isMusicPlaying() {
        return currentClip != null && currentClip.isRunning();
    }

    /**
     * Get the current volume
     * 
     * @return Current volume level
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Check if music is enabled
     * 
     * @return True if music is enabled
     */
    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        stopCurrentMusic();
    }
}