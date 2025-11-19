package Players;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.MusicManager;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.MapEntity;
import Enemies.Fireball;
import Enemies.WaterShot;
import Enemies.RockShot;
import Utils.Point;
import Level.PlayerState;
import Utils.AirGroundState;
import Utils.Direction;
import java.util.HashMap;
import GameObject.Rectangle;

// Player1 - Uses WASD controls for movement
public class Player1 extends MapEntity {
    protected Key FIREBALL_KEY = Key.E;
    protected java.util.List<Fireball> fireballs = new java.util.ArrayList<>();
    protected Key PUNCH_KEY = Key.F;
    private static final int HEART_HP = 100;
    private int maxHearts = 3;
    private int hearts = maxHearts;
    private int heartHP = HEART_HP;
    private int invulnFrames = 0;

    // Freeze (can't move) state applied by water shots (frames)
    private boolean isFrozen = false;
    private int frozenTimer = 0; // frames remaining frozen
    // Invincibility after respawn
    private boolean isInvincible = false;
    private int invincibleTimer = 0;
    private int invincibleBlinkTimer = 0;

    private int damageDealt = 0;
    private int maxDamage = 50;
    private boolean canUseSuperMove = false;

    // Rock sprite special protection
    private int rockProtectionHitsRemaining = 0;

    protected int punchDuration = 0;
    protected final int MAX_PUNCH_DURATION = 20;
    protected PlayerState previousNonPunchState = PlayerState.STANDING;

    protected float walkSpeed = 4.0f;
    protected float gravity = 0.5f;
    protected float jumpHeight = 14.5f;
    protected float jumpDegrade = 0.5f;
    protected float terminalVelocityY = 6f;
    protected float momentumYIncrease = 0.5f;

    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;

    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.W;
    protected Key MOVE_LEFT_KEY = Key.A;
    protected Key MOVE_RIGHT_KEY = Key.D;

    // Flamethrower special state (Fire character only)
    private boolean flamethrowerActive = false;
    private int flamethrowerTimer = 0; // frames remaining
    private final int FLAMETHROWER_DURATION_FRAMES = 1 * 60; // 1 second
    private final int FLAMETHROWER_SPAWN_INTERVAL = 4;
    private int flamethrowerSpawnCooldown = 0;
    
    private String characterSpritePathUsed;

    public Player1(float x, float y, String characterSpritePath, int spriteWidth, int spriteHeight) {
        super(x, y, new SpriteSheet(ImageLoader.load(characterSpritePath), spriteWidth, spriteHeight), "STAND_RIGHT");
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        this.characterSpritePathUsed = characterSpritePath;
    }

    public Player1(float x, float y) {
        this(x, y, "Fire_Sprite.png", 64, 64);
    }

