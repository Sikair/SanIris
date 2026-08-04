package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

import java.awt.Color;
import java.util.List;

public class sikr_leviathan_system_offense extends BaseShipSystemScript {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Shared modifier key used for damage reduction stats. */
    private static final String MOD_ID = "sikr_offense_horn";

    /** Horn ship physical properties during system activation. */
    private static final float     HORN_ACTIVE_MASS    = 24000f;
    private static final float     HORN_DEFAULT_MASS   = 1100f;
    private static final HullSize  HORN_ACTIVE_SIZE    = HullSize.CAPITAL_SHIP;
    private static final HullSize  HORN_DEFAULT_SIZE   = HullSize.FRIGATE;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    /** Reference to the horn module ship. Retried every apply() frame until found. */
    private ShipAPI  hornShip             = null;

    /** True while the system's reinforcement buffs are active. */
    private boolean  activated            = false;

    /** True once the per-frame plugin has been registered with the engine. */
    private boolean  framePluginAdded     = false;

    private SpriteAPI sprite;

    // -----------------------------------------------------------------------
    // Horn ship lookup
    // -----------------------------------------------------------------------

    /**
     * Finds the horn module among the Leviathan's child modules.
     * Retried every apply() frame until the module list is populated —
     * getChildModulesCopy() can return an empty list on the very first frames.
     */
    private void resolveHornShip(ShipAPI parent) {
        if (hornShip != null) return; // already found
        List<ShipAPI> modules = parent.getChildModulesCopy();
        if (modules != null && !modules.isEmpty()) {
            hornShip = modules.get(0);
        }
        // No early-exit flag — retry on the next frame if still null
    }

    /**
     * Returns true if the horn module is present and still alive in combat.
     * Once destroyed, a module cannot be recovered — this will permanently return false.
     */
    private boolean isHornValid() {
        return hornShip != null && hornShip.isAlive() && !hornShip.isHulk();
    }

    // -----------------------------------------------------------------------
    // BaseShipSystemScript
    // -----------------------------------------------------------------------

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI parent = (ShipAPI) stats.getEntity();
        if (parent == null) return;

        CombatEngineAPI engine = Global.getCombatEngine();

        // One-time initialization
        resolveHornShip(parent);
        if (sprite == null) {
            sprite = Global.getSettings().getSprite("fx", "sikr_heat");
        }

        // Register the always-running plugin once (velocity sync + HP bar)
        if (!framePluginAdded) {
            framePluginAdded = true;
            engine.addPlugin(new HornFramePlugin(parent, hornShip));
        }

        // Orange underbelly jitter on the parent ship (using stable source reference)
        parent.setJitterUnder(this, new Color(200, 70, 0, 200), effectLevel, 5, 0f, 15f);

        // Speed boost: unmodify during ramp-down so the ship decelerates naturally
        if (state == State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 200f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 350f * effectLevel);
        }

        // First frame of chargeup: reinforce the horn and apply damage reductions
        // Skipped entirely if the module was already destroyed before activation.
        if (!activated && state == State.IN && isHornValid()) {
            activated = true;

            // Visual burst on the horn ship at the render offset used in object-space
            MagicRender.objectspace(
                sprite, hornShip,
                new Vector2f(53f, 0f), new Vector2f(0f, 0f),
                new Vector2f(206f, 218f), new Vector2f(0f, 0f),
                180f, 0f,
                true,
                new Color(255, 255, 255, 255),
                true, 2, 6, 6, true
            );

            // Damage reduction for the horn ship
            hornShip.getMutableStats().getHullDamageTakenMult().modifyMult(MOD_ID, 0.54f);
            hornShip.getMutableStats().getArmorDamageTakenMult().modifyMult(MOD_ID, 0.30f);

            // Damage reduction for the Leviathan itself
            parent.getMutableStats().getHullDamageTakenMult().modifyMult(MOD_ID, 0.50f);
            parent.getMutableStats().getArmorDamageTakenMult().modifyMult(MOD_ID, 0.10f);

            // Treat the horn as a capital-mass object while engaged
            hornShip.setMass(HORN_ACTIVE_MASS);
            hornShip.setHullSize(HORN_ACTIVE_SIZE);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        // Revert horn reinforcement if it was applied
        if (activated) {
            activated = false;

            ShipAPI parent = (ShipAPI) stats.getEntity();

            if (hornShip != null) {
                // unmodify() is harmless on a destroyed ship — cleans up any lingering stat entries
                hornShip.getMutableStats().getHullDamageTakenMult().unmodify(MOD_ID);
                hornShip.getMutableStats().getArmorDamageTakenMult().unmodify(MOD_ID);
                // setMass/setHullSize only make sense on a living ship
                if (isHornValid()) {
                    hornShip.setMass(HORN_DEFAULT_MASS);
                    hornShip.setHullSize(HORN_DEFAULT_SIZE);
                }
            }
            if (parent != null) {
                parent.getMutableStats().getHullDamageTakenMult().unmodify(MOD_ID);
                parent.getMutableStats().getArmorDamageTakenMult().unmodify(MOD_ID);
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) return new StatusData("Speed boost engaged", false);
        return null;
    }

    // -----------------------------------------------------------------------
    // Per-frame plugin (velocity sync + player HUD bar)
    // -----------------------------------------------------------------------

    /**
     * Registered once on the first system activation frame.
     * Runs every combat frame regardless of system state.
     *
     * Responsibilities:
     *   - Keep the horn ship's velocity in sync with the Leviathan's (prevents drift)
     *   - Display the horn ship's HP as a status bar when the player pilots the Leviathan
     */
    private static class HornFramePlugin extends BaseEveryFrameCombatPlugin {

        private final ShipAPI parent;
        private ShipAPI horn;

        HornFramePlugin(ShipAPI parent, ShipAPI horn) {
            this.parent = parent;
            this.horn   = horn;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine.isPaused()) return;
            if (parent == null || !parent.isAlive() || parent.isHulk()) return;

            // Retry horn resolution if it was null at registration time
            if (horn == null) {
                List<ShipAPI> modules = parent.getChildModulesCopy();
                if (modules != null && !modules.isEmpty()) horn = modules.get(0);
            }
            if (horn == null || !horn.isAlive() || horn.isHulk()) return;

            // Sync velocity so the horn follows the Leviathan precisely
            horn.getVelocity().set(parent.getVelocity());

            // HP bar visible only to the player
            if (parent == engine.getPlayerShip()) {
                MagicUI.drawInterfaceStatusBar(
                    parent,
                    horn.getHitpoints() / horn.getMaxHitpoints(),
                    null, null, 0,
                    "HORN",
                    (int) horn.getHitpoints()
                );
            }
        }

        @Override public void renderInWorldCoords(ViewportAPI viewport) {}
        @Override public void renderInUICoords(ViewportAPI viewport) {}
    }
}
