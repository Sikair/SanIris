package data.scripts.weapons;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

public class sikr_emp_plugin implements EveryFrameWeaponEffectPlugin{

    private static final int EMP_DAMAGE = 250;
    private static final int DAMAGE = 150;

    private boolean runOnce=false;
    private ShipAPI ship;
    private List<WeaponSlotAPI> sys_slot = new ArrayList<>();
    private final IntervalUtil interval = new IntervalUtil (0.2f,0.3f);
    private float RANGE = 600;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if(!runOnce){
            runOnce = true;
            ship = weapon.getShip();
            for(int i=0;i<5;i++){
                sys_slot.add(ship.getVariant().getSlot("SYSL"+i));
                sys_slot.add(ship.getVariant().getSlot("SYSR"+i));
            }
            
        }

        if(ship.getSystem().isActive()){
            interval.advance(amount);
            if(interval.intervalElapsed()){
                WeaponSlotAPI active_slot = sys_slot.get(MathUtils.getRandomNumberInRange(0, sys_slot.size()-1));
                MissileAPI missile = AIUtils.getNearestEnemyMissile(ship);
                CombatEntityAPI target;
                if(missile != null && MathUtils.getDistance(missile, active_slot.computePosition(ship))<RANGE){
                    target = missile; 
                }else{
                    CombatEntityAPI enemy_ship = AIUtils.getNearestEnemy(ship);
                    if(enemy_ship != null && MathUtils.getDistance(enemy_ship, active_slot.computePosition(ship))<RANGE){
                        target=enemy_ship;
                    }else{
                        target = new SimpleEntity(MathUtils.getRandomPointInCone(active_slot.computePosition(ship), RANGE-300, active_slot.getAngle()-30, active_slot.getAngle()+30));
                    }
                }
                engine.spawnEmpArc(ship,active_slot.computePosition(ship) , ship, target, DamageType.ENERGY, DAMAGE, EMP_DAMAGE, RANGE+100, null, 20, new Color(195,45,215,255), new Color(240,115,255,255));
            }
        }
        
    }
    
}
