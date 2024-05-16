package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class sikr_armor extends BaseHullMod {

    public static float ARMOR_BONUS = 1f;
	public static float ARMOR_MULT = 0.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyMult(id, 1f + ARMOR_BONUS);
		stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Math.round(ARMOR_BONUS * 100) + "%";
		if (index == 1) return "" + (int) Math.round(ARMOR_MULT * 100) + "%";
		return null;
	}
}