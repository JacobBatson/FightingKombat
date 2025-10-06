package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;

// This class represents a "common" tileset of standard tiles defined in the CommonTileset.png file
public class CommonTileset extends Tileset {

    public CommonTileset() {
        super(ImageLoader.load("MapTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();

        // Back ground block
        Frame skyhellFrame = new FrameBuilder(getSubImage(0, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder skyhellTile = new MapTileBuilder(skyhellFrame);

        mapTiles.add(skyhellTile);

        //lava block 1
        Frame Lava1Frame = new FrameBuilder(getSubImage(0, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder Lava1Tile = new MapTileBuilder(Lava1Frame);

        mapTiles.add(Lava1Tile);

        //lava block 2
        Frame Lava2Frame = new FrameBuilder(getSubImage(0, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder Lava2Tile = new MapTileBuilder(Lava2Frame);

        mapTiles.add(Lava2Tile);

        //lava block waterfall bottom
        Frame Lava3Frame = new FrameBuilder(getSubImage(0, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder Lava3Tile = new MapTileBuilder(Lava3Frame);

        mapTiles.add(Lava3Tile);

        //lava block waterfall top1
        Frame Waterfall1Frame = new FrameBuilder(getSubImage(0, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder Waterfall1Tile = new MapTileBuilder(Waterfall1Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(Waterfall1Tile);

        //lava block waterfall top2
        Frame Waterfall2Frame = new FrameBuilder(getSubImage(0, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder Waterfall2Tile = new MapTileBuilder(Waterfall2Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(Waterfall2Tile);

        //platform Left corner hell
        Frame LefthellplatFrame = new FrameBuilder(getSubImage(1, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder LefthellplatTile = new MapTileBuilder(LefthellplatFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(LefthellplatTile);

        //platform middle 1
        Frame hellplat1Frame = new FrameBuilder(getSubImage(1, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder hellplat1Tile = new MapTileBuilder(hellplat1Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(hellplat1Tile);

        //platform middle 2
        Frame hellplat2Frame = new FrameBuilder(getSubImage(1, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder hellplat2Tile = new MapTileBuilder(hellplat2Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(hellplat2Tile);

        //platform middle 3
        Frame hellplat3Frame = new FrameBuilder(getSubImage(1, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder hellplat3Tile = new MapTileBuilder(hellplat3Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(hellplat3Tile);

        //platform Right corner hell
        Frame RighthellplatFrame = new FrameBuilder(getSubImage(1, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder RighthellplatTile = new MapTileBuilder(RighthellplatFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(RighthellplatTile);

        //Floating platform small
        Frame SmallplatFrame = new FrameBuilder(getSubImage(1, 5))
               .withScale(tileScale)
               .withBounds(0, 6, 16, 4)
               .build();


       MapTileBuilder SmallplatTile = new MapTileBuilder(SmallplatFrame)
               .withTileType(TileType.JUMP_THROUGH_PLATFORM);


       mapTiles.add(SmallplatTile);

       //Tourch head 1
       Frame Tourch1Frame = new FrameBuilder(getSubImage(2, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder Tourch1Tile = new MapTileBuilder(Tourch1Frame);

        mapTiles.add(Tourch1Tile);

        //Tourch head 2
        Frame Tourch2Frame = new FrameBuilder(getSubImage(2, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder Tourch2Tile = new MapTileBuilder(Tourch2Frame);

        mapTiles.add(Tourch2Tile);

        //Tourch bottom 1
        Frame Tourch3Frame = new FrameBuilder(getSubImage(2, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder Tourch3Tile = new MapTileBuilder(Tourch3Frame);

        mapTiles.add(Tourch3Tile);

        //Tourch bottom 2
        Frame Tourch4Frame = new FrameBuilder(getSubImage(2, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder Tourch4Tile = new MapTileBuilder(Tourch4Frame);

        mapTiles.add(Tourch4Tile);

        //platform left
        Frame platleftFrame = new FrameBuilder(getSubImage(2, 4))
               .withScale(tileScale)
               .withBounds(0, 6, 16, 4)
               .build();


       MapTileBuilder platleftTile = new MapTileBuilder(platleftFrame)
               .withTileType(TileType.JUMP_THROUGH_PLATFORM);


       mapTiles.add(platleftTile);

       //platform right
       Frame platrightFrame = new FrameBuilder(getSubImage(2, 5))
               .withScale(tileScale)
               .withBounds(0, 6, 16, 4)
               .build();


       MapTileBuilder platrightTile = new MapTileBuilder(platrightFrame)
               .withTileType(TileType.JUMP_THROUGH_PLATFORM);


       mapTiles.add(platrightTile);







        return mapTiles;
    }
}
