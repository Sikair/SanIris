package data.campaign.events;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.ColonySizeChangeListener;
import com.fs.starfarer.api.impl.campaign.NPCHassler;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FGBlockadeAction.FGBlockadeParams;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.intel.group.KnightsOfLuddTakeoverExpedition;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class sikr_SanIrisHostileActivityFactor extends BaseHostileActivityFactor implements
						FGIEventListener, ColonyPlayerHostileActListener, ColonySizeChangeListener {

    public static final String SAN_IRIS = "sikr_saniris";

	public static final String HASSLE_REASON = "sikr_oversee";
	
	// in $player memory
	public static final String DEFEATED_SAN_IRIS_EXPEDITION = "$sikr_defeatedSanIrisExpedition";
	public static final String MADE_OVERSEE_DEAL_WITH_SAN_IRIS = "$sikr_madeOverseeDealWithSanIris";
	public static final String BROKE_OVERSEE_DEAL_WITH_SAN_IRIS = "$sikr_brokeOverseeDealWithSanIris";
	
	public static boolean isDefeatedExpedition() {
		//if (true) return true;
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_SAN_IRIS_EXPEDITION);
	}
	public static void setDefeatedExpedition(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_SAN_IRIS_EXPEDITION, value);
	}
	
	public static boolean isMadeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(MADE_OVERSEE_DEAL_WITH_SAN_IRIS);
	}
	public static void setMadeDeal(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(MADE_OVERSEE_DEAL_WITH_SAN_IRIS, value);
	}
	
	public static boolean brokeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(BROKE_OVERSEE_DEAL_WITH_SAN_IRIS);
	}

	public static void setBrokeDeal(boolean broke) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(BROKE_OVERSEE_DEAL_WITH_SAN_IRIS, broke);
	}
	
	// main code
	public sikr_SanIrisHostileActivityFactor(HostileActivityEventIntel intel) {
		super(intel);
		
		Global.getSector().getListenerManager().addListener(this);
	}
	
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		if (!checkFactionExists(SAN_IRIS, true)) {
			return 0;
		}
		return super.getProgress(intel);
	}
	
	public String getDesc(BaseEventIntel intel) {
		return "San Iris";
	}
	
	public String getNameForThreatList(boolean first) {
		return "San Iris";
	}

	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(SAN_IRIS).getBaseUIColor();
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				//float opad = 10f;
				tooltip.addPara("A newly colonised Water World "
						+ "not under their control, and of inherent holy nature - "
						+ "is a small but growing source of concern to San Iris. "
						+ "\"Overseering\" fleets can be found in your systems, "
						+ "enforcing strict guideline on how to exploit the Water World.", 0f);
			}
		};
	}

	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) > 0;
	}

	public Color getNameColor(float mag) {
		if (mag <= 0f) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getFaction(SAN_IRIS).getBaseUIColor();
	}
	
	@Override
	public int getMaxNumFleets(StarSystemAPI system) {
		return 1; //TODO
	}
	
	@Override
	public float getSpawnInHyperProbability(StarSystemAPI system) {
		return 0f;
	}
	
	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
	
		//float f = intel.getMarketPresenceFactor(system);
		
		int maxSize = 0;
		for (MarketAPI curr : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			maxSize = Math.max(curr.getSize(), maxSize);
		}
		
		//int difficulty = 0 + (int) Math.max(1f, Math.round(f * 6f));
		int difficulty = maxSize + 1;
		difficulty += random.nextInt(4);
		if (difficulty > 10) difficulty = 10;
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = SAN_IRIS;
		
		m.createStandardFleet(difficulty, factionId, loc);
		m.triggerSetFleetQuality(FleetQuality.HIGHER);
		m.triggerSetFleetType(FleetTypes.TASK_FORCE);
		m.triggerSetPatrol();
		//m.triggerSetFleetHasslePlayer(HASSLE_REASON);
		m.triggerSetFleetFlag("$sikr_holy_knight");
		
		//m.triggerFleetAllowLongPursuit();
		m.triggerMakeLowRepImpact();
		
		//m.triggerMakeHostile();
		//m.triggerMakeHostileWhileTransponderOff();
		
		CampaignFleetAPI fleet = m.createFleet();
		
		if (fleet != null) {
			fleet.setName("San Iris Overseer " + fleet.getName());
			fleet.setNoFactionInName(true);
			NPCHassler hassle = new NPCHassler(fleet, system);
			hassle.getParams().crDamageMult = 0f; 
			fleet.addScript(hassle);
		}

		return fleet;
	}
	
	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color c = Global.getSector().getFaction(SAN_IRIS).getBaseUIColor();
		info.addPara("Impending San Iris overseering expedition", initPad, tc, c, "San Iris");
	}
	
	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		info.addPara("San Iris overseering averted", tc, initPad);
	}
	
	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		LabelAPI label = info.addPara("You've received intel that the San Iris federation "
				+ "are planning to force a deal, where they will permanently oversee one of your colonies, "
				+ "on the grounds that you are mismanaging what they qualify as \"Sacred Water World\".",
				small, Misc.getNegativeHighlightColor(), "oversee one of your colonies");
		label.setHighlight("oversee one of your colonies", "Sacred Water World");
		label.setHighlightColors(Misc.getNegativeHighlightColor(),
				Global.getSector().getFaction(SAN_IRIS).getBaseUIColor());
		
		label = info.addPara("If the expedition is defeated, you can expect a small immigration "
				+ "coming from San-Iris population, potentially bringing their skills in "
				+ "operating on a water world.", 
				opad);
		label.setHighlight("small immigration", "operating on a water world");
		label.setHighlightColors(Misc.getPositiveHighlightColor(),
				Global.getSector().getFaction(SAN_IRIS).getBaseUIColor());
		
		Color c = Global.getSector().getFaction(SAN_IRIS).getBaseUIColor();
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("You make an agreement with San Iris", 0f);
		info.addPara("%s is tactically bombarded", 0f, c, "Iris");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction(SAN_IRIS).getBaseUIColor());
	}
	
	
	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return Global.getSector().getFaction(SAN_IRIS).getCrest();
	}

	public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("San Iris expedition", intel, stage);
		}
		return null;
	}
	
	
	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			if (isDefeatedExpedition() || getIris(true) == null) {
				return 0f;
			}
			
			if (KnightsOfLuddTakeoverExpedition.get() != null) { //TODO
				return 0f;
			}
			
			MarketAPI target = findExpeditionTarget(intel, stage);
			MarketAPI source = getExpeditionSource(intel, stage, target);
			if (target != null && source != null) {
				return 20f;
			}
		}
		return 0;
	}
	

	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		HAERandomEventData data = new HAERandomEventData(this, stage);
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		MarketAPI target = findExpeditionTarget(intel, stage);
		MarketAPI source = getExpeditionSource(intel, stage, target);
		if (source == null || target == null) {
			return false;
		}
	
		stage.rollData = null;
		return startExpedition(source, target, stage, getRandomizedStageRandom(3));
	}
	
	
	public MarketAPI findExpeditionTarget(HostileActivityEventIntel intel, EventStageData stage) {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom());
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			if (market.getStarSystem() == null) continue;
			if (market.hasCondition(Conditions.WATER_SURFACE)) {
				float size = market.getSize();
				float w = Math.max(size - 3f, 1f);
				w = w * w * w;
				picker.add(market, w);
			}
		}
		return picker.pick();
	}
	
	public MarketAPI getExpeditionSource(HostileActivityEventIntel intel, EventStageData stage, final MarketAPI target) {
		return getIris(true);
	}
	
	public static MarketAPI getIris(boolean requireMilitaryBase) {
		MarketAPI iris = Global.getSector().getEconomy().getMarket("sikr_iris"); //TODO
		if (iris == null || iris.hasCondition(Conditions.DECIVILIZED)) {
			return null;
		}
		if (requireMilitaryBase) {
			Industry b = iris.getIndustry(Industries.MILITARYBASE);
			if (b == null) b = iris.getIndustry(Industries.HIGHCOMMAND);
			if (b == null || b.isDisrupted() || !b.isFunctional()) {
				return null;
			}
		}
		return iris;
	}
	
	
	public boolean startExpedition(MarketAPI source, MarketAPI target, EventStageData stage, Random random) {

		GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
		params.factionId = source.getFactionId();
		params.source = source;
		
		params.prepDays = 7f + random.nextFloat() * 14f;
		params.payloadDays = 180f;
		
		params.makeFleetsHostile = false;
		
		FGBlockadeParams bParams = new FGBlockadeParams();
		bParams.where = target.getStarSystem();
		bParams.targetFaction = Factions.PLAYER;
		bParams.specificMarket = target;
		
		params.noun = "enforcer";
		params.forcesNoun = "San Iris forces";
		
		params.style = FleetStyle.QUALITY;
		
		
		params.fleetSizes.add(10); // first size 10 pick becomes the Armada
		
		// and a few smaller picket forces
		params.fleetSizes.add(4);
		params.fleetSizes.add(4);
		params.fleetSizes.add(3);
		params.fleetSizes.add(3);

		
		KnightsOfLuddTakeoverExpedition blockade = new KnightsOfLuddTakeoverExpedition(params, bParams); //TODO
		blockade.setListener(this);
		Global.getSector().getIntelManager().addIntel(blockade);
		
		return true;
	}
	
	public void reportFGIAborted(FleetGroupIntel intel) {
		setDefeatedExpedition(true);
	}
	
	@Override
	public void notifyFactorRemoved() {
		Global.getSector().getListenerManager().removeListener(this);
	}

	public void notifyEventEnding() {
		notifyFactorRemoved();
	}
	
	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market,
			TempData actionData, CargoAPI cargo) {
	}

	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
	}

	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
	}

	public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == this) {
			MarketAPI iris = getIris(true);
			
			if (iris == null) {
				intel.resetHA_EVENT();
			}			
		}
	}

	@Override
	public void reportColonySizeChanged(MarketAPI arg0, int arg1) {
	}
}