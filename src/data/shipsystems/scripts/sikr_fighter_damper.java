package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class sikr_fighter_damper extends BaseShipSystemScript {
	public static final Object KEY_JITTER = new Object();
	public static final Color JITTER_UNDER_COLOR = new Color(255,165,90,155);
	public static final Color JITTER_COLOR = new Color(255,165,90,55);

	public static final float DMG_REDUCTION = 0.5f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

        if (effectLevel > 0) {
			float jitterLevel = effectLevel;
            float maxRangeBonus = 5f;
			float jitterRangeBonus = jitterLevel * maxRangeBonus;

			if(ship == Global.getCombatEngine().getPlayerShip()){
				Global.getCombatEngine().maintainStatusForPlayerShip("San-Iris", "graphics/icons/hullsys/targeting_feed.png", "Flux Dissipation",
				"Fighters damage taken reduced by " + (int)Math.round((DMG_REDUCTION) * 100f) + "%", false);
			}

			List<ShipAPI> fighters = null;
			fighters = getFighters(ship);
			if (fighters == null) { // shouldn't be possible, but still
				fighters = new ArrayList<ShipAPI>();
			}
			
			for (ShipAPI fighter : fighters) {
				if (fighter.isHulk()) continue;

				if (effectLevel == 1) {
					if (fighter != null) { 
                        MutableShipStatsAPI fStats = fighter.getMutableStats();
                        fStats.getHullDamageTakenMult().modifyMult(id, 1 - DMG_REDUCTION);
		                fStats.getArmorDamageTakenMult().modifyMult(id, 1 - DMG_REDUCTION);
		                fStats.getEmpDamageTakenMult().modifyMult(id, 1 - DMG_REDUCTION);
                        fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
					    fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
                    }
				}
			}
        }
    }

    public static List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
	}

    public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
        List<ShipAPI> fighters = null;
		fighters = getFighters(ship);
		if (fighters == null) { // shouldn't be possible, but still
			fighters = new ArrayList<ShipAPI>();
		}		
		for (ShipAPI fighter : fighters) {
			if (fighter.isHulk()) continue;
			if (fighter != null) { 
                MutableShipStatsAPI fStats = fighter.getMutableStats();
                fStats.getHullDamageTakenMult().unmodify(id);
                fStats.getArmorDamageTakenMult().unmodify(id);
                fStats.getEmpDamageTakenMult().unmodify(id);
            }	
		}
	}

}