    public void update() {
        moveAmountX = 0;
        moveAmountY = 0;
        applyGravity();

        // Handle frozen state: decrement timer and skip movement/state changes while frozen
        if (frozenTimer > 0) {
            frozenTimer--;
            if (frozenTimer <= 0) {
                isFrozen = false;
                frozenTimer = 0;
            } else {
                // still frozen: skip player state handling so input won't move the player
            }
        }

        if (!isFrozen) {
            do {
                previousPlayerState = playerState;
                handlePlayerState();
            } while (previousPlayerState != playerState);
        }

        previousAirGroundState = airGroundState;

        lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
        lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

        handlePlayerAnimation();
        updateLockedKeys();

        // Projectile firing / Super move activation
        if (Keyboard.isKeyDown(FIREBALL_KEY) && !keyLocker.isKeyLocked(FIREBALL_KEY) && canUseSuperMove) {
            keyLocker.lockKey(FIREBALL_KEY);
            useSuperMove(); // Reset the damage bar after using super move

            if (isFireSkin()) {
                flamethrowerActive = true;
                flamethrowerTimer = FLAMETHROWER_DURATION_FRAMES;
                flamethrowerSpawnCooldown = 0;
                MusicManager.getInstance().playSoundEffect("Resources/Fahh Sound Effect.wav");
            } else {
                float fbSpeed = 4.0f;
                int fbFrames = 60;
                Utils.Point offset = getFireballSpawnOffset();
                float fbX = this.x + (facingDirection == Direction.RIGHT ? 50 : 50);
                float fbY = this.y + offset.y;
                float speed = (facingDirection == Direction.RIGHT) ? fbSpeed : -fbSpeed;

                Fireball shot;
                if (isWaterSkin()) {
                    shot = new WaterShot(new Point(fbX, fbY), speed, fbFrames);
                } else if (isRockSkin()) {
                    shot = new RockShot(new Point(fbX, fbY), speed, fbFrames);
                } else {
                    shot = new Fireball(new Point(fbX, fbY), speed, fbFrames);
                }

                shot.setMap(this.map); // attach to same map for camera offset
                fireballs.add(shot);
            }
        }

        // Flamethrower logic:
        if (flamethrowerActive) {
            if (flamethrowerTimer > 0) {
                flamethrowerSpawnCooldown--;
                if (flamethrowerSpawnCooldown <= 0) {
                    float fbSpeed = 5.0f;
                    int fbFrames = 40;
                    Utils.Point offset = getFireballSpawnOffset();
                    float fbX = this.x + (facingDirection == Direction.RIGHT ? 50 : 50);
                    float fbY = this.y + offset.y;
                    float speed = (facingDirection == Direction.RIGHT) ? fbSpeed : -fbSpeed;
                    int flamethrowerDamage = 2;
                    Fireball shot = new Fireball(new Point(fbX, fbY), speed, fbFrames, flamethrowerDamage);
                    shot.setMap(this.map);
                    fireballs.add(shot);
                    flamethrowerSpawnCooldown = FLAMETHROWER_SPAWN_INTERVAL;
                }
                flamethrowerTimer--;
            } else {
                flamethrowerActive = false;
            }
        }
        if (Keyboard.isKeyUp(FIREBALL_KEY)) {
            keyLocker.unlockKey(FIREBALL_KEY);
        }

        java.util.Iterator<Fireball> it = fireballs.iterator();
        while (it.hasNext()) {
            Fireball fb = it.next();
            fb.update(null);
            if (fb.getMapEntityStatus() == Level.MapEntityStatus.REMOVED) {
                it.remove();
            }
        }

        if (invulnFrames > 0) {
            invulnFrames--;
        }

        // Invincibility timer handling (after respawn)
        if (isInvincible) {
            invincibleTimer--;
            invincibleBlinkTimer = (invincibleBlinkTimer + 1) % 10;
            if (invincibleTimer <= 0) {
                isInvincible = false;
                invincibleBlinkTimer = 0;
            }
        }

        super.update();
    }

    // Freeze movement for given seconds (approx. 60 FPS frame units)
    public void freezeMovementSeconds(int seconds) {
        if (seconds <= 0)
            return;
        int frames = seconds * 60;
        // extend freeze if new duration is longer
        if (frames > frozenTimer) {
            frozenTimer = frames;
        }
        isFrozen = true;
    }

