package Engine;

import Game.ScreenCoordinator;
import Game.GameState;
import GameObject.Rectangle;
import SpriteFont.SpriteFont;
import Utils.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;


public class GamePanel extends JPanel {
	// loads Screens on to the JPanel
	// each screen has its own update and draw methods defined to handle a "section" of the game.
	private ScreenManager screenManager;

	// used to draw graphics to the panel
	private GraphicsHandler graphicsHandler;

	private boolean isGamePaused = false;
	private SpriteFont pauseLabel;
	private SpriteFont settingsLabel;
	private SpriteFont quitLabel;
	private PauseMenu pauseMenu;
	private ControlsOverlay controlsOverlay;
	private KeyLocker keyLocker = new KeyLocker();
	private final Key pauseKey = Key.P;
	private Thread gameLoopProcess;

	private Key showFPSKey = Key.G;
	private SpriteFont fpsDisplayLabel;
	private boolean showFPS = false;
	private int currentFPS;
	private boolean doPaint;

	// menu navigation
	private SpriteFont[] menuLabels;
	private int pauseMenuSelection = 0; // index into menuLabels
	private KeyLocker menuKeyLocker = new KeyLocker();
	// The JPanel and various important class instances are setup here
	public GamePanel() {
		super();
		this.setDoubleBuffered(true);

		// attaches Keyboard class's keyListener to this JPanel
		this.addKeyListener(Keyboard.getKeyListener());

		graphicsHandler = new GraphicsHandler();

		screenManager = new ScreenManager();

		// The pause menu labels
		pauseLabel = new SpriteFont("UNPAUSE", 0, 0, "Arial", 24, Color.white);
		pauseLabel.setOutlineColor(Color.black);
		pauseLabel.setOutlineThickness(2.0f);
		settingsLabel = new SpriteFont("Controls", pauseLabel.getX(), pauseLabel.getY() + 30, "Arial", 24, Color.white);
		settingsLabel.setOutlineColor(Color.black);
		settingsLabel.setOutlineThickness(2.0f);
		quitLabel = new SpriteFont("Quit to Menu", settingsLabel.getX(), settingsLabel.getY() + 30, "Arial", 24, Color.white);
		quitLabel.setOutlineColor(Color.black);
		quitLabel.setOutlineThickness(2.0f);
		fpsDisplayLabel = new SpriteFont("FPS", 4, 3, "Arial", 12, Color.black);

		// menu labels array for keyboard navigation (now includes the PAUSE title so it can be selected)
		menuLabels = new SpriteFont[] {pauseLabel, settingsLabel, quitLabel};

	// create encapsulated pause menu
	pauseMenu = new PauseMenu(pauseLabel, Arrays.asList(pauseLabel, settingsLabel, quitLabel));

		// controls overlay
		controlsOverlay = new ControlsOverlay();

		// set pause menu activation callback to open settings overlay when selected
		pauseMenu.setOnActivate(idx -> {
			switch (idx) {
				case 0: 
					unPauseSelected(); 
					break;
				case 1: 
					onControlsSelected(); 
					break;
				case 2: 
					onQuitSelected(); 
					break;
			}
		});

		//Center labels on resize
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				centerUiLabels();
			}
		});

		updateMenuHighlight();

		currentFPS = Config.TARGET_FPS;

		// this game loop code will run in a separate thread from the rest of the program
		// will continually update the game's logic and repaint the game's graphics
		GameLoop gameLoop = new GameLoop(this);
		gameLoopProcess = new Thread(gameLoop.getGameLoopProcess());
	}

	// this is called later after instantiation, and will initialize screenManager
	// this had to be done outside of the constructor because it needed to know the JPanel's width and height, which aren't available in the constructor
	public void setupGame() {
		setBackground(Colors.CORNFLOWER_BLUE);
		screenManager.initialize(new Rectangle(getX(), getY(), getWidth(), getHeight()));
			// center UI labels now that the panel has been sized
			centerUiLabels();
	}

	// this starts the timer (the game loop is started here)
	public void startGame() {
		gameLoopProcess.start();
	}

	public ScreenManager getScreenManager() {
		return screenManager;
	}

	public void setCurrentFPS(int currentFPS) {
		this.currentFPS = currentFPS;
	}

	public void setDoPaint(boolean doPaint) {
		this.doPaint = doPaint;
	}

	public void update() {
		updatePauseState();
		updateShowFPSState();

		if (!isGamePaused) {
			screenManager.update();
		}
	}

	private void updatePauseState() {
		if (Keyboard.isKeyDown(pauseKey) && !keyLocker.isKeyLocked(pauseKey)) {
			isGamePaused = !isGamePaused;
			keyLocker.lockKey(pauseKey);
			if (isGamePaused) {
				pauseMenuSelection = 0;
				updateMenuHighlight();
			}
		}

		if (Keyboard.isKeyUp(pauseKey)) {
			keyLocker.unlockKey(pauseKey);
		}

		//The pause menu selection controls
		if (isGamePaused) {
			//Move up in selection menu 
			if ((Keyboard.isKeyDown(Key.UP) || Keyboard.isKeyDown(Key.W)) && !menuKeyLocker.isKeyLocked(Key.UP)) {
				pauseMenuSelection = (pauseMenuSelection - 1 + menuLabels.length) % menuLabels.length;
				menuKeyLocker.lockKey(Key.UP);
				if (pauseMenu != null) pauseMenu.setSelection(pauseMenuSelection);
				updateMenuHighlight();
			}
			if (Keyboard.isKeyUp(Key.UP) && Keyboard.isKeyUp(Key.W)) {
				menuKeyLocker.unlockKey(Key.UP);
			}

			//Move down in selection menu
			if ((Keyboard.isKeyDown(Key.DOWN) || Keyboard.isKeyDown(Key.S)) && !menuKeyLocker.isKeyLocked(Key.DOWN)) {
				pauseMenuSelection = (pauseMenuSelection + 1) % menuLabels.length;
				menuKeyLocker.lockKey(Key.DOWN);
				if (pauseMenu != null) pauseMenu.setSelection(pauseMenuSelection);
				updateMenuHighlight();
			}
			if (Keyboard.isKeyUp(Key.DOWN) && Keyboard.isKeyUp(Key.S)) {
				menuKeyLocker.unlockKey(Key.DOWN);
			}

			//activateMenuSelections gets called in which calls other methods based on selection
			if ((Keyboard.isKeyDown(Key.ENTER) || Keyboard.isKeyDown(Key.SPACE)) && !menuKeyLocker.isKeyLocked(Key.ENTER)) {
				menuKeyLocker.lockKey(Key.ENTER);
				activateMenuSelection();
			}
			if (Keyboard.isKeyUp(Key.ENTER) && Keyboard.isKeyUp(Key.SPACE)) {
				menuKeyLocker.unlockKey(Key.ENTER);
			}
		}
	}

	private void centerUiLabels() {
		int sw = getWidth();
		int sh = getHeight();
		if (sw <= 0 || sh <= 0) return;

		FontMetrics fmPause = getFontMetrics(pauseLabel.getFont());
		int pauseW = fmPause.stringWidth(pauseLabel.getText());
		int pauseH = fmPause.getHeight();

		float pauseX = (sw - pauseW) / 2f;
		float pauseY = (sh - pauseH) / 2f;
		pauseLabel.setLocation(pauseX, pauseY);

		FontMetrics fmSettings = getFontMetrics(settingsLabel.getFont());
		int settingsW = fmSettings.stringWidth(settingsLabel.getText());
		float settingsX = (sw - settingsW) / 2f;
		float settingsY = pauseY + pauseH + 6;
		settingsLabel.setLocation(settingsX, settingsY);

		FontMetrics fmQuit = getFontMetrics(quitLabel.getFont());
		int quitW = fmQuit.stringWidth(quitLabel.getText());
		float quitX = (sw - quitW) / 2f;
		float quitY = settingsY + fmSettings.getHeight() + 6;
		quitLabel.setLocation(quitX, quitY);
	}

	private void updateMenuHighlight() {
		for (SpriteFont sf : menuLabels) {
			sf.setColor(Color.white);
		}
		if (pauseMenuSelection >= 0 && pauseMenuSelection < menuLabels.length) {
			menuLabels[pauseMenuSelection].setColor(Color.yellow);
		}
	}

	private void activateMenuSelection() {
		if (pauseMenuSelection == 0) {
			unPauseSelected();
		} else if (pauseMenuSelection == 1) { 
			onControlsSelected();
		} else if (pauseMenuSelection == 2) {
			onQuitSelected();
		}
	}

	private void unPauseSelected() { 
		System.out.println("Unpause selected");
		isGamePaused = false;
		if (controlsOverlay != null) controlsOverlay.setVisible(false);
		keyLocker.unlockKey(pauseKey);
		menuKeyLocker.unlockKey(Key.ENTER);
		menuKeyLocker.unlockKey(Key.UP);
		menuKeyLocker.unlockKey(Key.DOWN);
		this.requestFocusInWindow();
	}

	private void onControlsSelected() {
		//Opens settings so then you can see keybinds
		try {
			Screens.PlayLevelScreen pls = (Screens.PlayLevelScreen) screenManager.getCurrentScreen();
			if (pls != null) {
				controlsOverlay.populateFromPlayers(pls.getPlayer1(), pls.getPlayer2());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		controlsOverlay.setVisible(true);
		this.requestFocusInWindow();
		//This is to prevent re opening the menu immediately
		menuKeyLocker.lockKey(Key.ENTER);
	}

	private void onQuitSelected() {
		System.out.println("Quit selected");
		try {
			Object curr = screenManager.getCurrentScreen();
			if (curr instanceof ScreenCoordinator) {
				((ScreenCoordinator) curr).setGameState(GameState.MENU);
			} else if (curr instanceof Screens.PlayLevelScreen) {
				((Screens.PlayLevelScreen) curr).goBackToMenu();
			} else {
				System.err.println("Warning: Quit to Menu requested but current screen is neither ScreenCoordinator nor PlayLevelScreen.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		isGamePaused = false;
		if (controlsOverlay != null) controlsOverlay.setVisible(false);
		keyLocker.unlockKey(pauseKey);
		menuKeyLocker.unlockKey(Key.ENTER);
		menuKeyLocker.unlockKey(Key.UP);
		menuKeyLocker.unlockKey(Key.DOWN);
		this.requestFocusInWindow();
	}

	private void updateShowFPSState() {
		if (Keyboard.isKeyDown(showFPSKey) && !keyLocker.isKeyLocked(showFPSKey)) {
			showFPS = !showFPS;
			keyLocker.lockKey(showFPSKey);
		}

		if (Keyboard.isKeyUp(showFPSKey)) {
			keyLocker.unlockKey(showFPSKey);
		}

		fpsDisplayLabel.setText("FPS: " + currentFPS);
	}

	public void draw() {
		screenManager.draw(graphicsHandler);

		if (isGamePaused) {

			graphicsHandler.drawFilledRectangle(0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), new Color(0, 0, 0, 100));
			Graphics g = graphicsHandler.getGraphics();
			if (controlsOverlay != null && controlsOverlay.isVisible()) {
				controlsOverlay.center(ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), g);
				controlsOverlay.draw(graphicsHandler);
				controlsOverlay.handleInput();
			} else {
				if (pauseMenu != null && g != null) {
					pauseMenu.center(ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), g);
					pauseMenu.draw(graphicsHandler);
				} else {
					settingsLabel.draw(graphicsHandler);
					pauseLabel.draw(graphicsHandler);
					quitLabel.draw(graphicsHandler);
				}
			}
		}

		if (showFPS) {
			fpsDisplayLabel.draw(graphicsHandler);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (doPaint) {
			// every repaint call will schedule this method to be called
			// when called, it will setup the graphics handler and then call this class's draw method
			graphicsHandler.setGraphics((Graphics2D) g);
			draw();
		}
	}
}
