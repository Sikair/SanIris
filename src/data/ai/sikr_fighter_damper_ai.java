package data.ai;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_fighter_damper_ai implements ShipSystemAIScript{

    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil interval = new IntervalUtil (1.5f,2f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system; 
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        interval.advance(amount);
        if(interval.intervalElapsed()){
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                List<ShipAPI> fighters = null;
			    fighters = getFighters(ship);
			    if (fighters == null) { // shouldn't be possible, but still
				    fighters = new ArrayList<ShipAPI>();
			    }
                int threat_level = 0;
                int nbr_fighter = fighters.size();
                for(ShipAPI fighter : fighters){
                    if(fighter.areAnyEnemiesInRange()) threat_level++;
                }
                if(threat_level > nbr_fighter * 0.33){
                    ship.useSystem();
                }
            }
        }
    }

    public static List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
	}
}
