package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import org.lazywizard.lazylib.MathUtils;

import org.magiclib.util.MagicAnim;

public class sikr_unnr_armor_plugin implements EveryFrameWeaponEffectPlugin{

    private boolean runOnce = false;
    private ShipAPI ship;
    private ShipAPI parent;
    private boolean active = false;
    private float var;
    private float range = 0;
    
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if(!runOnce){
            ship = weapon.getShip();
            parent = weapon.getShip().getParentStation();
            ship.getMutableStats().getVentRateMult().modifyMult(ship.getId() + "no_vent", 0);
            if (parent == null) return;
            if(weapon.getSlot().getId().contains("LOGIL")){ //left logic weapon slot
                var = 45f;
            }else{
                var = -45f;
            } 
            runOnce = true;
        }else{
            if(engine.isPaused() || ship == null) return;
    
            if((parent.getSystem().isOn()) && range < 1.2){
                range += 0.02;    
                ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyFlat("UnnrArmor"+var, 20000f);
                ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyFlat("UnnrArmor"+var, 20000f);
                ship.getMutableStats().getMissileWeaponFluxCostMod().modifyFlat("UnnrArmor"+var, 20000f);
            } else if (!active && range > 0){
                range -= 0.02;    
            } else if (range <= 0){
                ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify("UnnrArmor"+var);
                ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify("UnnrArmor"+var);
                ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify("UnnrArmor"+var);
            }
            if(range != 0) ship.setFacing(ship.getFacing() + (MathUtils.getShortestRotation(ship.getFacing(), parent.getFacing()+var)*MagicAnim.smooth(range))); 
        }

        
            
    }

}
