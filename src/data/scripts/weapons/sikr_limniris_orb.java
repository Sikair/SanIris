package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.Color;
import java.util.EnumSet;

/**
 * A purple energy orb spawned when a sikr_limniris projectile hits an enemy.
 *
 * Lifecycle:
 *   1. LAUNCHED — orb is ejected sideways from the impact point, decelerates to a stop.
 *   2. HOMING   — orb travels back to the weapon that fired the projectile.
 *                 On arrival it reduces the weapon's remaining cooldown.
 *
 * Trail rendering backend is selectable via USE_MAGIC_TRAIL:
 *   true  → MagicTrail (smooth textured trail, automatic fade)
 *   false → built-in GL11 ring-buffer trail
 */
public class sikr_limniris_orb extends BaseCombatLayeredRenderingPlugin {

    // -----------------------------------------------------------------------
    // Trail backend selector
    // -----------------------------------------------------------------------

    /**
     * Set to true to use MagicTrail for the orb's trail.
     * Set to false to use the built-in GL11 ring-buffer trail.
     */
    private static final boolean USE_MAGIC_TRAIL = true;

    // -----------------------------------------------------------------------
    // State machine
    // -----------------------------------------------------------------------

    private enum OrbState { LAUNCHED, HOMING }

    // -----------------------------------------------------------------------
    // Movement constants
    // -----------------------------------------------------------------------

    /** Initial speed at spawn, in the bounce direction (world units/second). */
    private static final float LAUNCH_SPEED    = 800f;

    /** Drag per second during LAUNCHED: vel *= max(0, 1 - FRICTION * dt). */
    private static final float FRICTION        = 5.5f;

    /** Speed below which the orb is considered stopped and switches to HOMING. */
    private static final float STOP_THRESHOLD  = 8f;

    /** Travel speed toward the weapon during the HOMING phase (world units/second). */
    private static final float HOMING_SPEED    = 420f;

    /** Distance from the weapon at which the orb is considered arrived. */
    private static final float ARRIVAL_RADIUS  = 15f;

    /** Cooldown reduction applied to the weapon on arrival (seconds). */
    private static final float COOLDOWN_REDUCTION = 0.1f;

    /** Fade-in duration at spawn (seconds). */
    private static final float FADE_IN_DURATION = 0.08f;

    /** Safety lifetime cap. */
    private static final float MAX_LIFETIME    = 6f;

    // -----------------------------------------------------------------------
    // MagicTrail constants (used when USE_MAGIC_TRAIL = true)
    // -----------------------------------------------------------------------

    /** Trail segment width at the newest point (pixels). */
    private static final float TRAIL_WIDTH_START    = 20f;

    /** Trail segment width at the oldest point. */
    private static final float TRAIL_WIDTH_END      = 5f;

    /** Opacity at the newest trail point. */
    private static final float TRAIL_OPACITY_START  = 0.85f;

    /** How long each trail segment persists (seconds). */
    private static final float TRAIL_DURATION       = 0.2f;

    // Trail color (0–255 range for MagicTrail)
    private static final Color TRAIL_COLOR = new Color(190, 80, 255);

    // -----------------------------------------------------------------------
    // Built-in GL11 trail constants (used when USE_MAGIC_TRAIL = false)
    // -----------------------------------------------------------------------

    private static final int   TRAIL_CAPACITY = 16;
    private static final float TRAIL_INTERVAL = 0.035f;

    // -----------------------------------------------------------------------
    // Arrival flash colors
    // -----------------------------------------------------------------------

    private static final Color FLASH_OUTER = new Color(190, 80,  255, 255);
    private static final Color FLASH_INNER = new Color(240, 200, 255, 255);

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private final WeaponAPI sourceWeapon;

    /** Current world position. */
    private float x;
    private float y;

    /** Velocity during the LAUNCHED phase. */
    private float velX;
    private float velY;

    private OrbState state    = OrbState.LAUNCHED;
    private float    age      = 0f;
    private boolean  finished = false;

    // Built-in trail ring buffer (allocated regardless; only used when !USE_MAGIC_TRAIL)
    private final float[] trailX   = new float[TRAIL_CAPACITY];
    private final float[] trailY   = new float[TRAIL_CAPACITY];
    private int   trailHead  = 0;
    private int   trailCount = 0;
    private float trailTimer = 0f;

