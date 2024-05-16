//Homing part from MagicGuidedProjectileScript by Nicke 535
package data.scripts.weapons;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_spear_plugin extends BaseEveryFrameCombatPlugin{

    private static final float TURN_RATE = 380f;
    private static final float INTERCEPT_ACCURACY_FACTOR = 1f;
    private static final float ONE_TURN_TARGET_INACCURACY = 5f;

    private MissileAPI proj; //The projectile itself
	private CombatEntityAPI target; // Current target of the projectile
    private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB

    private boolean run_once = false;

    public sikr_spear_plugin(CombatEntityAPI missile, CombatEntityAPI target){
        this.proj = (MissileAPI) missile;
        this.target = target;
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
        if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}

        if(proj.getFlightTime() > 1f){
            if(!run_once){
                run_once = true; 
                targetPoint = MathUtils.getRandomPointInCircle(getApproximateInterception(15), ONE_TURN_TARGET_INACCURACY);
                proj.setCollisionClass(CollisionClass.MISSILE_FF);
                //proj.setAngularVelocity(0);
            }
            //proj.setAngularVelocity(proj.getAngularVelocity()*0.98f);
            //proj.getVelocity().scale(0.98f);

            float facingSwayless = proj.getFacing();
            float angleToHit = VectorUtils.getAngle(proj.getLocation(), targetPoint);
            float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
            while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
            facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount); 
            if(angleDiffAbsolute < 3){ 
                //MagicRender.objectspace(thruster_sprite, proj, new Vector2f(14,0), new Vector2f(), new Vector2f(8,24), new Vector2f(), 90, 0, true, new Color(255,125,25,255), false, 0.1f, 2, 0.1f, true);
                //MagicRender.objectspace(thruster_sprite, proj, new Vector2f(14,0), new Vector2f(), new Vector2f(8,24), new Vector2f(), 270, 0, true, new Color(255,125,25,255), false, 0.1f, 2, 0.1f, true);
                proj.setFacing(facingSwayless); 
                proj.setAngularVelocity(0);          
                proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless).x;
                proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless).y;
            }else{
                proj.getVelocity().scale(0.97f);
            }
            
        }else{
            proj.setAngularVelocity(proj.getAngularVelocity()*0.99f); 
            proj.getVelocity().scale(0.98f);
        }  
    }

    private Vector2f getApproximateInterception(int calculationSteps) {
		Vector2f returnPoint = new Vector2f(target.getLocation());

		//Iterate a set amount of times, improving accuracy each time
		for (int i = 0; i < calculationSteps; i++) {
			//Get the distance from the current iteration point and the projectile, and calculate the approximate arrival time
			float arrivalTime = MathUtils.getDistance(proj.getLocation(), returnPoint)/proj.getVelocity().length();

			//Calculate the targeted point with this arrival time
			returnPoint.x = target.getLocation().x + (target.getVelocity().x * arrivalTime * INTERCEPT_ACCURACY_FACTOR);
			returnPoint.y = target.getLocation().y + (target.getVelocity().y * arrivalTime * INTERCEPT_ACCURACY_FACTOR);
		}

		return returnPoint;
	}
}