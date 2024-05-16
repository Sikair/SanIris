package data.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import org.lwjgl.util.vector.Vector2f;

public class sikr_damper_module_ai implements ShipSystemAIScript{

    private ShipAPI parent_ship;
    private ShipAPI ship;
    private boolean runOnce = true;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if(runOnce){
            parent_ship = ship.getParentStation();
            runOnce = false;
        }
        if(parent_ship.getSystem().isActive()){
            ship.useSystem();
        }
        if(!ship.getFluxTracker().isOverloaded() && parent_ship.getFluxTracker().isOverloaded()){
            ship.getFluxTracker().forceOverload(parent_ship.getFluxTracker().getOverloadTimeRemaining());
        }
        
    }
}
