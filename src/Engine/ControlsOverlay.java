package Engine;

import Players.Player1;
import Players.Player2;
import GameObject.Rectangle;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ControlsOverlay {
    private boolean visible = false;

    private SpriteFont header1;
    private SpriteFont header2;
    private List<SpriteFont> leftActionLabels = new ArrayList<>();
    private List<SpriteFont> leftValueLabels = new ArrayList<>();
    private List<SpriteFont> rightActionLabels = new ArrayList<>();
    private List<SpriteFont> rightValueLabels = new ArrayList<>();
    private SpriteFont footer;
    // Bounds of player to draw labels under
    private Rectangle p1BoundsForLabels = null;
    private Rectangle p2BoundsForLabels = null;

    private KeyLocker closeLocker = new KeyLocker();

    public ControlsOverlay() {
        header1 = new SpriteFont("Player 1", 0, 0, "Arial", 20, Color.white);
        header2 = new SpriteFont("Player 2", 0, 0, "Arial", 20, Color.white);
        footer = new SpriteFont("Press P or ESC to return", 0, 0, "Arial", 14, Color.white);
        //The two headers and footer
        header1.setOutlineColor(Color.black);
        header1.setOutlineThickness(2f);
        header2.setOutlineColor(Color.black);
        header2.setOutlineThickness(2f);
        footer.setOutlineColor(Color.black);
        footer.setOutlineThickness(2f);
    }

    public void setVisible(boolean v) { 
        visible = v; 
    }
    public boolean isVisible() { 
        return visible; 
    }

    public void populateFromPlayers(Player1 p1, Player2 p2) {
    leftActionLabels.clear();
    leftValueLabels.clear();
    rightActionLabels.clear();
    rightValueLabels.clear();

    // Array of actions in the game
    String[] actions = new String[] { "Move Left", "Move Right", "Jump", "Punch", "Ranged Attack" };

        for (String a : actions) {
            SpriteFont la = new SpriteFont(a, 0, 0, "Arial", 16, Color.white);
            SpriteFont lv = new SpriteFont("-", 0, 0, "Arial", 16, Color.yellow);
            SpriteFont ra = new SpriteFont(a, 0, 0, "Arial", 16, Color.white);
            SpriteFont rv = new SpriteFont("-", 0, 0, "Arial", 16, Color.yellow);
            // outline value labels for contrast
            lv.setOutlineColor(Color.black);
            lv.setOutlineThickness(2f);
            rv.setOutlineColor(Color.black);
            rv.setOutlineThickness(2f);
            leftActionLabels.add(la);
            leftValueLabels.add(lv);
            rightActionLabels.add(ra);
            rightValueLabels.add(rv);
        }

        if (p1 != null) {
            Player1 p1c = (Player1) p1;
            leftValueLabels.get(0).setText(keyToString(p1c.getMoveLeftKey()));
            leftValueLabels.get(1).setText(keyToString(p1c.getMoveRightKey()));
            leftValueLabels.get(2).setText(keyToString(p1c.getJumpKey()));
            leftValueLabels.get(3).setText(keyToString(p1c.getPunchKey()));
            leftValueLabels.get(4).setText(keyToString(p1c.getFireballKey()));
            try { 
                p1BoundsForLabels = p1.getCalibratedBounds(); 
            } catch (Exception ex) { 
                p1BoundsForLabels = null; 
            }
        } else {
            p1BoundsForLabels = null;
        }
        if (p2 != null) {
            Player2 p2c = (Player2) p2;
            rightValueLabels.get(0).setText(keyToString(p2c.getMoveLeftKey()));
            rightValueLabels.get(1).setText(keyToString(p2c.getMoveRightKey()));
            rightValueLabels.get(2).setText(keyToString(p2c.getJumpKey()));
            rightValueLabels.get(3).setText(keyToString(p2c.getPunchKey()));
            rightValueLabels.get(4).setText(keyToString(p2c.getFireballKey()));
            try { 
                p2BoundsForLabels = p2.getCalibratedBounds(); 
            } catch (Exception ex) { 
                p2BoundsForLabels = null; 
            }
        } else {
            p2BoundsForLabels = null;
        }
    }

    private String keyToString(Key k) {
        if (k == null) return "-";
        return k.toString();
    }

    public void center(int width, int height, Graphics g) {
        if (g == null) return;

        int boxW = Math.min(600, width - 40);
        int boxH = Math.min(300, height - 80);
        int boxX = (width - boxW) / 2;
        int boxY = (height - boxH) / 2;

        //puts the headers in the right place
        FontMetrics fh1 = g.getFontMetrics(header1.getFont());
        int h1w = fh1.stringWidth(header1.getText());
        header1.setLocation(boxX + 60 - h1w/2f, boxY + 20);

        FontMetrics fh2 = g.getFontMetrics(header2.getFont());
        int h2w = fh2.stringWidth(header2.getText());
        header2.setLocation(boxX + boxW - 60 - h2w/2f, boxY + 20);

        // Row positions
        int gap = 8;
        int startY = boxY + 60;
        for (int i = 0; i < leftActionLabels.size(); i++) {
            SpriteFont label = leftActionLabels.get(i);
            SpriteFont val = leftValueLabels.get(i);
            FontMetrics fam = g.getFontMetrics(label.getFont());
            label.setLocation(boxX + 40, startY + i * (fam.getHeight() + gap));
            val.setLocation(boxX + 160, startY + i * (fam.getHeight() + gap));

            SpriteFont rlabel = rightActionLabels.get(i);
            SpriteFont rval = rightValueLabels.get(i);
            FontMetrics fr = g.getFontMetrics(rlabel.getFont());
            rlabel.setLocation(boxX + boxW - 200, startY + i * (fr.getHeight() + gap));
            rval.setLocation(boxX + boxW - 80, startY + i * (fr.getHeight() + gap));
        }

        // footer
        FontMetrics ff = g.getFontMetrics(footer.getFont());
        int fw = ff.stringWidth(footer.getText());
        footer.setLocation(boxX + (boxW - fw) / 2f, boxY + boxH - 30);
    }

    public void draw(GraphicsHandler gh) {
        if (!visible) return;
        //The dim background
        Graphics g = gh.getGraphics();
        g.setColor(new Color(0,0,0,140));
        g.fillRect(0,0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight());

        center(ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), g);

        //draws the headers
        header1.draw(gh);
        header2.draw(gh);
            // Checks whether the labels exist
            if (leftActionLabels.isEmpty()) {
                String[] actions = new String[] { "Move Left", "Move Right", "Jump", "Punch", "Ranged Attack" };
                //If cant read keybind for player a - will be put in
                String[] leftVals = new String[] {"-", "-", "-", "-", "-"};
                String[] rightVals = new String[] {"-", "-", "-", "-", "-"};
                try {
                    Player1 tp1 = new Player1(0,0);
                    Player2 tp2 = new Player2(0,0);

                    leftVals[0] = keyToString(tp1.getMoveLeftKey());
                    leftVals[1] = keyToString(tp1.getMoveRightKey());
                    leftVals[2] = keyToString(tp1.getJumpKey());
                    leftVals[3] = keyToString(tp1.getPunchKey());
                    leftVals[4] = keyToString(tp1.getFireballKey());
                    rightVals[0] = keyToString(tp2.getMoveLeftKey());
                    rightVals[1] = keyToString(tp2.getMoveRightKey());
                    rightVals[2] = keyToString(tp2.getJumpKey());
                    rightVals[3] = keyToString(tp2.getPunchKey());
                    rightVals[4] = keyToString(tp2.getFireballKey());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                for (int i = 0; i < actions.length; i++) {
                    String a = actions[i];
                    SpriteFont la = new SpriteFont(a, 0, 0, "Arial", 16, Color.white);
                    SpriteFont lv = new SpriteFont(leftVals[i], 0, 0, "Arial", 16, Color.yellow);
                    SpriteFont ra = new SpriteFont(a, 0, 0, "Arial", 16, Color.white);
                    SpriteFont rv = new SpriteFont(rightVals[i], 0, 0, "Arial", 16, Color.yellow);
                    //Adds the outlines to labels
                    la.setOutlineColor(Color.black);
                    la.setOutlineThickness(1.5f);
                    ra.setOutlineColor(Color.black);
                    ra.setOutlineThickness(1.5f);
                    lv.setOutlineColor(Color.black);
                    lv.setOutlineThickness(2f);
                    rv.setOutlineColor(Color.black);
                    rv.setOutlineThickness(2f);
                    leftActionLabels.add(la);
                    leftValueLabels.add(lv);
                    rightActionLabels.add(ra);
                    rightValueLabels.add(rv);
                }
    }
    //Draws the labels that go in the center
    for (SpriteFont f : leftActionLabels) {
        f.draw(gh);
    }
    for (SpriteFont f : leftValueLabels) {
        f.draw(gh);
    }
    for (SpriteFont f : rightActionLabels) {
        f.draw(gh);
    }
    for (SpriteFont f : rightValueLabels) {
        f.draw(gh);
    }
    footer.draw(gh);

    if (p1BoundsForLabels != null) {
        drawLabelsUnderPlayer(gh, p1BoundsForLabels, leftActionLabels, leftValueLabels);
    }
    if (p2BoundsForLabels != null) {
        drawLabelsUnderPlayer(gh, p2BoundsForLabels, rightActionLabels, rightValueLabels);
    }
}

    private void drawLabelsUnderPlayer(GraphicsHandler gh, Rectangle bounds, java.util.List<SpriteFont> actions, java.util.List<SpriteFont> values) {
        if (bounds == null) return;
        Graphics g = gh.getGraphics();
        int centerX = Math.round(bounds.getX() + bounds.getWidth() / 2f);
        int startY = Math.round(bounds.getY() + bounds.getHeight()) + 6; // just below feet

        FontMetrics fm = g.getFontMetrics(actions.get(0).getFont());
        int lineHeight = fm.getHeight();
        int leftColX = centerX - 48; // offset left column
        int rightColX = centerX + 8; // offset for key column

        for (int i = 0; i < actions.size(); i++) {
            SpriteFont a = actions.get(i);
            SpriteFont v = values.get(i);
            a.setLocation(leftColX, startY + i * (lineHeight + 2));
            v.setLocation(rightColX, startY + i * (lineHeight + 2));
            a.draw(gh);
            v.draw(gh);
        }
    }

    public void handleInput() {
        //Getting out of settings 
        if ((Keyboard.isKeyDown(Key.P) || Keyboard.isKeyDown(Key.ESC) || Keyboard.isKeyDown(Key.ENTER) || Keyboard.isKeyDown(Key.SPACE)) && !closeLocker.isKeyLocked(Key.P)) {
            setVisible(false);
            closeLocker.lockKey(Key.P);
        }
        if (Keyboard.isKeyUp(Key.P) && Keyboard.isKeyUp(Key.ESC) && Keyboard.isKeyUp(Key.ENTER) && Keyboard.isKeyUp(Key.SPACE)) {
            closeLocker.unlockKey(Key.P);
        }
    }
}
