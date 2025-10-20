package Engine;

import SpriteFont.SpriteFont;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class PauseMenu {
    private SpriteFont titleLabel;
    private List<SpriteFont> items;
    private int selection = 0;

    private Color defaultColor = Color.white;
    private Color highlightColor = Color.yellow;

    private Consumer<Integer> onActivate;

    public PauseMenu(SpriteFont titleLabel, List<SpriteFont> items) {
        this.titleLabel = titleLabel;
        this.items = items;
    }

    public void setSelection(int sel) {
        if (items == null || items.isEmpty()) return;
        this.selection = Math.max(0, Math.min(items.size() - 1, sel));
        updateHighlight();
    }

    public int getSelection() { 
        return selection; 
    }
    
    public void updateHighlight() {
        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setColor(i == selection ? highlightColor : defaultColor);
        }
    }
    //This just centers the menu on the screen
    public void center(int width, int height, Graphics g) {
        if (width <= 0 || height <= 0 || g == null || titleLabel == null || items == null) return;
        FontMetrics fmTitle = g.getFontMetrics(titleLabel.getFont());
        int titleW = fmTitle.stringWidth(titleLabel.getText());
        int titleH = fmTitle.getHeight();

        float titleX = (width - titleW) / 2f;
        float titleY = (height - titleH) / 2f;
        titleLabel.setLocation(titleX, titleY);

        int gap = 6;
        int y = Math.round(titleY + titleH + gap);
        for (SpriteFont item : items) {
            FontMetrics fm = g.getFontMetrics(item.getFont());
            int w = fm.stringWidth(item.getText());
            float x = (width - w) / 2f;
            item.setLocation(x, y);
            y += fm.getHeight() + gap;
        }
    }

    public void draw(GraphicsHandler gh) {
        if (titleLabel != null) titleLabel.draw(gh);
        if (items == null) return;
        for (SpriteFont item : items) item.draw(gh);
    }
    //The menu navigation
    public void moveSelection(int delta) {
        if (items == null || items.isEmpty()) return;
        int n = items.size();
        selection = ((selection + delta) % n + n) % n;
        updateHighlight();
    }

    public void setOnActivate(Consumer<Integer> onActivate) { this.onActivate = onActivate; }
    public void activate() { if (onActivate != null) onActivate.accept(selection); }

    public SpriteFont getTitleLabel() { return titleLabel; }
    public List<SpriteFont> getItems() { return items; }

    public void setDefaultColor(Color c) { defaultColor = c; updateHighlight(); }
    public void setHighlightColor(Color c) { highlightColor = c; updateHighlight(); }
}
