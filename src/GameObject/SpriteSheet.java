package GameObject;

import java.awt.image.BufferedImage;
// ...existing code...
import Builders.FrameBuilder;

// This class is for reading in a SpriteSheet (collection of images laid out in a specific way)
// As long as each graphic on the sheet is the same size, it can parse it into sub images
public class SpriteSheet {
	protected BufferedImage image;
	protected int spriteWidth;
	protected int spriteHeight;
	protected int rowLength;
	protected int columnLength;

	public SpriteSheet(BufferedImage image, int spriteWidth, int spriteHeight) {
		this.image = image;
		this.spriteWidth = spriteWidth;
		this.spriteHeight = spriteHeight;
		this.rowLength = image.getHeight() / spriteHeight;
		this.columnLength = image.getWidth() / spriteWidth;
	}

	/*
	 * image.getSubimage((animationNumber * spriteWidth) + animationNumber, (spriteNumber * spriteHeight) + spriteNumber, spriteWidth, spriteHeight);
	 * image.getSubimage(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
	 */
	
	// returns a subimage from the sprite sheet image based on the row and column
	public BufferedImage getSprite(int spriteNumber, int animationNumber) { 
		int x1 = animationNumber * spriteWidth;
		int y1 = spriteNumber * spriteHeight;
		
		int x3 = (animationNumber * spriteWidth) + animationNumber;
		int y3 = (spriteNumber * spriteHeight) + spriteNumber;
		
		try {
			if (x3 + spriteWidth <= image.getWidth() && y3 + spriteHeight <= image.getHeight()) {
				return image.getSubimage(x3, y3, spriteWidth, spriteHeight);
			} else {
				return image.getSubimage(x1, y1, spriteWidth, spriteHeight);
			}
		} catch (Exception e) {
			return image.getSubimage(0, 0, spriteWidth, spriteHeight);
		} 
		
	}
	
	// Helper: create sequential frames that advance across columns and wrap to the next row when needed
	// This version walks columns and when it reaches the end of a row it moves to the next row at column 0.
	public static Frame[] createSequentialFrames(SpriteSheet spriteSheet, int column, int row, int count, int delay, boolean flip) {
        final int out_of_bounds = 4; //The max bounds for current sprite sheet this could change if need to create new sprite sheet
        final int y_axis_bounds = 30; //Proper y-axis bounds
        final int x_axis_bounds = 16; //Proper x-axis bounds


        Frame[] frames = new Frame[count];
        int x = column, y = row; // Row and Column in sprite sheet
        for (int i = 0; i < count; i++) {
            FrameBuilder builder = new FrameBuilder(spriteSheet.getSprite(x, y), delay)
                    .withScale(2) // 2 is the proper scale factor for 64x64
                    .withBounds(x_axis_bounds, y_axis_bounds, 16, 18); //The 16 and 18 are width and height of collision bounds
            if (flip)  {
               builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
            }
            frames[i] = builder.build();
            y++;
            if (y > out_of_bounds) {
                y = 0;
                x++;
            }
        }
        return frames;
    }



	/*
	 * image.getSubimage((column * spriteWidth) + column, (row * spriteHeight) + row, spriteWidth, spriteHeight);
	 * image.getSubimage(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
	 */


	// returns a subimage from the sprite sheet image based on the row and column
	// this does the same as "getSprite", I added two methods that do the same thing for some reason
	public BufferedImage getSubImage(int row, int column) { // It is now working properly had to add try and catch block and if else statements

		int x1 = column * spriteWidth;
		int y1 = row * spriteHeight;

		int x3 = (column * spriteWidth) + column;
		int y3 = (row * spriteHeight) + row;
		
		try {
			if (x3 + spriteWidth <= image.getWidth() && y3 + spriteHeight <= image.getHeight()) {
				return image.getSubimage(x3, y3, spriteWidth, spriteHeight);
			} else {
				return image.getSubimage(x1, y1, spriteWidth, spriteHeight);
			}
		} catch (Exception e) {
			return image.getSubimage(0, 0, spriteWidth, spriteHeight);
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getSpriteWidth() {
		return spriteWidth;
	}

	public int getSpriteHeight() {
		return spriteHeight;
	}
}
