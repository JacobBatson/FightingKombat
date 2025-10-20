package Maps;


    import Level.*;
    import Tilesets.CommonTileset;
    import java.util.ArrayList;
    
    public class Map2 extends Map {
    
        public Map2() {
            super("map4.txt", new CommonTileset());
            // default player start position left as (0,0) or set via getMapTile when needed
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
    
    

