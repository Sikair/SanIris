package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.*;

/**
 * Rendering plugin that breaks the ship sprite into Voronoi polygon chunks
 * and scatters them outward to simulate shedding armor.
 *
 * Register via CombatEngineAPI.addLayeredRenderingPlugin(new sikr_shed_armor_render(ship)).
 */
public class sikr_shed_armor_render extends BaseCombatLayeredRenderingPlugin {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Number of fragment seed columns and rows (total = GRID_W * GRID_H). */
    private static final int GRID_W = 8;
    private static final int GRID_H = 6;
    private static final int FRAGMENT_COUNT = GRID_W * GRID_H;

    /** Total animation duration in seconds. */
    private static final float ANIM_DURATION = 2f;

    /** Maximum scatter speed in world units per second. */
    private static final float MAX_SPEED = 80f;

    /** Maximum fragment rotation speed in degrees per second. */
    private static final float MAX_ROTATION_SPEED = 200f;

    /** Animation progress at which fade-out begins (0 = immediately). */
    private static final float FADE_START = 0f;

    /** Vertex scale multiplier (1.0 = matches original sprite size). */
    private static final float CHUNK_SCALE = 1.0f;

    /** Fragment color — matches the darkest point of the IN phase overlay. */
    private static final float TARGET_R = 0.4f;
    private static final float TARGET_G = 0.4f;
    private static final float TARGET_B = 0.4f;

    // -----------------------------------------------------------------------
    // Inner types
    // -----------------------------------------------------------------------

    /**
     * A single polygonal fragment of the sprite.
     * UV coordinates are normalized [0..1].
     * Local vertices are in pixels centered on the fragment centroid.
     */
    private static class Fragment {
        /** Polygon vertices in UV space. */
        float[] uvX, uvY;
        /** Polygon vertices in local pixel space. */
        float[] lx, ly;
        /** Vertex count. */
        int count;

        /** World position at spawn. */
        float worldX, worldY;
        /** Scatter velocity in world units per second. */
        float velX, velY;
        /** Rotation speed in degrees per second. */
        float rotSpeed;
        /** Current angle in degrees. */
        float angle;
        /** Time elapsed since spawn. */
        float age;
    }

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private final ShipAPI         ship;
    private       SpriteAPI       sprite;
    private final List<Fragment>  fragments   = new ArrayList<>();
    private       boolean         initialized = false;
    private       boolean         finished    = false;
    private final Random          rng         = new Random();

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public sikr_shed_armor_render(ShipAPI ship) {
        this.ship = ship;
    }

    // -----------------------------------------------------------------------
    // Initialization
    // -----------------------------------------------------------------------

    /**
     * Loads the sprite and generates all Voronoi fragment polygons.
     * Called once on the first render frame.
     */
    private void init() {
        sprite = Global.getSettings().getSprite("fx", "sikr_ship_bara");

        float sw = sprite.getWidth();
        float sh = sprite.getHeight();

        // getCenterX/Y() = distance from left/bottom edge (world Y-up convention)
        // Anchor from top = sh - getCenterY()
        float anchorX        = ship.getSpriteAPI().getCenterX();
        float anchorYFromTop = sh - ship.getSpriteAPI().getCenterY();

        // Grid-based seeds with small random jitter for uniform coverage
        int     seedCount = FRAGMENT_COUNT;
        float[] seedU     = new float[seedCount];
        float[] seedV     = new float[seedCount];
        int s = 0;
        for (int gy = 0; gy < GRID_H; gy++) {
            for (int gx = 0; gx < GRID_W; gx++) {
                float jitterU = (rng.nextFloat() - 0.5f) * 0.5f / GRID_W;
                float jitterV = (rng.nextFloat() - 0.5f) * 0.5f / GRID_H;
                seedU[s] = Math.max(0.02f, Math.min(0.98f, (gx + 0.5f) / GRID_W + jitterU));
                seedV[s] = Math.max(0.02f, Math.min(0.98f, (gy + 0.5f) / GRID_H + jitterV));
                s++;
            }
        }

        // Build a convex polygon approximating each Voronoi cell
        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            Fragment f = buildVoronoiFragment(seedU, seedV, i, sw, sh);
            if (f == null) continue;

            // Offset from the real sprite anchor (not the geometric center)
            float offsetX =  (f.worldX - anchorX);
            float offsetY = -(f.worldY - anchorYFromTop);

            // Rotate offset to match the ship's current facing
            float shipAngleRad = (float) Math.toRadians(ship.getFacing() - 90f);
            float cosA = (float) Math.cos(shipAngleRad);
            float sinA = (float) Math.sin(shipAngleRad);

            float rotOffX = offsetX * cosA - offsetY * sinA;
            float rotOffY = offsetX * sinA + offsetY * cosA;

            f.worldX = ship.getLocation().x + rotOffX;
            f.worldY = ship.getLocation().y + rotOffY;

            // Radial scatter velocity away from the ship center
            float dx   = f.worldX - ship.getLocation().x;
            float dy   = f.worldY - ship.getLocation().y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 1f) dist = 1f;

