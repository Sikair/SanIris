package data.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_unnr_ai implements ShipSystemAIScript{

    private final float RANGE = 700;
    private final float REAR_ANGLE = 60;

    private ShipAPI ship;
    private ShipSystemAPI system;
    private float ally_protect;
    private boolean activated = false;

    private final IntervalUtil interval = new IntervalUtil (0.5f,1.5f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system; 
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        interval.advance(amount);
        if(interval.intervalElapsed()){
            ally_protect = 0;
            if(!system.isActive() && activated) activated = false;
            boolean threat = false;
            boolean system_on = false;
            for(ShipAPI s : AIUtils.getNearbyEnemies(ship, RANGE * 2)){
                if(s.isFighter()) continue;
                threat = true;
                break;
            }
            if(threat && ship.getCurrFlux() < ship.getMaxFlux() * 0.8f){
                for(ShipAPI ally : AIUtils.getNearbyAllies(ship, RANGE)){
                    if(ally == ship) continue;
                    if(ship.getChildModulesCopy().contains(ally)) continue;
                    float angle = Math.abs(VectorUtils.getAngle(ship.getLocation(), ally.getLocation()) - ship.getFacing());
                    //Global.getCombatEngine().addFloatingText(ally.getLocation(), MathUtils.getDistance(ship, ally) + "", 15, new Color(100f / 255f, 110f / 255f, 100f / 255f, 0.25f), ship, 10, 15); //debug text
                    if(MathUtils.getDistance(ship, ally) < 350){
                        //Global.getCombatEngine().addFloatingText(ally.getLocation(), "close", 15, new Color(100f / 255f, 110f / 255f, 100f / 255f, 0.25f), ship, 10, 15); //debug text
                        if(ally.isFighter()){
                            ally_protect += 0.34f;
                        }else{
                            ally_protect += 2;
                        }
                    }else if(angle > 180 - REAR_ANGLE && angle < 180 + REAR_ANGLE){
                        if(ally.isFighter()) continue;
                        //Global.getCombatEngine().addFloatingText(ally.getLocation(), "behind", 15, new Color(100f / 255f, 110f / 255f, 100f / 255f, 0.25f), ship, 10, 15); //debug text
                        ally_protect++;
                    }
                }
                //Global.getCombatEngine().addFloatingText(ship.getLocation(), ally_protect + "", 15, new Color(100f / 255f, 110f / 255f, 100f / 255f, 0.25f), ship, 10, 15); //debug text
                //float shield_flux = (float) ship.getCustomData().get(ship.getId()+"shield_flux");
                float shield_flux = (float) ship.getCustomData().get(ship.getId()+"shield_flux_remaining");
                if(system.isActive()){
                    if(ally_protect >= 4 && shield_flux < 3000){
                        system_on = true;
                    }else if(shield_flux < 3000){
                        system_on = false;
                    }else if (ally_protect >= 2){
                        system_on = true;
                    }
                }else{
                    if(shield_flux < 4000){
                        system_on = false;
                    }else if(ally_protect >= 2){
                        system_on = true;
                    }else{
                        system_on = false;
                    }
                } 
            }else{
                system_on = false;
            }
            if(system_on && !system.isActive()){
                ship.useSystem();
                activated = true;
            }else if(!system_on && system.isActive()){
                ship.useSystem();
                activated = false;
            }
        }
    }
}
