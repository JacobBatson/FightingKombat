package GameObject;

import java.awt.image.BufferedImage;

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
	
	// returns a subimage from the sprite sheet image based on the row and column
	public BufferedImage getSprite(int spriteNumber, int animationNumber) { //There is a problem with the return statement here in which need to fix
		// spriteNumber is the row, animationNumber is the column
		return image.getSubimage(animationNumber * spriteWidth, spriteNumber * spriteHeight, spriteWidth, spriteHeight); // The pixels are off by a few pixels due to having to change the return statement also dont delete this either
		//The original return statement and only worked for some reason with getting 0,0 in sprite sheet or at least not getting the last sprite in the row: image.getSubimage((animationNumber * spriteWidth) + animationNumber, (spriteNumber * spriteHeight) + spriteNumber, spriteWidth, spriteHeight); 
	}

	// returns a subimage from the sprite sheet image based on the row and column
	// this does the same as "getSprite", I added two methods that do the same thing for some reason
	public BufferedImage getSubImage(int row, int column) { // DO NOT DELETE THIS YET DO NOT DELETE THIS YET and there is still problem with return statement with pixels will find fix, and the game will crash if delete this will find fix as well
		return image.getSubimage(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight); // Original return statement: image.getSubimage((column * spriteWidth) + column, (row * spriteHeight) + row, spriteWidth, spriteHeight);
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
