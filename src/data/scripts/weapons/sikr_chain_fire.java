package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class sikr_chain_fire implements OnFireEffectPlugin{

    private int shot_counter = 0;

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		shot_counter++;
        if(shot_counter >= 5){
            shot_counter = 0;

            engine.spawnProjectile(
                    weapon.getShip(),
                    weapon,
                    "sikr_chain1",
                    projectile.getLocation(),
                    projectile.getFacing(),
                    weapon.getShip().getVelocity());
            engine.removeEntity(projectile);
        }
	}
}
