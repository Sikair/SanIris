package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class sikr_battlecarrier_array extends BaseHullMod {

    public static float FIGHTER_DAMAGE_REDUCTION = 0.2f;
    public static float FIGHTER_RANGE_MULT = 0.5f;
    public static float FIGHTER_MAX_RANGE_BONUS = 1000f;

    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterWingRange().modifyMult(id, 0f);
	}

    @Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		fighter.getMutableStats().getHullDamageTakenMult().modifyMult(id, 1 - FIGHTER_DAMAGE_REDUCTION);
		fighter.getMutableStats().getShieldDamageTakenMult().modifyMult(id, 1 - FIGHTER_DAMAGE_REDUCTION);
		fighter.getMutableStats().getArmorDamageTakenMult().modifyMult(id, 1 - FIGHTER_DAMAGE_REDUCTION);
		
		if (fighter.getWing() != null && fighter.getWing().getSpec() != null) {
			if (fighter.getWing().getSpec().isRegularFighter() ||
					fighter.getWing().getSpec().isAssault() ||
					fighter.getWing().getSpec().isBomber() ||
					fighter.getWing().getSpec().isInterceptor()) {
				fighter.addTag(Tags.WING_STAY_IN_FRONT_OF_SHIP);
			}
		}

        fighter.addListener(new sikr_fighter_array_range_modifier());
	}

    public static class sikr_fighter_array_range_modifier implements WeaponBaseRangeModifier {
		public float max = FIGHTER_MAX_RANGE_BONUS;
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSpec() == null) {
				return 0f;
			}

            if (weapon.getSpec().getMountType() == WeaponType.MISSILE) {
				return 0f;
			}
            
			float base = weapon.getSpec().getMaxRange();
            float bonus = base * FIGHTER_RANGE_MULT;
			if (base + bonus > max) {
				bonus = max - base;
			}
			if (bonus < 0) bonus = 0;
			return bonus;
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) (100 * FIGHTER_RANGE_MULT) + "%";
		if (index == 1) return "" + (int) FIGHTER_MAX_RANGE_BONUS;
		if (index == 2) return "" + (int) (100 * FIGHTER_DAMAGE_REDUCTION) + "%";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
		return ship != null && bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not have fighter bays";
	}
}
