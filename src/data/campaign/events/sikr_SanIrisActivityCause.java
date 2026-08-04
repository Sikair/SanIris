package data.campaign.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class sikr_SanIrisActivityCause extends BaseHostileActivityCause2 {
    public static float MAX_MAG = 0.3f;
	
	public sikr_SanIrisActivityCause(HostileActivityEventIntel intel) {
		super(intel);
	}

	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Negotiating with San Iris about a diplomatic solution " //TODO
						+ "to the exploitation of the Water World, "
						+ "is likely to get this forced overseering to stop. If "
						+ "left unchecked, this low-grade conflict will eventually come to a head and is likely to "
						+ "be resolved one way or another.", 0f, Misc.getHighlightColor(),
						"Negotiating");
				
				FactionAPI f = Global.getSector().getFaction("sikr_saniris");
				tooltip.addPara("Event progress value is based on the number and size of colonies "
						+ "on a \"Water World\".", opad, f.getBaseUIColor(),
						"Water World");
			}
		};
	}
	
	public int getProgress() {
		//if (LuddicChurchHostileActivityFactor.isDefeatedExpedition()) return 0; TODO
		//if (LuddicChurchHostileActivityFactor.isMadeDeal()) return 0;


		int score = 0;
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			if (!market.hasCondition(Conditions.WATER_SURFACE)) continue;
			
			int size = market.getSize();
			score += size;
		}
		
		int progress = score;
		
		return progress; //TODO
	}
	
	
	public String getDesc() {
		return "Colonies on a Water World";
	}

	
	public float getMagnitudeContribution(StarSystemAPI system) {
		//if (KantaCMD.playerHasProtection()) return 0f;
		if (getProgress() <= 0) return 0f;
		
		float total = 0f;
		for (MarketAPI market : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			if (!market.hasCondition(Conditions.WATER_SURFACE)) continue;
			total += market.getSize();
		}
		
		float f = total / 6f;
		if (f > 1f) f = 1f;
		
		return f * MAX_MAG; 
	}

}







