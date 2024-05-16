package data.campaign.industry;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sikr_water_tech extends BaseHazardCondition {

    public static Map<String, String> COMMODITY = new HashMap<String, String>();
    public static Map<String, String> INDUSTRY = new HashMap<String, String>();
    public static Map<String, Integer> MODIFIER = new HashMap<String, Integer>();

    public static int WORK_MODIFIER = 1;
    public static float QUALITY_BONUS = 0.1f;
    public static float STABILITY_PENALTY = 3;

    static{
        COMMODITY.put(Conditions.ORE_SPARSE, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_MODERATE, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_ABUNDANT, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_RICH, Commodities.ORE);
		COMMODITY.put(Conditions.ORE_ULTRARICH, Commodities.ORE);
		
		COMMODITY.put(Conditions.RARE_ORE_SPARSE, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_MODERATE, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_ABUNDANT, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_RICH, Commodities.RARE_ORE);
		COMMODITY.put(Conditions.RARE_ORE_ULTRARICH, Commodities.RARE_ORE);
		
		COMMODITY.put(Conditions.ORGANICS_TRACE, Commodities.ORGANICS);
		COMMODITY.put(Conditions.ORGANICS_COMMON, Commodities.ORGANICS);
		COMMODITY.put(Conditions.ORGANICS_ABUNDANT, Commodities.ORGANICS);
		COMMODITY.put(Conditions.ORGANICS_PLENTIFUL, Commodities.ORGANICS);
		
		COMMODITY.put(Conditions.VOLATILES_TRACE, Commodities.VOLATILES);
		COMMODITY.put(Conditions.VOLATILES_DIFFUSE, Commodities.VOLATILES);
		COMMODITY.put(Conditions.VOLATILES_ABUNDANT, Commodities.VOLATILES);
		COMMODITY.put(Conditions.VOLATILES_PLENTIFUL, Commodities.VOLATILES);
		
		COMMODITY.put(Conditions.VOLTURNIAN_LOBSTER_PENS, Commodities.LOBSTER);
		COMMODITY.put(Conditions.WATER_SURFACE, Commodities.FOOD);

        INDUSTRY.put(Commodities.ORE, Industries.MINING);
		INDUSTRY.put(Commodities.RARE_ORE, Industries.MINING);
		INDUSTRY.put(Commodities.VOLATILES, Industries.MINING);
		INDUSTRY.put(Commodities.ORGANICS, Industries.MINING);
		
		INDUSTRY.put(Commodities.FOOD, Industries.AQUACULTURE);
		INDUSTRY.put(Commodities.LOBSTER, Industries.AQUACULTURE);

        MODIFIER.put(Commodities.ORE, 1);
        MODIFIER.put(Commodities.RARE_ORE, 1);
        MODIFIER.put(Commodities.VOLATILES, 1);
        MODIFIER.put(Commodities.ORGANICS, 1);

        MODIFIER.put(Commodities.FOOD, 2);
        MODIFIER.put(Commodities.LOBSTER, 1);
    }

    public void apply(String id) {
        super.apply(id);

        //Mining and aquaculture bonus
        for(MarketConditionAPI m : market.getConditions()){
            String commodity = COMMODITY.get(m.getId());
            if(commodity != null){
                if(market.hasIndustry(INDUSTRY.get(commodity))){
                    Industry industry = market.getIndustry(INDUSTRY.get(commodity));
                    if(industry.isFunctional()){
                        industry.supply(id + commodity + "_0", commodity, 0, BaseIndustry.BASE_VALUE_TEXT);
                        industry.supply(id + commodity + "_1", commodity, MODIFIER.get(commodity), Misc.ucFirst(condition.getName().toLowerCase()));
                    }else{
                        industry.getSupply(commodity).getQuantity().unmodifyFlat(id + commodity +"_0");
                        industry.getSupply(commodity).getQuantity().unmodifyFlat(id + commodity +"_1");
                    }
                }
            }
        }

        //Industry bonus
        if(market.hasIndustry(Industries.HEAVYINDUSTRY) || market.hasIndustry(Industries.ORBITALWORKS)){
            Industry industry_heavy = market.getIndustry(Industries.HEAVYINDUSTRY);
            if (industry_heavy == null) industry_heavy = market.getIndustry(Industries.ORBITALWORKS);

            if(industry_heavy != null){
                market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id + "_quality", QUALITY_BONUS, "Tamed Ocean");

                if(industry_heavy.isFunctional()){
                    industry_heavy.supply(id + Commodities.HAND_WEAPONS + "_0", Commodities.HAND_WEAPONS, 0, BaseIndustry.BASE_VALUE_TEXT);
                    industry_heavy.supply(id + Commodities.HAND_WEAPONS + "_1", Commodities.HAND_WEAPONS, WORK_MODIFIER, Misc.ucFirst(condition.getName().toLowerCase()));
                    industry_heavy.supply(id + Commodities.SHIPS + "_0", Commodities.SHIPS, 0, BaseIndustry.BASE_VALUE_TEXT);
                    industry_heavy.supply(id + Commodities.SHIPS + "_1", Commodities.SHIPS, WORK_MODIFIER, Misc.ucFirst(condition.getName().toLowerCase()));
                    industry_heavy.supply(id + Commodities.HEAVY_MACHINERY + "_0", Commodities.HEAVY_MACHINERY, 0, BaseIndustry.BASE_VALUE_TEXT);
                    industry_heavy.supply(id + Commodities.HEAVY_MACHINERY + "_1", Commodities.HEAVY_MACHINERY, WORK_MODIFIER, Misc.ucFirst(condition.getName().toLowerCase()));
                    industry_heavy.supply(id + Commodities.SUPPLIES + "_0", Commodities.SUPPLIES, 0, BaseIndustry.BASE_VALUE_TEXT);
                    industry_heavy.supply(id + Commodities.SUPPLIES + "_1", Commodities.SUPPLIES, WORK_MODIFIER, Misc.ucFirst(condition.getName().toLowerCase()));
                    //indEvo integration
                    if(Global.getSettings().getModManager().isModEnabled("indEvo")){
                        industry_heavy.supply(id + "IndEvo_parts" + "_0", "IndEvo_parts", 0, BaseIndustry.BASE_VALUE_TEXT);
                        industry_heavy.supply(id + "IndEvo_parts" + "_1", "IndEvo_parts", WORK_MODIFIER, Misc.ucFirst(condition.getName().toLowerCase()));
                    }
                }else{
                    industry_heavy.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + Commodities.HAND_WEAPONS +"_0");
                    industry_heavy.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + Commodities.HAND_WEAPONS +"_1");
                    industry_heavy.getSupply(Commodities.SHIPS).getQuantity().unmodifyFlat(id + Commodities.SHIPS +"_0");
                    industry_heavy.getSupply(Commodities.SHIPS).getQuantity().unmodifyFlat(id + Commodities.SHIPS +"_1");
                    industry_heavy.getSupply(Commodities.HEAVY_MACHINERY).getQuantity().unmodifyFlat(id + Commodities.HEAVY_MACHINERY +"_0");
                    industry_heavy.getSupply(Commodities.HEAVY_MACHINERY).getQuantity().unmodifyFlat(id + Commodities.HEAVY_MACHINERY +"_1");
                    industry_heavy.getSupply(Commodities.SUPPLIES).getQuantity().unmodifyFlat(id + Commodities.SUPPLIES +"_0");
                    industry_heavy.getSupply(Commodities.SUPPLIES).getQuantity().unmodifyFlat(id + Commodities.SUPPLIES +"_1");
                    //indEvo integration
                    if(Global.getSettings().getModManager().isModEnabled("indEvo")){
                        industry_heavy.getSupply("IndEvo_parts").getQuantity().unmodifyFlat(id + "IndEvo_parts" +"_0");
                        industry_heavy.getSupply("IndEvo_parts").getQuantity().unmodifyFlat(id + "IndEvo_parts" +"_1");
                    }
                }
            } 
        } 
    }

    public void unapply(String id) {
		super.unapply(id);
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(id + "_quality");
	}

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);

        float pad = 10f;
        Color h = Misc.getHighlightColor();

        for(MarketConditionAPI m : market.getConditions()){
            String commodity = COMMODITY.get(m.getId());
            if(commodity != null){
                if(market.hasIndustry(INDUSTRY.get(commodity))){
                    if(market.getCommodityData(Commodities.ORE) != null) tooltip.addPara("+" + MODIFIER.get(commodity) + " " + Global.getSettings().getCommoditySpec(commodity).getName().toLowerCase() + " production (" + INDUSTRY.get(commodity) + ")", pad, h, "+" + MODIFIER.get(commodity));	
                }
            }
        }

        if(market.hasIndustry(Industries.HEAVYINDUSTRY) || market.hasIndustry(Industries.ORBITALWORKS)){
            IndustrySpecAPI industry_heavy = Global.getSettings().getIndustrySpec(Industries.HEAVYINDUSTRY);
            if (industry_heavy == null) industry_heavy = Global.getSettings().getIndustrySpec(Industries.ORBITALWORKS);

            CommoditySpecAPI spec_machine = Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY);
            CommoditySpecAPI spec_weapon = Global.getSettings().getCommoditySpec(Commodities.HAND_WEAPONS);
            CommoditySpecAPI spec_ships = Global.getSettings().getCommoditySpec(Commodities.SHIPS);

            tooltip.addPara("+" + WORK_MODIFIER + " " + spec_machine.getName().toLowerCase() + " production (" + industry_heavy.getName() + ")", pad, h, "+" + WORK_MODIFIER);	
            tooltip.addPara("+" + WORK_MODIFIER + " " + spec_weapon.getName().toLowerCase() + " production (" + industry_heavy.getName() + ")", pad, h, "+" + WORK_MODIFIER);	
            tooltip.addPara("+" + WORK_MODIFIER + " " + spec_ships.getName().toLowerCase() + " production (" + industry_heavy.getName() + ")", pad, h, "+" + WORK_MODIFIER);	
        
            String bonus = "+" + ((int) (QUALITY_BONUS * 100)) + "%";
            tooltip.addPara("%s ship quality (" + industry_heavy.getName() + ")", pad, h, bonus);
        }
    }
}
