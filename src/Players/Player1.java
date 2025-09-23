package Players;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.MapEntity;
import Level.PlayerState;
import Utils.AirGroundState;
import Utils.Direction;

import java.util.HashMap;

// Player1 - Uses WASD controls for movement
public class Player1 extends MapEntity {
    // Movement values
    protected float walkSpeed = 2.3f;
    protected float gravity = 0.5f;
    protected float jumpHeight = 14.5f;
    protected float jumpDegrade = 0.5f;
    protected float terminalVelocityY = 6f;
    protected float momentumYIncrease = 0.5f;

    // Movement tracking
    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    // State tracking
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;

    // Input handling
    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.W;
    protected Key MOVE_LEFT_KEY = Key.A;
    protected Key MOVE_RIGHT_KEY = Key.D;

    public Player1(float x, float y, String characterSpritePath, int spriteWidth, int spriteHeight) {
        super(x, y, new SpriteSheet(ImageLoader.load(characterSpritePath), spriteWidth, spriteHeight), "STAND_RIGHT");
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
    }
    
    public Player1(float x, float y) {
        this(x, y, "Cat.png", 24, 24);
    }

    public void update() {
        moveAmountX = 0;
        moveAmountY = 0;

        applyGravity();

        // Update player state
        do {
            previousPlayerState = playerState;
            handlePlayerState();
        } while (previousPlayerState != playerState);

        previousAirGroundState = airGroundState;

        // Move player with collision detection
        lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
        lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

        handlePlayerAnimation();
        updateLockedKeys();

        // Update animation
        super.update();
    }

    protected void applyGravity() {
        moveAmountY += gravity + momentumY;
    }

    protected void handlePlayerState() {
        switch (playerState) {
            case STANDING:
                playerStanding();
                break;
            case WALKING:
                playerWalking();
                break;
            case JUMPING:
                playerJumping();
                break;
            case CROUCHING:
                // Crouching not implemented for simplified player
                break;
        }
    }

    protected void playerStanding() {
        // If walk left or walk right key is pressed, enter WALKING state
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        }
        // If jump key is pressed, enter JUMPING state
        else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerWalking() {
        // Move left
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        }
        // Move right
        else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        } else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        // Jump while walking
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerJumping() {
        // Setup jump if on ground
        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {
            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }
        }
        // Continue jump in air
        else if (airGroundState == AirGroundState.AIR) {
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            // Allow movement while in air
            if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
                moveAmountX -= walkSpeed;
            } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
                moveAmountX += walkSpeed;
            }

            // Increase falling momentum
            if (moveAmountY > 0) {
                increaseMomentum();
            }
        }
        // Land on ground
        else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }
    }

    protected void increaseMomentum() {
        momentumY += momentumYIncrease;
        if (momentumY > terminalVelocityY) {
            momentumY = terminalVelocityY;
        }
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(JUMP_KEY)) {
            keyLocker.unlockKey(JUMP_KEY);
        }
    }

    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING) {
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        } else if (playerState == PlayerState.WALKING) {
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        } else if (playerState == PlayerState.JUMPING) {
            if (lastAmountMovedY <= 0) {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            } else {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT";
            }
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // Handle ground collision
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        }
        // Handle ceiling collision
        else if (direction == Direction.UP) {
            if (hasCollided) {
                jumpForce = 0;
            }
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {
            {
                put("STAND_RIGHT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(0, 0))
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("STAND_LEFT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(0, 0))
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("WALK_RIGHT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build(),
                        new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build(),
                        new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build(),
                        new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("WALK_LEFT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(1, 0), 14)
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build(),
                        new FrameBuilder(spriteSheet.getSprite(1, 1), 14)
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build(),
                        new FrameBuilder(spriteSheet.getSprite(1, 2), 14)
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build(),
                        new FrameBuilder(spriteSheet.getSprite(1, 3), 14)
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("JUMP_RIGHT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(2, 0))
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("JUMP_LEFT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(2, 0))
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("FALL_RIGHT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(3, 0))
                                .withScale(3)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });

                put("FALL_LEFT", new Frame[] {
                        new FrameBuilder(spriteSheet.getSprite(3, 0))
                                .withScale(3)
                                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                                .withBounds(8, 9, 8, 9)
                                .build()
                });
            }
        };
    }
}