    // MagicTrail sprite and unique trail ID
    private SpriteAPI trailSprite;
    private float     trail_index;

    /**
     * Current movement angle in degrees, updated every frame from the velocity
     * vector (LAUNCHED) or the direction toward the target (HOMING).
     * Passed to MagicTrail so the trail texture aligns with the direction of travel.
     */
    private float movementAngleDeg = 0f;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * @param weapon      The weapon that fired the projectile (homing target).
     * @param spawnPoint  World position of the impact (orb spawn point).
     * @param bounceDirX  Normalized X component of the initial bounce direction.
     * @param bounceDirY  Normalized Y component of the initial bounce direction.
     */
    public sikr_limniris_orb(WeaponAPI weapon, Vector2f spawnPoint,
                              float bounceDirX, float bounceDirY) {
        this.sourceWeapon = weapon;
        this.x    = spawnPoint.x;
        this.y    = spawnPoint.y;
        this.velX = bounceDirX * LAUNCH_SPEED;
        this.velY = bounceDirY * LAUNCH_SPEED;
        this.trailSprite = Global.getSettings().getSprite("fx","trails_trail_smooth");
        this.trail_index = MagicTrailPlugin.getUniqueID();
    }

    // -----------------------------------------------------------------------
    // BaseCombatLayeredRenderingPlugin
    // -----------------------------------------------------------------------

    @Override public boolean isExpired()       { return finished; }
    @Override public float   getRenderRadius() { return Float.MAX_VALUE; }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    @Override
    public void advance(float amount) {
        if (finished) return;
        if (Global.getCombatEngine().isPaused()) return;

        age += amount;
        if (age > MAX_LIFETIME)   { finished = true; return; }
        if (!isSourceValid())     { finished = true; return; }

        if (state == OrbState.LAUNCHED) {
            advanceLaunched(amount);
        } else {
            advanceHoming(amount);
        }

        // Update whichever trail backend is active
        advanceTrail(amount);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (finished) return;

        float fadeIn = Math.min(1f, age / FADE_IN_DURATION);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Built-in trail is rendered here; MagicTrail renders itself automatically
        if (!USE_MAGIC_TRAIL) {
            renderTrailBuiltin(fadeIn);
        }

        // Orb core — rendered regardless of trail backend
        renderOrbCore(fadeIn);

        GL11.glPopAttrib();
    }

    // -----------------------------------------------------------------------
    // Movement phases
    // -----------------------------------------------------------------------

    /** LAUNCHED: apply velocity and drag; transition to HOMING when nearly stopped. */
    private void advanceLaunched(float amount) {
        x += velX * amount;
        y += velY * amount;

        float drag = Math.max(0f, 1f - FRICTION * amount);
        velX *= drag;
        velY *= drag;

        float speed = (float) Math.sqrt(velX * velX + velY * velY);

        // Keep the angle updated as long as the orb is moving
        if (speed > 0.1f) {
            movementAngleDeg = (float) Math.toDegrees(Math.atan2(velY, velX));
        }

        if (speed < STOP_THRESHOLD) {
            velX  = 0f;
            velY  = 0f;
            state = OrbState.HOMING;
        }
    }

    /** HOMING: move toward the weapon; apply cooldown reduction on arrival. */
    private void advanceHoming(float amount) {
        float tx   = sourceWeapon.getLocation().x;
        float ty   = sourceWeapon.getLocation().y;
        float dx   = tx - x;
        float dy   = ty - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist <= ARRIVAL_RADIUS) {
            applyCooldownReduction();
            spawnArrivalFlash();
            finished = true;
            return;
        }

