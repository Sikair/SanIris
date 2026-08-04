package data.scripts.weapons;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class sikr_herkir_fire implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        if(weapon.getShip().getShipTarget().getHullSize() == HullSize.FIGHTER){
            for(int i = 0; i < 14;i++){
                float angle = projectile.getFacing() + MathUtils.getRandomNumberInRange(0, 10) - 5;
                Vector2f speed_var = new Vector2f(MathUtils.getRandomNumberInRange(-40, 40),MathUtils.getRandomNumberInRange(-40, 40));
                engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), "sikr_herkir_cannon_stage2", 
                projectile.getLocation(), angle, speed_var);
            }

            engine.removeEntity(projectile);
        }
    }
}
