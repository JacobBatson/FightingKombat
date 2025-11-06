package Maps;

import Level.*;
import Tilesets.CommonTileset;
import java.util.ArrayList;

public class Map4 extends Map {

    public Map4() {
        super("Map4.txt", new CommonTileset());
    }

    @Override
    protected ArrayList<Enemy> loadEnemies() {
        return new ArrayList<>();
    }

    @Override
    protected ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        return new ArrayList<>();
    }

    @Override
    protected ArrayList<NPC> loadNPCs() {
        return new ArrayList<>();
    }
}