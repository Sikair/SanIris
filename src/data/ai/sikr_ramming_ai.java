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

public class sikr_ramming_ai implements ShipSystemAIScript{

    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil interval = new IntervalUtil (1.5f,2f);
    private float RANGE = 1200;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system; 
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        interval.advance(amount);
        if(interval.intervalElapsed()){
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                if(target != null){
                    if(target.isCapital() || target.isCruiser() || target.isDestroyer()){
                        //Global.getCombatEngine().addFloatingText(target.getLocation(),  MathUtils.getDistance(ship, target) + "", 15, new Color(100f / 255f, 110f / 255f, 100f / 255f, 0.25f), ship, 10, 15); //debug text
                        if(MathUtils.getDistance(ship, target) < 800){
                            float trajectory_angle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
                            float angle = ship.getFacing();
                            if(angle < trajectory_angle+1 && angle > trajectory_angle-1){
                                int ship_danger = 0; 
                                int target_danger = 0;
                                for(ShipAPI s : AIUtils.getNearbyEnemies(ship, RANGE)){
                                    if(s == target) continue;
                                    if(s.isCapital()){
                                        ship_danger += 6;
                                    }else if(s.isCruiser()){
                                        ship_danger += 4;
                                    }else if(s.isDestroyer()){
                                        ship_danger += 2;
                                    }else if(s.isFrigate()){
                                        ship_danger += 1;
                                    }
                                }
                                for(ShipAPI s :AIUtils.getNearbyAllies(target, RANGE)){
                                    if(s == target) continue;
                                    if(s.isCapital()){
                                        target_danger += 6;
                                    }else if(s.isCruiser()){
                                        target_danger += 4;
                                    }else if(s.isDestroyer()){
                                        target_danger += 2;
                                    }else if(s.isFrigate()){
                                        target_danger += 1;
                                    }
                                }
                                if(ship_danger > target_danger+1 || target_danger <= 6 ) ship.useSystem();
                            }
                        }
                    }
                    
                }
            }
        }
        
    }
  
}
