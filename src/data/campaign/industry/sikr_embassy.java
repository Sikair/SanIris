package data.campaign.industry;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sikr_embassy extends BaseIndustry{

    float ACCESSIBILITY = 0.1f;

    public boolean isFunctional() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        //if (ip.getPerson("sikr_lily_yellow") != null);
        return super.isFunctional() && (market.getFaction().getRelationship("sikr_saniris") >= 30 || market.getFactionId().equals("sikr_saniris") && ip.getPerson("sikr_lily_yellow") != null);
	}

    @Override
    public void apply() {
        super.apply(true);
        int size = market.getSize();
        demand(Commodities.MARINES, size-3);
        demand(Commodities.LUXURY_GOODS, size-4);

        String desc = getNameForModifier();
        market.getAccessibilityMod().modifyFlat(getModId(0), ACCESSIBILITY, desc);
    }
    @Override
    public void unapply() {
		super.unapply();
    }
    @Override
	protected int getBaseStabilityMod() {
		return 2;
	}
        @Override
    public boolean isAvailableToBuild() {
        return market.getFaction().getRelationship("sikr_saniris") >= 30;
    }

    @Override
    public String getUnavailableReason() {
        return "Reputation too low";
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		//return mode == IndustryTooltipMode.NORMAL && isFunctional();
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}

    @Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        Color h = Misc.getHighlightColor();
        float opad = 10f;
        if(ACCESSIBILITY > 0){
            tooltip.addPara("Accessibility bonus: %s", opad, h, ACCESSIBILITY+"%");
        }else{
            h = Misc.getNegativeHighlightColor();
            tooltip.addPara("Accessibility penalty: %s", opad, h, ACCESSIBILITY+"%");
        }
        
    }
    
    

}
