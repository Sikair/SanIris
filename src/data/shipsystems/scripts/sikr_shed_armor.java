package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.Color;

public class sikr_shed_armor extends BaseShipSystemScript {

	private static final float HIGHER_ARMOR_KEPT = 0.8f;

	private static final Color JITTER_COLOR = new Color(235, 155, 20, 255);

	private boolean fired = false;
	private sikr_shed_armor_overlay overlayPlugin = null;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI) stats.getEntity();
		if (ship == null) return;

		CombatEngineAPI engine = Global.getCombatEngine();

		// IN phase: create/update the gray overlay
		if (state == State.IN) {
			if (overlayPlugin == null || overlayPlugin.isExpired()) {
				overlayPlugin = new sikr_shed_armor_overlay(ship);
				engine.addLayeredRenderingPlugin(overlayPlugin);
			}
			overlayPlugin.setEffectLevel(effectLevel);
		}

		// OUT phase: orange jitter fading out as effectLevel goes 1 → 0
		if (state == State.OUT) {
			ship.setJitter(this, JITTER_COLOR, effectLevel, 2, 0f, 8f * effectLevel);
		}

		// First ACTIVE frame: dismiss the overlay, apply armor, launch visual effect
		if (!fired && state == State.ACTIVE) {
			fired = true;

			if (overlayPlugin != null) {
				overlayPlugin.expire();
				overlayPlugin = null;
			}

			float ammo        = Math.min(ship.getSystem().getAmmo(), 10);
			float armorPct    = ammo / 10f;
			float newArmor    = armorPct * ship.getArmorGrid().getArmorRating() / 15f;

			ArmorGridAPI grid  = ship.getArmorGrid();
			float[][]    cells = grid.getGrid();

			for (int x = 0; x < cells.length; x++) {
				for (int y = 0; y < cells[0].length; y++) {
					float current = grid.getArmorValue(x, y);
					if (current * HIGHER_ARMOR_KEPT < newArmor) {
						grid.setArmorValue(x, y, newArmor);
					} else {
						grid.setArmorValue(x, y, current * HIGHER_ARMOR_KEPT);
					}
				}
			}

			ship.syncWithArmorGridState();

			// Restore weapon health alongside armor
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.isDecorative()) continue;
				if (w.isDisabled()) w.repair();
				w.setCurrHealth(w.getMaxHealth());
			}

			// Clear damage decals when regenerating at least 50% armor
			if (ammo >= 5) {
				ship.clearDamageDecals();
			}

			ship.getSystem().setAmmo(0);

			engine.addLayeredRenderingPlugin(new sikr_shed_armor_render(ship));
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		fired = false;
		if (overlayPlugin != null) {
			overlayPlugin.expire();
			overlayPlugin = null;
		}
	}
}