            float speed = MAX_SPEED * (0.4f + rng.nextFloat() * 0.6f);
            f.velX     = (dx / dist) * speed + (rng.nextFloat() - 0.5f) * 40f;
            f.velY     = (dy / dist) * speed + (rng.nextFloat() - 0.5f) * 40f;
            f.rotSpeed = (rng.nextFloat() - 0.5f) * 2f * MAX_ROTATION_SPEED;
            f.angle    = ship.getFacing() - 90f;
            f.age      = 0f;

            fragments.add(f);
        }

        initialized = true;
    }

    /**
     * Builds a convex polygon approximating the Voronoi cell around seed [idx].
     *
     * @param su  U coordinates of all seeds
     * @param sv  V coordinates of all seeds
     * @param idx Index of the target seed
     * @param sw  Sprite width in pixels
     * @param sh  Sprite height in pixels
     * @return Fragment, or null if the area is too small.
     */
    private Fragment buildVoronoiFragment(float[] su, float[] sv,
                                          int idx, float sw, float sh) {
        int   rays = 8 + rng.nextInt(5); // 8 to 12 rays
        float cx   = su[idx];
        float cy   = sv[idx];

        float[] polyU = new float[rays];
        float[] polyV = new float[rays];

        for (int r = 0; r < rays; r++) {
            float angle  = (float) (2.0 * Math.PI * r / rays);
            float radius = findVoronoiBoundary(su, sv, idx, cx, cy, angle);
            // Slight organic variation
            radius  *= (0.92f + rng.nextFloat() * 0.08f);
            polyU[r] = Math.max(0f, Math.min(1f, cx + radius * (float) Math.cos(angle)));
            polyV[r] = Math.max(0f, Math.min(1f, cy + radius * (float) Math.sin(angle)));
        }

        // Compute centroid in UV space
        float centU = 0f, centV = 0f;
        for (int r = 0; r < rays; r++) { centU += polyU[r]; centV += polyV[r]; }
        centU /= rays;
        centV /= rays;

        // Convert UV deltas to local pixel coordinates
        float[] lx = new float[rays];
        float[] ly = new float[rays];
        for (int r = 0; r < rays; r++) {
            lx[r] =  (polyU[r] - centU) * sw * CHUNK_SCALE;
            // UV V grows downward, world Y grows upward → negate
            ly[r] = -((polyV[r] - centV) * sh * CHUNK_SCALE);
        }

        float area = polygonArea(lx, ly, rays);
        if (area < 4f) return null;

        Fragment f = new Fragment();
        f.uvX    = polyU;
        f.uvY    = polyV;
        f.lx     = lx;
        f.ly     = ly;
        f.count  = rays;
        f.worldX = centU * sw;
        f.worldY = centV * sh;
        return f;
    }

    /**
     * Finds the distance to the Voronoi boundary from (cx, cy) in the given direction.
     * Uses the perpendicular bisector formula: t = |d|² / (2 · proj).
     */
    private float findVoronoiBoundary(float[] su, float[] sv,
                                      int idx, float cx, float cy, float angle) {
        float minT = Float.MAX_VALUE;
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);

        for (int j = 0; j < su.length; j++) {
            if (j == idx) continue;
            float dx   = su[j] - cx;
            float dy   = sv[j] - cy;
            float proj = dx * cosA + dy * sinA;
            if (proj <= 0f) continue;
            float t = (dx * dx + dy * dy) / (2f * proj);
            if (t < minT) minT = t;
        }
        return (minT == Float.MAX_VALUE) ? 0.3f : minT;
    }

    /** Computes polygon area using the shoelace formula. */
    private float polygonArea(float[] xs, float[] ys, int n) {
        float area = 0f;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            area += (xs[j] + xs[i]) * (ys[j] - ys[i]);
        }
        return Math.abs(area) * 0.5f;
    }

    // -----------------------------------------------------------------------
    // BaseCombatLayeredRenderingPlugin
    // -----------------------------------------------------------------------

    private boolean isShipInvalid() {
        if (ship == null) return true;
        if (!Global.getCombatEngine().isEntityInPlay(ship)) return true;
        return false;
    }

    @Override
    public boolean isExpired() {
        return finished;
    }

    @Override
    public float getRenderRadius() {
        // Intentionally large to disable culling at all zoom levels
        return Float.MAX_VALUE;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    @Override
    public void advance(float amount) {
        if (finished) return;
        if (!initialized) return;
        if (isShipInvalid()) { finished = true; return; }
        if (Global.getCombatEngine().isPaused()) return;

        boolean allDone = true;
        for (Fragment f : fragments) {
            if (f.age >= ANIM_DURATION) continue;
            allDone = false;

            f.age    += amount;
            f.worldX += f.velX * amount;
            f.worldY += f.velY * amount;
            f.velX   *= Math.max(0f, 1f - 0.6f * amount);
            f.velY   *= Math.max(0f, 1f - 0.6f * amount);
            f.angle  += f.rotSpeed * amount;
        }
        if (allDone) finished = true;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (finished) return;
        if (isShipInvalid()) { finished = true; return; }
        if (!initialized) init();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        sprite.bindTexture();

        float texW = sprite.getTextureWidth();
        float texH = sprite.getTextureHeight();

        for (Fragment f : fragments) {
            if (f.age >= ANIM_DURATION) continue;

            // Fade-out over the animation
            float progress = f.age / ANIM_DURATION;
            float alpha    = (progress < FADE_START)
                    ? 1f
                    : 1f - (progress - FADE_START) / (1f - FADE_START);
            alpha = Math.max(0f, alpha);

            GL11.glColor4f(TARGET_R, TARGET_G, TARGET_B, alpha);

            GL11.glPushMatrix();
            GL11.glTranslatef(f.worldX, f.worldY, 0f);
            GL11.glRotatef(f.angle, 0f, 0f, 1f);

            // Compute UV centroid for the triangle fan center
            float cuv = 0f, cvv = 0f;
            for (int v = 0; v < f.count; v++) { cuv += f.uvX[v]; cvv += f.uvY[v]; }
            cuv /= f.count;
            cvv /= f.count;

            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glTexCoord2f(cuv * texW, (1f - cvv) * texH);
            GL11.glVertex2f(0f, 0f);

            for (int v = 0; v <= f.count; v++) {
                int   vi = v % f.count;
                float u  = f.uvX[vi] * texW;
                float tv = (1f - f.uvY[vi]) * texH;
                GL11.glTexCoord2f(u, tv);
                GL11.glVertex2f(f.lx[vi], f.ly[vi]);
            }
            GL11.glEnd();

            GL11.glPopMatrix();
        }

        GL11.glPopAttrib();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Registers this plugin with the combat engine. */
    public void register(CombatEngineAPI engine) {
        engine.addLayeredRenderingPlugin(this);
    }

    /** Resets the animation (rebuilds all fragments). */
    public void reset() {
        fragments.clear();
        initialized = false;
        finished    = false;
    }
}
