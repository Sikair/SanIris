package data.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_emp_ai implements ShipSystemAIScript{

    private ShipAPI ship;
    private ShipSystemAPI system;
    private boolean system_on;
    private boolean flux;
    private float missile_range;
    private float ship_range;
    private final IntervalUtil interval = new IntervalUtil (1f,1.5f);
    private float RANGE = 700;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system; 
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        interval.advance(amount);
        if(interval.intervalElapsed()){
            if(flux){
                if(ship.getFluxLevel() <= 0.55f) flux = false;
                return;
            }
            if(!system.isActive() && system_on)system_on = false;
            if(ship.getFluxLevel() <= 0.75f){
                ShipAPI enemy_ship = (ShipAPI) AIUtils.getNearestEnemy(ship);
                CombatEntityAPI enemy_missile = AIUtils.getNearestEnemyMissile(ship);
                if(enemy_missile != null){
                    missile_range = MathUtils.getDistance(enemy_missile,ship);
                }else{
                    missile_range = RANGE+100;
                }
                if(enemy_ship != null && !enemy_ship.isHulk()){
                    ship_range = MathUtils.getDistance(enemy_ship,ship);
                }else{
                    ship_range = RANGE+100;
                }
                if(missile_range<RANGE || ship_range<RANGE){
                    if(!system_on){
                        ship.useSystem();
                        system_on = true;
                    }
                    return;
                }
            }else{
                flux = true;
            }
            if(system_on){
                ship.useSystem();
                system_on = false;
            }
            
        }
    }
}