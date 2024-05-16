package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicIncompatibleHullmods;

public class sikr_aegirran_manufacturing extends BaseHullMod{

    public static final float PROFILE_MULT = 0.7f;
	public static final float HEALTH_BONUS = 0.5f;
    public static final float ARMOR_BONUS = 100f;
    public static final float HULL_WEAKNESS = 0.2f;

    public static final String HULL_KEY = "_sikr_hull_tracker";

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEngineHealthBonus().modifyMult(id, 1 + HEALTH_BONUS);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
        stats.getHullDamageTakenMult().modifyMult(id, 1 + HULL_WEAKNESS);
        stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_BONUS);


        if(stats.getVariant().getHullMods().contains("insulatedengine")){
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                stats.getVariant(),
                "insulatedengine",
                "sikr_aegirran"
                );
        }
        
    }
    
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(!ship.isAlive()) return;
        // boolean has_listener = ship.hasListenerOfClass(sikr_wound_effect.class); 
        // if(!has_listener) ship.addListener(new sikr_wound_effect());
        // if(ship.getFluxTracker().isOverloaded()){
        //    if(ship.getCurrFlux() < ship.getMaxFlux()*0.2f){
        //        ship.getFluxTracker().stopOverload();
        //    }
        // }
        String KEY = ship.getId() + HULL_KEY;
        if(ship.getCustomData().containsKey(KEY)){
            float previous_hp = (float) ship.getCustomData().get(KEY);
            if(previous_hp > ship.getHitpoints()){         
                float flux = ship.getCurrFlux()-Math.round(previous_hp - ship.getHitpoints());
                if(ship.getFluxTracker().getHardFlux()>flux)ship.getFluxTracker().setHardFlux(flux);
                ship.getFluxTracker().setCurrFlux(flux);

                ship.setCustomData(KEY, ship.getHitpoints());
            }
        }else{
            ship.setCustomData(KEY, ship.getHitpoints());
        }

        float flux_dissip = ship.getHitpoints() / ship.getMaxHitpoints() + 0.5f;
        ship.getMutableStats().getFluxDissipation().modifyMult(ship.getId()+"_sikr_manu", flux_dissip);

        if(ship == Global.getCombatEngine().getPlayerShip()){
            Global.getCombatEngine().maintainStatusForPlayerShip("Aegirràn", "graphics/hullmods/flux_coil_adjunct.png", "Flux Dissipation",
            Math.round(flux_dissip*100) + " %", false);
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int) HEALTH_BONUS + "%";
		//if (index == 1) return "" + (int) ((1f - PROFILE_MULT) * 100f) + "%";
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

        tooltip.addSectionHeading("Flux system", Alignment.MID, opad);
        LabelAPI label = tooltip.addPara("Specialized flux circulatory system hidden under thick plate of armor results in :"
                + "\n\nAdding %s armor for damage calculation."
                + "\nIncreased flux dissipation by %s down to %s of base stat, reduced as hull point decrease."
                + "\nDamage received to hull reduce current flux for the same amount."
                + "\nHull take %s more damage as result of the complex system.", opad, h,
                (int) ARMOR_BONUS+"", 50 +"%", "-" + 50 + "%", (int)Math.round(HULL_WEAKNESS * 100f) + "%");
        label.setHighlight((int) ARMOR_BONUS+"", 50 +"%", "-" + 50 + "%", (int)Math.round(HULL_WEAKNESS * 100f) + "%");
        label.setHighlightColors(h, h, Misc.getNegativeHighlightColor(), h);

		tooltip.addSectionHeading("Thruster", Alignment.MID, opad);
		label = tooltip.addPara("Uncommon propulsion system with :"
                + "\n\nIncreased engines durability by %s,"
                + "\nDecreased sensor profil by %s."
                + "\n\nIncompatible with Insulated Engine Assembly ", opad, h,
                (int)Math.round((1f - HEALTH_BONUS) * 100f) + "%", (int)Math.round((1f - PROFILE_MULT) * 100f) + "%");
		label.setHighlight( (int)Math.round((1f - HEALTH_BONUS) * 100f) + "%", (int)Math.round((1f - PROFILE_MULT) * 100f) + "%");
		label.setHighlightColors(h, h);
	}
  
}

