package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.listeners.sikr_angle_damage_reduction;
import data.scripts.listeners.sikr_durnir_shield;

public class sikr_mechs extends BaseHullMod {
	
    private final static float EMP_RESIST = 33f;
    private final static float HEALTH_BONUS = 1f;
    public static final float ARMOR_BONUS = 50f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) EMP_RESIST + "%";
        }
        if (index == 1) {
            return "" + (int) (1 + HEALTH_BONUS) + "";
        }
        if (index == 2) {
            return "" + (int) ARMOR_BONUS + "";
        }
        return null;
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getEmpDamageTakenMult().modifyMult(id, (100-EMP_RESIST)/100);
        ship.getMutableStats().getWeaponHealthBonus().modifyMult(id, 1 + HEALTH_BONUS);
        ship.getMutableStats().getEngineHealthBonus().modifyMult(id, 1 + HEALTH_BONUS);
        ship.getMutableStats().getEffectiveArmorBonus().modifyFlat(id, ARMOR_BONUS);

        if(ship.getHullSpec().getHullId().equals("sikr_durnir")){
            if(!ship.hasListenerOfClass(sikr_angle_damage_reduction.class)){
                //ship.addListener(new sikr_angle_damage_reduction(ship, 0.5f, 60, true));

                for(WeaponAPI w : ship.getAllWeapons()){
                    if(w.getSlot().isDecorative() && w.getSlot().getId().equals("ARM_L_SR")){
                        ship.addListener(new sikr_durnir_shield(ship, 60, w, 1000));
                        return;
                    }
                    ship.addListener(new sikr_durnir_shield(ship, 60, null, 1000));
                }
            }
        }   
        // if(ship.getHullSpec().getHullId().equals("sikr_herkir")){
        // }
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return ( ship.getHullSpec().getHullId().startsWith("sikr_"));	
    }
}

