package data.scripts.weapons;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import org.magiclib.util.MagicAnim;

public class sikr_spear_hit implements OnHitEffectPlugin, OnFireEffectPlugin{

    private static final int HITPOINT_COST = 250;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (projectile instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) projectile;
			float flight_time = weapon.getRange() / weapon.getProjectileSpeed() + 0.1f;
			missile.setMaxFlightTime(flight_time);
		}
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldhit,
            ApplyDamageResultAPI damage, CombatEngineAPI engine) {

            MissileAPI missile = (MissileAPI) projectile;

        if(target instanceof ShipAPI && target.getOwner() != projectile.getOwner() && !missile.isExpired()){
            if(shieldhit){
                int hitpoint = (int) (projectile.getHitpoints() - HITPOINT_COST);
                if(hitpoint < HITPOINT_COST) return;

                float shield_angle = VectorUtils.getAngle(point, target.getShield().getLocation());
                float adjusted_angle = MathUtils.getShortestRotation(MathUtils.clampAngle(projectile.getFacing()-180), shield_angle + MathUtils.getRandomNumberInRange(0, 24)-12);
                float angle_var = adjusted_angle < 0 ? -1 : 1;
                float angular_vel = (MagicAnim.normalizeRange(adjusted_angle < 0 ? -adjusted_angle : adjusted_angle, 90, 180) * 1000) + 1 * angle_var;
                Vector2f velocity = VectorUtils.rotate(projectile.getVelocity(), MathUtils.clampAngle(adjusted_angle + MathUtils.getShortestRotation(VectorUtils.getFacing(projectile.getVelocity()), shield_angle))) ;
                velocity.scale(1 + Math.abs(1.5f-(target.getShield().getFluxPerPointOfDamage() * 1)));

                CombatEntityAPI new_proj = engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), "sikr_spear_launcher", point, MathUtils.clampAngle(shield_angle + adjusted_angle + (MathUtils.getRandomNumberInRange(0, 24)-12)), null);
                //engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), "sikr_spear_launcher", projectile.getWeapon().getLocation(), projectile.getWeapon().getArcFacing()+projectile.getSource().getFacing(), null);

                new_proj.setCollisionClass(CollisionClass.FIGHTER);
                new_proj.getVelocity().set(velocity);
                new_proj.setAngularVelocity(angular_vel);
                new_proj.setHitpoints(hitpoint);
                engine.addPlugin(new sikr_spear_plugin2(new_proj, target));
            }
        }
    }
    
}
