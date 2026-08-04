package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

import java.awt.Color;
import java.util.List;

public class sikr_leviathan_system_defense extends BaseShipSystemScript {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Shared modifier key for damage reduction stats on the horn ship. */
    private static final String MOD_ID = "sikr_defense_horn";

    /** Damage reduction applied to the Leviathan itself while the system is active. */
    private static final float DMG_REDUCTION = 0.3f;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    /** Reference to the horn module ship. Retried every apply() frame until found. */
    private ShipAPI   hornShip        = null;

    /** Cached base armor rating of the horn ship, used for the healing formula. */
    private float     armorRating     = 0f;

    /** True while the system's reinforcement buffs are active on the horn. */
    private boolean   activated       = false;

    /** True once the per-frame plugin has been registered with the engine. */
    private boolean   framePluginAdded = false;

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
            hornShip    = modules.get(0);
            armorRating = hornShip.getArmorGrid().getArmorRating();
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

        // One-time initialization (retried until horn is found)
        resolveHornShip(parent);
        if (sprite == null) {
            sprite = Global.getSettings().getSprite("fx", "sikr_frost");
        }

        // Register the always-running plugin once (velocity sync + HP bar)
        if (!framePluginAdded) {
            framePluginAdded = true;
            engine.addPlugin(new HornFramePlugin(parent, hornShip));
        }

        // Damage reduction on the Leviathan itself
        stats.getHullDamageTakenMult().modifyMult(id, 1f - DMG_REDUCTION);
        stats.getArmorDamageTakenMult().modifyMult(id, 1f - DMG_REDUCTION);

        // First frame of chargeup: reinforce the horn
        // Skipped entirely if the module was already destroyed before activation.
        if (!activated && state == State.IN && isHornValid()) {
            activated = true;

            MagicRender.objectspace(
                sprite, hornShip,
                new Vector2f(5f, 0f), new Vector2f(0f, 0f),
                new Vector2f(390f, 62f), new Vector2f(0f, 0f),
                180f, 0f, true,
                new Color(255, 255, 255, 255),
                true, 2, 8, 2, true
            );

            hornShip.getMutableStats().getHullDamageTakenMult().modifyMult(MOD_ID, 0.2f);
            hornShip.getMutableStats().getArmorDamageTakenMult().modifyMult(MOD_ID, 0.2f);
        }

        // Every active frame: heal the most damaged armor cell on the horn ship
        // Does not run if the module has been destroyed.
        if (activated && isHornValid()) {
            Point point = DefenseUtils.getMostDamagedArmorCell(hornShip);
            if (point != null) {
                float current = hornShip.getArmorGrid().getArmorValue(point.getX(), point.getY());
                float toHeal  = 10f + (armorRating - current) * 0.02f;
                hornShip.getArmorGrid().setArmorValue(point.getX(), point.getY(), toHeal);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);

        if (activated) {
            activated = false;
            if (hornShip != null) {
                // unmodify() is harmless on a destroyed ship — cleans up any lingering stat entries
                hornShip.getMutableStats().getHullDamageTakenMult().unmodify(MOD_ID);
                hornShip.getMutableStats().getArmorDamageTakenMult().unmodify(MOD_ID);
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) return new StatusData("Damage reduction active", false);
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
