package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

/**
 *
 * @author Tartiflette
 */
public class sikr_pauldron_effect implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false;
    private WeaponAPI reference;
    private ShipAPI ship;
    private float offset = 0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            for(WeaponAPI w : weapon.getShip().getAllWeapons()){
                if(w!=weapon && weapon.getSlot().getId().endsWith("SL") && w.getSlot().getId().endsWith("ML")){
                    reference=w;
                    if(weapon.getShip().getHullSpec().getHullId().equals("sikr_herkir")){
                        offset = -90;
                    }
                    break;
                }
                
                if(w!=weapon && weapon.getSlot().getId().endsWith("SR") && w.getSlot().getId().endsWith("MR")){
                    if(weapon.getShip().getHullSpec().getHullId().equals("sikr_herkir")){
                        offset = +90;
                    }
                    reference=w;
                }
            }
        }
        
        if (engine.isPaused() || reference==null) {
            return;
        }
        
        weapon.setCurrAngle(ship.getFacing() + MathUtils.getShortestRotation(ship.getFacing() + offset,reference.getCurrAngle())*0.6f);
    }
}

