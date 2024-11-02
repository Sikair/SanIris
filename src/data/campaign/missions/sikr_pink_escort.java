package data.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sikr_pink_escort extends HubMissionWithSearch{

    // mission stages. 
    public static enum Stage {
        ESCORT_PINK,
        COMPLETED
    }
    // important people and planets
    protected PersonAPI pink;
    protected MarketAPI lily;

    @Override
    protected boolean create(MarketAPI createdAt, boolean event) {
        pink = getImportantPerson("sikr_lily_pink");
        lily = getMarket("sikr_lily_market");

        if(pink == null){
            return false;
        }

        if(lily == null){
            return false;
        }

        if (!setGlobalReference("$sikr_PE_ref")) {
			return false;
		}

        setName("Primrose Escort");

        setStartingStage(Stage.ESCORT_PINK);
        addSuccessStages(Stage.COMPLETED);

        //setStoryMission();
        
        setStageOnGlobalFlag(Stage.ESCORT_PINK, "$sikr_PE");
        setStageOnGlobalFlag(Stage.COMPLETED, "$sikr_PE_completed");
        
        beginStageTrigger(Stage.ESCORT_PINK);
        makeImportant(lily, "$sikr_PE", Stage.ESCORT_PINK);
        triggerSetGlobalMemoryValue("$sikr_PE", true);
        endTrigger();

        beginWithinHyperspaceRangeTrigger(createdAt, 1f, true, Stage.ESCORT_PINK);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.SMOD_1, 
						Factions.TRITACHYON, FleetTypes.MERC_PATROL, createdAt.getStarSystem());
		triggerSetFleetOfficers(OfficerNum.DEFAULT, OfficerQuality.LOWER);
		triggerMakeHostileAndAggressive();
		//triggerMakeNoRepImpact(); // this happens in dialog instead
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerSetPatrol();
        triggerMakeFleetIgnoreOtherFleets();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerPickLocationTowardsEntity(lily.getStarSystem().getHyperspaceAnchor(), 90f, getUnits(1.5f));
		triggerSpawnFleetAtPickedLocation("$sikr_PE_patrol", null);
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetEBurn(1f);
		triggerFleetMakeImportant(null, Stage.ESCORT_PINK);
		endTrigger();
        
        beginStageTrigger(Stage.COMPLETED);
        triggerSetGlobalMemoryValue("$sikr_PE_completed", true);
        endTrigger();

        setRepFactionChangesNone();
        //setRepPersonChangesNone();
        setRepRewardPerson(20f);
        setCreditReward(50000);
        
        return true;
        //return false;
    }
    
    protected void updateInteractionDataImpl() {
        set("$sikr_pink_escort_dist", getDistanceLY(lily));
        set("$sikr_pink_escort_reward", Misc.getWithDGS(getCreditsReward()));
        set("$sikr_pink_escort_dist_marketName", lily.getName());
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height){
        float opad = 10f;
        if(currentStage == Stage.ESCORT_PINK){
            info.addPara("Drop " + pink.getNameString() + " on Lily.", opad);
        }

    }

    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad){
        return false;
    }

    @Override
    public String getBaseName() {
        return "Take Primrose to Lily";
    }

    @Override
    public String getPostfixForState() {
        if (startingStage != null) {
            return "";
        }
        return super.getPostfixForState();
    }
}