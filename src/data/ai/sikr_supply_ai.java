package data.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_supply_ai implements ShipSystemAIScript{

    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil interval = new IntervalUtil (2.5f,3f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system; 
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        interval.advance(amount);
        if(interval.intervalElapsed()){
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                int max_fighter = 0;
                int supply_demand = 0;
                for(FighterWingAPI w : ship.getAllWings()){
                    for(ShipAPI f : w.getWingMembers()){
                        if(f.isDrone()) continue;
                        max_fighter++;
                        if(f.getHitpoints() < f.getMaxHitpoints()) supply_demand++;
                    }
                }
                if(supply_demand > 0 && max_fighter >= 2){
                    ship.useSystem();;
                }
            }
        }
    }
}