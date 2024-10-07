package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sikr_small_integration extends BaseHullMod {

	public static final float COST_REDUCTION = 1;
    public static final float RANGE_BONUS = 100;
    public static final float MAX_RANGE = 700;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
	}

    @Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new sikr_small_range_modifier(RANGE_BONUS, MAX_RANGE));
	}
	
	public static class sikr_small_range_modifier implements WeaponBaseRangeModifier {

        public float bonus, max;
		public sikr_small_range_modifier(float bonus, float max) {
			this.bonus = bonus;
			this.max = max;
		}

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
			if (weapon.getSpec().getMountType() != WeaponType.BALLISTIC) {
				return 0f;
			}
			
			float base = weapon.getSpec().getMaxRange();

			if (base + bonus > max) {
				bonus = max - base;
			}
			if (bonus < 0) bonus = 0;
			return bonus;
		}
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

    public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int)RANGE_PENALTY_PERCENT + "%";
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();

        LabelAPI label = tooltip.addPara("Reduces the ordnance point cost of small ballistic weapons by %s and increases their base range by %s up to a max of %s.", opad, h,
                (int) COST_REDUCTION+"", (int) RANGE_BONUS+"", (int) MAX_RANGE+"");
        label.setHighlight((int) COST_REDUCTION+"", (int) RANGE_BONUS+"", (int) MAX_RANGE+"");
        label.setHighlightColors(h, h, h);
	
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad + 7f);
		tooltip.addPara("Since the base range is increased, this modifier"
				+ " - unlike most other flat modifiers - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
	}

}

