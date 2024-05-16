//Homing part from MagicGuidedProjectileScript by Nicke 535
package data.scripts.weapons;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

public class sikr_supply_plugin extends BaseEveryFrameCombatPlugin {

    private static final float SWAY_AMOUNT_PRIMARY = 4f;
	private static final float SWAY_AMOUNT_SECONDARY = 2f;
    private static final float SWAY_PERIOD_PRIMARY = 1.4f;
	private static final float SWAY_PERIOD_SECONDARY = 2f;
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    private static final float TURN_RATE = 80f;

    private MissileAPI proj; //The projectile itself
    private float delayCounter; // Counter for delaying targeting
    private float actualGuidanceDelay; // The actual guidance delay for this specific projectile
    private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB
    private float swayCounter1; // Counter for handling primary sway
	private float swayCounter2; // Counter for handling secondary sway
	private float lifeCounter; // Keeps track of projectile lifetime
	private float estimateMaxLife; // How long we estimate this projectile should be alive
    private Vector2f targeted_point;

    public sikr_supply_plugin(CombatEntityAPI missile, Vector2f target_point){
        this.proj = (MissileAPI) missile;
        this.targeted_point = target_point;
        delayCounter = 0f;
        actualGuidanceDelay = 1;
        targetPoint = MathUtils.getRandomPointInCircle(target_point, 10);
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

        //Global.getCombatEngine().addFloatingText(targeted_point,  "target", 15, new Color(100f / 255f, 110f / 255f, 100f / 255f, 0.25f), proj.getSource(), 10, 15); //debug text

        //Delays targeting if we have that enabled
		if (delayCounter < actualGuidanceDelay) {
			delayCounter+=amount;
			return;
		}

        //Tick the sway counter up here regardless of if we need it or not: helps reduce boilerplate code
		swayCounter1 += amount*SWAY_PERIOD_PRIMARY;
		swayCounter2 += amount*SWAY_PERIOD_SECONDARY;
		float swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
				((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));

        float facingSwayless = proj.getFacing() - swayThisFrame;
        float angleToHit = VectorUtils.getAngle(proj.getLocation(), targetPoint);
        float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
        while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
        facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
        proj.setFacing(facingSwayless + swayThisFrame);
        proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
        proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;

        //split check
        if(MathUtils.getDistance(proj.getLocation(), targeted_point) <= 40){
            ShipAPI ship = proj.getSource();
            CombatEngineAPI engine = Global.getCombatEngine();
            for(FighterWingAPI w : ship.getAllWings()){
                for(ShipAPI f : w.getWingMembers()){
                    if(f.getHullSpec().getMinCrew() == 0) continue;
                    CombatEntityAPI missile = engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "sikr_supply_system2", 
                    proj.getLocation(), proj.getFacing()+MathUtils.getRandomNumberInRange(0, 10)-5, proj.getSource().getVelocity());
                    missile.setCollisionClass(CollisionClass.FIGHTER);
                    engine.addPlugin(new sikr_supply_plugin2(missile, f));
                }
            }
            engine.spawnExplosion(proj.getLocation(), new Vector2f(), new Color(245,180,40,255), 56, 1f);
            engine.removeEntity(proj);
            engine.removePlugin(this);
        }   
    }
}
