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

//Water map tiles

// Water background block 
        Frame skywaterFrame = new FrameBuilder(getSubImage(3, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder skywaterTile = new MapTileBuilder(skywaterFrame);

        mapTiles.add(skywaterTile);

        //Water block 1
        Frame Water1Frame = new FrameBuilder(getSubImage(3, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder Water1Tile = new MapTileBuilder(Water1Frame);

        mapTiles.add(Water1Tile);

        //Water block 2
        Frame Water2Frame = new FrameBuilder(getSubImage(3, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder Water2Tile = new MapTileBuilder(Water2Frame);

        mapTiles.add(Water2Tile);

        //Water block waterfall bottom
        Frame Water3Frame = new FrameBuilder(getSubImage(3, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder Water3Tile = new MapTileBuilder(Water3Frame);

        mapTiles.add(Water3Tile);

        //platform left water
        Frame platleftWaterFrame = new FrameBuilder(getSubImage(3, 4))
               .withScale(tileScale)
               .withBounds(0, 6, 16, 4)
               .build();


       MapTileBuilder platleftWaterTile = new MapTileBuilder(platleftWaterFrame)
               .withTileType(TileType.JUMP_THROUGH_PLATFORM);


       mapTiles.add(platleftWaterTile);

       //platform right water
       Frame platrightWaterFrame = new FrameBuilder(getSubImage(3, 5))
               .withScale(tileScale)
               .withBounds(0, 6, 16, 4)
               .build();


       MapTileBuilder platrightWaterTile = new MapTileBuilder(platrightWaterFrame)
               .withTileType(TileType.JUMP_THROUGH_PLATFORM);


       mapTiles.add(platrightWaterTile);


 //platform Left corner Water
        Frame LeftwaterplatFrame = new FrameBuilder(getSubImage(4, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder LeftwaterplatTile = new MapTileBuilder(LeftwaterplatFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(LeftwaterplatTile);


//platform middle water 1
        Frame Waterplat1Frame = new FrameBuilder(getSubImage(4, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder Waterplat1Tile = new MapTileBuilder(Waterplat1Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(Waterplat1Tile);


 //platform middle 2
        Frame Waterplat2Frame = new FrameBuilder(getSubImage(4, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder Waterplat2Tile = new MapTileBuilder(Waterplat2Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(Waterplat2Tile);


//platform middle 3
        Frame Waterplat3Frame = new FrameBuilder(getSubImage(4, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder Waterplat3Tile = new MapTileBuilder(Waterplat3Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(Waterplat3Tile);


//platform Right corner water
        Frame RightwaterplatFrame = new FrameBuilder(getSubImage(4, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder RightwaterplatTile = new MapTileBuilder(RightwaterplatFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(RightwaterplatTile);

//Floating platform small water
        Frame SmallplatwaterFrame = new FrameBuilder(getSubImage(4, 5))
               .withScale(tileScale)
               .withBounds(0, 6, 16, 4)
               .build();


       MapTileBuilder SmallplatwaterTile = new MapTileBuilder(SmallplatwaterFrame)
               .withTileType(TileType.JUMP_THROUGH_PLATFORM);


       mapTiles.add(SmallplatwaterTile);

       //Seaweed 1
       Frame Seaweed1Frame = new FrameBuilder(getSubImage(5, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder Seaweed1Tile = new MapTileBuilder(Seaweed1Frame);

        mapTiles.add(Seaweed1Tile);

        //Seaweed 2
        Frame Seaweed2Frame = new FrameBuilder(getSubImage(5, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder Seaweed2Tile = new MapTileBuilder(Seaweed2Frame);

        mapTiles.add(Seaweed2Tile);

        //Coral bottom 1
        Frame Coral1Frame = new FrameBuilder(getSubImage(5, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder Coral1Tile = new MapTileBuilder(Coral1Frame);

        mapTiles.add(Coral1Tile);

        //Coral bottom 2
        Frame Coral2Frame = new FrameBuilder(getSubImage(5, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder Coral2Tile = new MapTileBuilder(Coral2Frame);

        mapTiles.add(Coral2Tile);


        //Bubbles bottom 1
        Frame Bubbles1Frame = new FrameBuilder(getSubImage(5, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder Bubbles1Tile = new MapTileBuilder(Bubbles1Frame);

        mapTiles.add(Bubbles1Tile);

        //Bubbles bottom 2
        Frame Bubbles2Frame = new FrameBuilder(getSubImage(5, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder Bubbles2Tile = new MapTileBuilder(Bubbles2Frame);

        mapTiles.add(Bubbles2Tile);


        return mapTiles;



    }
}
