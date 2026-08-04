package data.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lwjgl.util.vector.Vector2f;

/**
 * Activation conditions (in priority order):
 * 1. EMERGENCY: hull below 30% AND taking damage → activate even with few charges
 * 2. CRITICAL DANGER: IN_CRITICAL_DPS_DANGER flag AND hull below 50% → activate with ≥1 charge
 * 3. CRITICAL FRONTAL ARMOR (<25%) AND charges at maximum → activate
 * 4. OVERALL ARMOR DAMAGED (<50%) → activate if charges are sufficient relative to danger level
 */
public class sikr_shed_armor_ai implements ShipSystemAIScript {

    // -----------------------------------------------------------------------
    // Thresholds
    // -----------------------------------------------------------------------

    /** Frontal armor fraction below which the zone is considered critical. */
    private static final float FRONTAL_CRITICAL_THRESHOLD = 0.7f;

    /** Overall armor fraction below which danger evaluation begins. */
    private static final float OVERALL_DAMAGED_THRESHOLD = 0.6f;

    /** Hull fraction below which the ship is considered in a critical state. */
    private static final float HULL_CRITICAL_THRESHOLD = 0.3f;

    /** Hull fraction threshold for the combined critical danger condition. */
    private static final float HULL_DANGER_THRESHOLD = 0.5f;

    /** Maximum flux level allowed to activate the system. */
    private static final float MAX_FLUX_TO_ACTIVATE = 0.9f;

    /** Top portion of the armor grid considered as the frontal zone.
     *  The ship sprite points upward, so the front maps to high Y values in the grid. */
    private static final float FRONTAL_ZONE_RATIO = 1f / 3f;

    /** Evaluation interval in seconds to avoid recalculating every frame. */
    private static final float EVAL_INTERVAL = 0.25f;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private ShipAPI        ship;
    private ShipSystemAPI  system;
    private ShipwideAIFlags flags;

    private final IntervalUtil interval = new IntervalUtil(EVAL_INTERVAL, EVAL_INTERVAL);



    // -----------------------------------------------------------------------
    // Init
    // -----------------------------------------------------------------------

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship   = ship;
        this.system = system;
        this.flags  = flags;
    }

    // -----------------------------------------------------------------------
    // Main loop
    // -----------------------------------------------------------------------

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        interval.advance(amount);
        if(!interval.intervalElapsed()) return;

        if (system.isCoolingDown() || system.isActive()) return;
        if (system.getAmmo() <= 0) return;

        // Flux too high: do not activate
        if (ship.getFluxLevel() > MAX_FLUX_TO_ACTIVATE) return;

        int   ammo    = system.getAmmo();
        int   maxAmmo = system.getMaxAmmo();
        float hull    = ship.getHullLevel();
        boolean takingDamage = flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE);

        ArmorGridAPI grid         = ship.getArmorGrid();
        float        frontalArmor = evaluateFrontalArmor(grid);
        float        overallArmor = evaluateOverallArmor(grid);
        float        danger       = evaluateDanger();

        // --- Condition 1: absolute emergency ---
        // Critical hull + active incoming damage → activate regardless of charge count
        if (hull < HULL_CRITICAL_THRESHOLD && takingDamage) {
            ship.useSystem();
            return;
        }

        // --- Condition 2: critical danger ---
        // IN_CRITICAL_DPS_DANGER flag + damaged hull → activate with ≥1 charge
        if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER) && hull < HULL_DANGER_THRESHOLD) {
            ship.useSystem();
            return;
        }

        // --- Condition 3: critical frontal armor + maximum charges ---
        // Reserved for the optimal case: full regeneration potential available
        if (ammo == maxAmmo && frontalArmor < FRONTAL_CRITICAL_THRESHOLD) {
            ship.useSystem();
            return;
        }

        // --- Condition 4: overall armor damaged ---
        // Higher danger → fewer charges required before activating.
        // danger ∈ [0..1] → required charge ratio ∈ [0.5..0.1]
        if (overallArmor < OVERALL_DAMAGED_THRESHOLD) {
            float chargeRatio         = (float) ammo / (float) maxAmmo;
            float requiredChargeRatio = 0.5f - danger * 0.4f;
            if (chargeRatio >= requiredChargeRatio) {
                ship.useSystem();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Armor evaluation
    // -----------------------------------------------------------------------

    /**
     * Average armor fraction in the frontal zone of the ship.
     * The sprite points upward, so the front corresponds to the top third of the grid (high Y).
     */
    private float evaluateFrontalArmor(ArmorGridAPI grid) {
        float[][] cells  = grid.getGrid();
        int       width  = cells.length;
        int       height = cells[0].length;
        int       frontY = (int) (height * (1f - FRONTAL_ZONE_RATIO));

        float sum   = 0f;
        int   count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = frontY; y < height; y++) {
                sum += grid.getArmorFraction(x, y);
                count++;
            }
        }
        return count > 0 ? sum / count : 1f;
    }

    /**
     * Average armor fraction across the entire grid.
     */
    private float evaluateOverallArmor(ArmorGridAPI grid) {
        float[][] cells  = grid.getGrid();
        int       width  = cells.length;
        int       height = cells[0].length;

        float sum   = 0f;
        int   count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sum += grid.getArmorFraction(x, y);
                count++;
            }
        }
        return count > 0 ? sum / count : 1f;
    }

    // -----------------------------------------------------------------------
    // Danger evaluation
    // -----------------------------------------------------------------------

    /**
     * Composite danger score [0..1].
     * Combines AI flags, flux level, and hull state.
     */
    private float evaluateDanger() {
        float danger = 0f;

        if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) danger += 0.45f;
        if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE))    danger += 0.25f;
        if (flags.hasFlag(AIFlags.NEEDS_HELP))             danger += 0.15f;
        if (flags.hasFlag(AIFlags.BACK_OFF))               danger += 0.10f;

        // High flux = shields under pressure = increased danger
        danger += ship.getFluxLevel() * 0.30f;

        // Low hull = increased urgency
        danger += (1f - ship.getHullLevel()) * 0.25f;

        return Math.min(1f, danger);
    }
}
