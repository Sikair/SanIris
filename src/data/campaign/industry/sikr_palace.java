package data.campaign.industry;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class sikr_palace extends BaseIndustry{

	public static float DEFENSE_BONUS = 0.3f;
    public static int STABILITY_BONUS = 2;
    public static int PATROL_NUM_LIGHT_BONUS = 3;
    public static int PATROL_NUM_MEDIUM_BONUS = 3;
    public static int PATROL_NUM_HEAVY_BONUS = 2;

    @Override
	public boolean isHidden() {
		return !market.getFactionId().equals("sikr_saniris") && market.getPlanetEntity().getId().equals("sikr_iris");
	}

    @Override
    public boolean isFunctional() {
		return super.isFunctional() && market.getFactionId().equals("sikr_saniris") && market.getPlanetEntity().getId().equals("sikr_iris");
	}

    @Override
    public void apply() {
        super.apply(true);
        int size = market.getSize();

        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), PATROL_NUM_LIGHT_BONUS);
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), PATROL_NUM_MEDIUM_BONUS);
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), PATROL_NUM_HEAVY_BONUS);
        //market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), 1 + DEFENSE_BONUS, getNameForModifier());

        modifyStabilityWithBaseMod();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

        demand(Commodities.FOOD, size-2);
        demand(Commodities.MARINES, size-2);
        demand(Commodities.LUXURY_GOODS, size-3);
        demand(Commodities.SUPPLIES, size - 1);
		demand(Commodities.FUEL, size - 1);
		demand(Commodities.SHIPS, size - 1);
		
        if (!isFunctional()) {
			supply.clear();
			unapply();
		}
    }
    @Override
    public void unapply() {
		super.unapply();

        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());
        //market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());

        unmodifyStabilityWithBaseMod();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);

        
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}

    @Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
			addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS, Commodities.SUPPLIES);
		}
    }

    @Override
	protected int getBaseStabilityMod() {
		return STABILITY_BONUS;
	}

    public String getNameForModifier() {
                return Misc.ucFirst(getSpec().getName());
    }

    @Override
	protected Pair<String, Integer> getStabilityAffectingDeficit() {
		return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS);
	}

    // @Override
	// public String getCurrentImage() {
    //     return "";
	// }

    public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

    @Override
    public boolean isAvailableToBuild() {
        //return market.getPlanetEntity().getId().equals("sikr_iris");
        return false;
    }

    @Override
    public String getUnavailableReason() {
        return "Unique";
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }
}
