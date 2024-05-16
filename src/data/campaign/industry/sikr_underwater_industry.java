package data.campaign.industry;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class sikr_underwater_industry extends BaseIndustry {
    public static float QUALITY_BONUS = 0.3f;
	
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		//boolean works = Industries.ORBITALWORKS.equals(getId());
		int shipBonus = 0;
		float qualityBonus = QUALITY_BONUS;
		/*if (works) {
			//shipBonus = 2;
			qualityBonus = ORBITAL_WORKS_QUALITY_BONUS;
		}*/
		
		demand(Commodities.METALS, size);
        demand(Commodities.ORGANICS, size-1);
		demand(Commodities.RARE_METALS, size - 1);
		
		supply(Commodities.HEAVY_MACHINERY, size - 1);
		supply(Commodities.SUPPLIES, size - 2);
		supply(Commodities.HAND_WEAPONS, size - 3);
		supply(Commodities.SHIPS, size - 2);
		if (shipBonus > 0) {
			supply(1, Commodities.SHIPS, shipBonus, "Underwater works");
		}
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.METALS, Commodities.RARE_METALS);
		int maxDeficit = size - 3; // to allow *some* production so economy doesn't get into an unrecoverable state
		if (deficit.two > maxDeficit) deficit.two = maxDeficit;
		
		applyDeficitToProduction(2, deficit,
					Commodities.HEAVY_MACHINERY,
					Commodities.SUPPLIES,
					Commodities.HAND_WEAPONS,
					Commodities.SHIPS);
		
//		if (market.getId().equals("chicomoztoc")) {
//			System.out.println("efwefwe");
//		}
		
		if (qualityBonus > 0) {
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), qualityBonus, "Underwater works");
		}
		
		float stability = market.getPrevStability();
		if (stability < 8) {
			float stabilityMod = (stability - 5f) / 5f;
			stabilityMod *= 0.5f;
			//market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, "Low stability at production source");
            market.getStats().getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).modifyFlat(getModId(0), stabilityMod, getNameForModifier() + " - low stability");
			market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(0), stabilityMod, getNameForModifier() + " - low stability");
		}
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}
	
	@Override
	public void unapply() {
		super.unapply();
		
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(getModId(1));
	}

	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		//if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {			
				float total = QUALITY_BONUS;
				String totalStr = "+" + (int)Math.round(total * 100f) + "%";
				Color h = Misc.getHighlightColor();
				if (total < 0) {
					h = Misc.getNegativeHighlightColor();
					totalStr = "" + (int)Math.round(total * 100f) + "%";
				}
				float opad = 10f;
				if (total >= 0) {
					tooltip.addPara("Ship quality: %s", opad, h, totalStr);
					tooltip.addPara("*Quality bonus only applies for the largest ship producer in the faction.", 
							Misc.getGrayColor(), opad);
				}
			
		}
	}
	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

    @Override
    public boolean isAvailableToBuild() {
        return market.getFaction().getRelationship("sikr_saniris") >= 50 && market.hasCondition(Conditions.WATER_SURFACE);
    }

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
	
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		if (special != null && Items.CORRUPTED_NANOFORGE.equals(special.getId()) &&
				data != null && Items.PRISTINE_NANOFORGE.equals(data.getId())) {
			return true;
		}
		return super.wantsToUseSpecialItem(data);
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
	}
	

	@Override
	public void setSpecialItem(SpecialItemData special) {
		super.setSpecialItem(special);
	}	
}









