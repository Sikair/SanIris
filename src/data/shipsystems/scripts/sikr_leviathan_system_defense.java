package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class sikr_leviathan_system_defense extends BaseShipSystemScript {

    public static final float ROF_BONUS = 0.7f;
	public static final float FLUX_REDUCTION = 30f;
	public static final float DMG_REDUCTION = 0.3f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
    /*float mult = 1f + ROF_BONUS * effectLevel;
	stats.getBallisticRoFMult().modifyMult(id, mult);
	stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));*/	
	stats.getHullDamageTakenMult().modifyMult(id, 1-DMG_REDUCTION);
    stats.getArmorDamageTakenMult().modifyMult(id, 1-DMG_REDUCTION);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
        /*stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);*/
		stats.getHullDamageTakenMult().unmodify();
    	stats.getArmorDamageTakenMult().unmodify();
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("Damage reduction active", false);
		}
		return null;
	}
}