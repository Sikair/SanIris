//Homing part from MagicGuidedProjectileScript by Nicke 535
package data.scripts.weapons;

import java.util.List;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicLensFlare;

import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_supply_plugin2 extends BaseEveryFrameCombatPlugin{

    public static final Object KEY_JITTER = new Object();
    public static final Color JITTER_UNDER_COLOR = new Color(30,180,70,155);
	public static final Color JITTER_COLOR = new Color(30,180,70,55);

    private static final int INTERCEPT_ITERATIONS = 4;
    private static final float SWAY_AMOUNT_PRIMARY = 8f;
	private static final float SWAY_AMOUNT_SECONDARY = 4f;
    private static final float SWAY_PERIOD_PRIMARY = 1.4f;
	private static final float SWAY_PERIOD_SECONDARY = 2f;
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    private static final float TURN_RATE = 480f;
    private static final float INTERCEPT_ACCURACY_FACTOR = 1f;

	private static final float MIN_HP_RECOVER = 200f;
	private static final float MAX_HP_RECOVER = 800f;

    private MissileAPI proj; //The projectile itself
	private ShipAPI target; // Current target of the projectile
	private float swayCounter1; // Counter for handling primary sway
	private float swayCounter2; // Counter for handling secondary sway
	private float lifeCounter; // Keeps track of projectile lifetime
	private float estimateMaxLife; // How long we estimate this projectile should be alive

    public sikr_supply_plugin2(CombatEntityAPI missile, ShipAPI target){
        this.proj = (MissileAPI) missile;
        this.target = target;
		swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
		swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
		lifeCounter = 0f;
        estimateMaxLife = proj.getMaxFlightTime() * 2;
    }
 
    @Override
    public void advance(float amount, List<InputEventAPI> events){
        if (Global.getCombatEngine() == null) {
			return;
		}
        if (Global.getCombatEngine().isPaused()) {
			amount = 0f;
		}
        if (proj == null || proj.isFizzling() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}

		if(!target.isHulk()){

			//homing
			lifeCounter+=amount;
			if (lifeCounter > estimateMaxLife) { lifeCounter = estimateMaxLife; }

			swayCounter1 += amount*SWAY_PERIOD_PRIMARY;
			swayCounter2 += amount*SWAY_PERIOD_SECONDARY;
			float swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
					((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));

			float facingSwayless = proj.getFacing() - swayThisFrame;
			Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(new Vector2f(Misc.ZERO)), target.getFacing());
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(getApproximateInterception(INTERCEPT_ITERATIONS), targetPointRotated, new Vector2f(Misc.ZERO)));
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;

			//ally contact check
			if(CollisionUtils.isPointWithinCollisionCircle(proj.getLocation(), target)){
				//target.getMutableStats().getCombatEngineRepairTimeMult().modifyFlat("sikr_supply", 10);
				float hp_recover = 200;
				if(target.getMaxHitpoints() / 2 > MIN_HP_RECOVER) hp_recover = target.getMaxHitpoints() / 2;
				if(hp_recover > MAX_HP_RECOVER) hp_recover = MAX_HP_RECOVER;

				if(target.getHitpoints() + hp_recover > target.getMaxHitpoints()){
					target.setHitpoints(target.getMaxHitpoints());
				}else{
					target.setHitpoints(target.getHitpoints() + hp_recover);
				}
				MagicLensFlare.createSharpFlare(Global.getCombatEngine(), proj.getSource(), target.getLocation(), 40, 30, 0, new Color(190,255,80,200), new Color(190,255,80,255));
				Global.getCombatEngine().removeEntity(proj);
				Global.getCombatEngine().removePlugin(this);
			}
		}else{
			proj.flameOut();
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