        // Update angle from the direction toward the target (recomputed each frame
        // since the weapon moves with the ship)
        movementAngleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));

        float step = Math.min(HOMING_SPEED * amount, dist);
        x += (dx / dist) * step;
        y += (dy / dist) * step;
    }

    // -----------------------------------------------------------------------
    // Trail — dispatch
    // -----------------------------------------------------------------------

    /**
     * Called every advance() frame after the orb position has been updated.
     * Dispatches to the active trail backend.
     */
    private void advanceTrail(float amount) {
        if (USE_MAGIC_TRAIL) {
            advanceTrailMagic();
        } else {
            advanceTrailBuiltin(amount);
        }
    }

    // -----------------------------------------------------------------------
    // Trail — MagicTrail backend
    // -----------------------------------------------------------------------

    /**
     * Adds one trail member at the current orb position.
     * MagicTrail handles fading, rendering, and cleanup automatically.
     * 'this' is used as the unique trail source so each orb has its own trail.
     */
    private void advanceTrailMagic() {
        MagicTrailPlugin.addTrailMemberSimple(
            sourceWeapon.getShip(),
            trail_index,    //unique index
            trailSprite,
            new Vector2f(x, y),
            0f, //speed
            movementAngleDeg,   // direction of travel, updated every frame
            TRAIL_WIDTH_START,
            TRAIL_WIDTH_END,
            TRAIL_COLOR,
            TRAIL_OPACITY_START,
            0f,    //inDuration           
            TRAIL_DURATION, //mainDuration
            0.4f,       //outDuration
            true    //additive
        );
    }

    // -----------------------------------------------------------------------
    // Trail — built-in GL11 backend
    // -----------------------------------------------------------------------

    /** Records a trail point at fixed intervals into the ring buffer. */
    private void advanceTrailBuiltin(float amount) {
        trailTimer -= amount;
        if (trailTimer <= 0f) {
            trailTimer        = TRAIL_INTERVAL;
            trailX[trailHead] = x;
            trailY[trailHead] = y;
            trailHead         = (trailHead + 1) % TRAIL_CAPACITY;
            if (trailCount < TRAIL_CAPACITY) trailCount++;
        }
    }

    /** Renders the ring-buffer trail using GL11 filled circles. */
    private void renderTrailBuiltin(float fadeIn) {
        for (int i = 0; i < trailCount; i++) {
            int   idx   = (trailHead - 1 - i + TRAIL_CAPACITY) % TRAIL_CAPACITY;
            float ratio = 1f - (float) i / trailCount; // 1 = newest, 0 = oldest
            float alpha = ratio * ratio * 0.35f * fadeIn;
            float size  = 2.5f * ratio;
            drawCircle(trailX[idx], trailY[idx], size, 0.65f, 0.10f, 1.00f, alpha);
        }
    }

    // -----------------------------------------------------------------------
    // Orb core rendering (common to both backends)
    // -----------------------------------------------------------------------

    /**
     * Renders the three concentric glow circles that form the orb head.
     * GL state (blend, no-texture) must already be set by the caller.
     */
    private void renderOrbCore(float fadeIn) {
        drawCircle(x, y, 13f * fadeIn, 0.50f, 0.00f, 0.85f, 0.20f * fadeIn); // outer glow
        drawCircle(x, y,  7f * fadeIn, 0.70f, 0.20f, 1.00f, 0.45f * fadeIn); // mid glow
        drawCircle(x, y,  3f * fadeIn, 0.95f, 0.75f, 1.00f, 0.90f * fadeIn); // bright core
    }

    /** Draws a filled circle using a triangle fan (no texture). */
    private void drawCircle(float cx, float cy, float radius,
                             float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 12; i++) {
            float ang = (float) (2.0 * Math.PI * i / 12);
            GL11.glVertex2f(cx + (float) Math.cos(ang) * radius,
                            cy + (float) Math.sin(ang) * radius);
        }
        GL11.glEnd();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Reduces the weapon's remaining cooldown, floored at 0. */
    private void applyCooldownReduction() {
        float current = sourceWeapon.getCooldownRemaining();
        sourceWeapon.setRemainingCooldownTo(Math.max(0f, current - COOLDOWN_REDUCTION));
    }

    /** Spawns a brief purple flash at the orb's arrival position. */
    private void spawnArrivalFlash() {
        Vector2f pos  = new Vector2f(x, y);
        Vector2f zero = new Vector2f(0f, 0f);
        Global.getCombatEngine().addHitParticle(pos, zero, 28f, 1.2f, 0.25f, FLASH_OUTER);
        Global.getCombatEngine().addHitParticle(pos, zero, 12f, 1.5f, 0.18f, FLASH_INNER);
    }

    /** Returns true if the source weapon and its ship are still active in combat. */
    private boolean isSourceValid() {
        if (sourceWeapon == null) return false;
        if (sourceWeapon.getShip() == null) return false;
        if (!sourceWeapon.getShip().isAlive() || sourceWeapon.getShip().isHulk()) return false;
        if (!Global.getCombatEngine().isEntityInPlay(sourceWeapon.getShip())) return false;
        return true;
    }
}