    private Utils.Point getFireballSpawnOffset() {
        float dx = (facingDirection == Direction.RIGHT) ? (this.getWidth() - 8f) : (-8f);
        float dy = (this.getHeight() * 0.40f);

        String anim = (this.currentAnimationName == null) ? "" : this.currentAnimationName;
        if (anim.contains("PUNCH"))
            dy -= 6f;
        if (anim.contains("JUMP") || anim.contains("FALL"))
            dy -= 10f;

        return new Utils.Point(Math.round(dx), Math.round(dy));
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
                break;
            case PUNCHING:
                playerPunching();
                break;
        }
    }

    protected void playerStanding() {
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.STANDING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        } else if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        } else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerWalking() {
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.WALKING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        } else if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        } else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerJumping() {
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.JUMPING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        }

        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0)
                    jumpForce = 0;
            }
        } else if (airGroundState == AirGroundState.AIR) {
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0)
                    jumpForce = 0;
            }
            if (Keyboard.isKeyDown(MOVE_LEFT_KEY))
                moveAmountX -= walkSpeed;
            else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY))
                moveAmountX += walkSpeed;
            if (moveAmountY > 0)
                increaseMomentum();
        } else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }
    }

    protected void playerPunching() {
        punchDuration++;
        if (punchDuration >= MAX_PUNCH_DURATION) {
            punchDuration = 0;
            playerState = previousNonPunchState;
        }
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed * 0.5f;
            facingDirection = Direction.LEFT;
        } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed * 0.5f;
            facingDirection = Direction.RIGHT;
        }
    }

    protected void increaseMomentum() {
        momentumY += momentumYIncrease;
        if (momentumY > terminalVelocityY)
            momentumY = terminalVelocityY;
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(JUMP_KEY))
            keyLocker.unlockKey(JUMP_KEY);
        if (Keyboard.isKeyUp(PUNCH_KEY))
            keyLocker.unlockKey(PUNCH_KEY);
    }

    protected void handlePlayerAnimation() {
        String baseAnim;
        if (playerState == PlayerState.STANDING)
            baseAnim = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        else if (playerState == PlayerState.WALKING)
            baseAnim = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        else if (playerState == PlayerState.JUMPING)
            baseAnim = (lastAmountMovedY <= 0)
                    ? (facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT")
                    : (facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT");
        else if (playerState == PlayerState.PUNCHING)
            baseAnim = facingDirection == Direction.RIGHT ? "PUNCH_RIGHT" : "PUNCH_LEFT";
        else
            baseAnim = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";

        // If rock protection is active and player is rock skin, use the special animation sprite
        if (rockProtectionHitsRemaining > 0 && isRockSkin()) {
            if (baseAnim != null && baseAnim.startsWith("ROCK_SPECIAL_")) {
                currentAnimationName = baseAnim;
            } else {
                currentAnimationName = "ROCK_SPECIAL_" + baseAnim;
            }
        } else {
            currentAnimationName = baseAnim;
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        } else if (direction == Direction.UP && hasCollided) {
            jumpForce = 0;
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        java.awt.Graphics2D g2 = graphicsHandler.getGraphics();
        java.awt.Composite oldComposite = null;
        boolean appliedAlpha = false;
        if (isInvincible && g2 != null) {

            if (invincibleBlinkTimer < 5) {
                oldComposite = g2.getComposite();
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
                appliedAlpha = true;
            }
        }

        super.draw(graphicsHandler);

        if (appliedAlpha && g2 != null && oldComposite != null) {
            g2.setComposite(oldComposite);
        }
        for (Fireball fb : fireballs) {
            fb.draw(graphicsHandler);
        }
    }

    public int getMaxHearts() {
        return maxHearts;
    }

    public int getHearts() {
        return hearts;
    }

    public int getHeartHP() {
        return heartHP;
    }

    // Damage bar methods
    public int getDamageDealt() {
        return damageDealt;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public void addDamageDealt(int amount) {
        damageDealt += amount;
        if (damageDealt > maxDamage)
            damageDealt = maxDamage;

        // Check if super move is ready
        if (damageDealt >= maxDamage) {
            canUseSuperMove = true;
        }
    }

    public boolean canUseSuperMove() {
        return canUseSuperMove;
    }

    public void useSuperMove() {
        if (canUseSuperMove) {
            damageDealt = 0;
            canUseSuperMove = false;

            if (isRockSkin()) {
                rockProtectionHitsRemaining = 5; // protected for 5 hits
                
            }
        }
    }

    public int getHeartHpMax() {
        return HEART_HP;
    }

    public boolean isKO() {
        return hearts <= 0 && heartHP <= 0;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    // Initiates invincibility for player one
    public void grantInvincibility(int frames) {
        if (frames <= 0)
            return;
        this.isInvincible = true;
        this.invincibleTimer = frames;
        this.invincibleBlinkTimer = 0;
    }

    // Force a death/respawn when the player falls out of the stage bounds
    public void handleFallOffMap() {
        if (map == null || isKO()) {
            return;
        }

        // Ignore any temporary shields/invulnerability so the fall always counts
        this.isInvincible = false;
        this.invincibleTimer = 0;
        this.invincibleBlinkTimer = 0;
        this.invulnFrames = 0;
        this.rockProtectionHitsRemaining = 0;

        int damage = heartHP > 0 ? heartHP : HEART_HP;
        takeDamage(Math.max(1, damage), this.getX());
    }

    public Rectangle getPunchHitbox() {
        if (playerState != PlayerState.PUNCHING)
            return null;
        Rectangle bounds = getBounds();
        float punchRange = 40f;
        float punchHeight = 30f;
        float punchX = (facingDirection == Direction.RIGHT)
                ? bounds.getX() + bounds.getWidth()
                : bounds.getX() - punchRange;
        float punchY = bounds.getY() + (bounds.getHeight() - punchHeight) / 2;
        return new Rectangle(punchX, punchY, (int) punchRange, (int) punchHeight);
    }

    public java.util.List<Fireball> getFireballs() {
        return this.fireballs;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public int getPunchDuration() {
        return punchDuration;
    }

   /*  public boolean takeDamage(int amount) {
        // Fallback: use facing direction to estimate attacker position so existing
        // callers that don't provide an attacker X get reasonable knockback.
        float fallbackAttackerX = this.getX() + (facingDirection == Direction.RIGHT ? 1f : -1f);
        return takeDamage(amount, fallbackAttackerX);
    } */

    // Attacker-aware overload: applies knockback away from attackerX when health
    // decreases (even slightly). Keeps the original behavior and adds the
    // one-frame upward impulse plus a horizontal nudge.
    public boolean takeDamage(int amount, float attackerX) {
        // If rock protection is active, consume one protection and ignore damage
        if (rockProtectionHitsRemaining > 0) {
            rockProtectionHitsRemaining--;
            invulnFrames = 3;
            if (rockProtectionHitsRemaining <= 0) {
               
            }
            return false;
        }

        if (amount <= 0 || invulnFrames > 0 || isInvincible)
            return false;

        // Set short invincibility frames immediately
        invulnFrames = 3;

        int prevHeartHP = heartHP;
        int prevHearts = hearts;
        heartHP -= amount;
        if (heartHP <= 0 && hearts > 1) {
            hearts--;
            heartHP = HEART_HP;
            MusicManager.getInstance().playSoundEffect("Resources/my-leg!-made-with-Voicemod.wav", 10.0f);
        } else if (heartHP <= 0 && hearts == 1) {
            hearts = 0;
            heartHP = 0;
        }
        if (hearts < 0)
            hearts = 0;
        if (heartHP < 0)
            heartHP = 0;

        // If any health decreased (even slightly), apply knockback away from attacker
        boolean healthDecreased = (heartHP < prevHeartHP) || (hearts < prevHearts);
        if (healthDecreased) {
            // increased knockback
            float kbPixels = 20f;
            // determine direction: if attacker is left of us, push right; otherwise push
            // left
            if (attackerX < this.getX()) {
                this.setX(this.getX() + kbPixels);
            } else {
                this.setX(this.getX() - kbPixels);
            }
            // Give a single-frame upward impulse rather than setting persistent momentum
            this.jumpForce = 0; // cancel any active jump force
            this.moveAmountY -= 8f; // one-frame upward move; gravity will pull back next frames
            this.previousX = this.getX();
            this.previousY = this.getY();
        }

        // If a full heart was lost, respawn at a random safe position on the map
        if (hearts < prevHearts && map != null) {
            // pick a spawn tile inside camera with padding (10% of screen or 32px min)
            int padding = Math.max(32, Math.round(Engine.ScreenManager.getScreenWidth() * 0.10f));
            Utils.Point tile = map.getRandomSafeSpawnTileInCamera(padding);
            if (tile.x >= 0) {
                Utils.Point pos = map.getPositionByTileIndex(Math.round(tile.x), Math.round(tile.y));
                this.setX(pos.x);
                // place player on top of tile so they don't fall through
                this.setY(pos.y - this.getHeight());
            } else {
                Utils.Point spawn = map.getRandomSafeSpawnPositionInCamera();
                this.setX(spawn.x);
                this.setY(spawn.y);
            }
            // reset motion to avoid falling through tiles or carrying momentum
            this.momentumY = 0;
            this.jumpForce = 0;
            this.moveAmountX = 0;
            this.moveAmountY = 0;
            this.previousX = this.getX();
            this.previousY = this.getY();
            // short invulnerability after respawn (hit-stun) and a longer invincibility
            // window
            this.invulnFrames = 60;
            this.isInvincible = true;
            this.invincibleTimer = 180; // 3 seconds at 60 FPS
            this.invincibleBlinkTimer = 0;
        }

        invulnFrames = 3;
        return true;
    }

    public Rectangle getCustomHitboxBounds() {
        Rectangle bounds = getBounds();
        int extra = 20;
        int hitboxHeight = bounds.getHeight() + extra;
        int hitboxY = Math.round(bounds.getY()) - (extra / 2);
        return new Rectangle(Math.round(bounds.getX()) + 10, hitboxY,
                bounds.getWidth(), hitboxHeight);
    }

    private boolean isWaterSkin() {
        if (characterSpritePathUsed == null)
            return false;
        String p = characterSpritePathUsed.toLowerCase();
        return p.contains("water");
    }

    private boolean isRockSkin() {
        if (characterSpritePathUsed == null)
            return false;
        String p = characterSpritePathUsed.toLowerCase();
        return p.contains("earth");
    }

    private boolean isFireSkin() {
        if (characterSpritePathUsed == null)
            return false;
        String p = characterSpritePathUsed.toLowerCase();
        return p.contains("fire");
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        int standing = 0;
        int walking_row = 1;
        int walking_col = 0;
        int jump_row = 2;
        int jump_col = 0;
        int fall_row = 3;
        int fall_col = 0;
        int punch_row = 4;
        int punch_col = 0;

        return new HashMap<String, Frame[]>() {
            {
                put("STAND_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, standing, standing, 3, 30, false));
                put("STAND_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, standing, standing, 3, 30, true));
                put("WALK_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, walking_row, walking_col, 3, 30, false));
                put("WALK_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, walking_row, walking_col, 3, 30, true));
                put("JUMP_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, jump_row, jump_col, 3, 20, false));
                put("JUMP_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, jump_row, jump_col, 3, 20, true));
                put("FALL_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, fall_row, fall_col, 3, 20, false));
                put("FALL_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, fall_row, fall_col, 3, 20, true));
                put("PUNCH_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, punch_row, punch_col, 1, 15, false));
                put("PUNCH_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, punch_row, punch_col, 1, 15, true));
                // Rock-special sprites for all base animations
                put("ROCK_SPECIAL_STAND_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 5, 0, 3, 30, false));
                put("ROCK_SPECIAL_STAND_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 5, 0, 3, 30, true));
                put("ROCK_SPECIAL_WALK_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 6, 0, 3, 30, false));
                put("ROCK_SPECIAL_WALK_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 6, 0, 3, 30, true));
                put("ROCK_SPECIAL_JUMP_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 7, 0, 3, 20, false));
                put("ROCK_SPECIAL_JUMP_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 7, 0, 3, 20, true));
                put("ROCK_SPECIAL_FALL_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 8, 0, 3, 20, false));
                put("ROCK_SPECIAL_FALL_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 8, 0, 3, 20, true));
                put("ROCK_SPECIAL_PUNCH_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 9, 0, 1, 15, false));
                put("ROCK_SPECIAL_PUNCH_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 9, 0, 1, 15, true));
            }
        };
    }

    // Keybind getters
    public Engine.Key getJumpKey() {
        return JUMP_KEY;
    }

    public Engine.Key getMoveLeftKey() {
        return MOVE_LEFT_KEY;
    }

    public Engine.Key getMoveRightKey() {
        return MOVE_RIGHT_KEY;
    }

    public Engine.Key getPunchKey() {
        return PUNCH_KEY;
    }

    public Engine.Key getFireballKey() {
        return FIREBALL_KEY;
    }
}
