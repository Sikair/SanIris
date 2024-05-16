package data.scripts.weapons;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.input.InputEventAPI;

public class sikr_spear_plugin2 extends BaseEveryFrameCombatPlugin{

    private MissileAPI proj; //The projectile itself

    private boolean run_once = false;

    public sikr_spear_plugin2(CombatEntityAPI missile, CombatEntityAPI target){
        this.proj = (MissileAPI) missile;
    }
 
    @Override
    public void advance(float amount, List<InputEventAPI> events){
        if (Global.getCombatEngine() == null) {
			return;
		}
        if (Global.getCombatEngine().isPaused()) {
			//amount = 0f;
            return;
		}
        if (proj.getFlightTime() > 2 || proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}

        if(proj.getFlightTime() > 0.33f){
            if(!run_once){
                run_once = true; 
                proj.setCollisionClass(CollisionClass.MISSILE_FF);
                //proj.setAngularVelocity(0);
            }
            proj.getVelocity().scale(0.96f);
            
        }else{
            proj.setAngularVelocity(proj.getAngularVelocity()*0.99f); 
            proj.getVelocity().scale(0.98f);
        }  
    }
}