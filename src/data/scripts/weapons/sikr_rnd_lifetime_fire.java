package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;


public class sikr_rnd_lifetime_fire implements OnFireEffectPlugin{

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

		float lifetime_modifier = 1f + 0.15f * (float) Math.random();
        MissileAPI missile = (MissileAPI)projectile;
		missile.setMaxFlightTime(missile.getMaxFlightTime() * lifetime_modifier);

	}
}
