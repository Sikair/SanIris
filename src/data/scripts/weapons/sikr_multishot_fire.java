package data.scripts.weapons;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class sikr_multishot_fire implements OnFireEffectPlugin{

    public static int NBR_SHOT = 8;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        int spread = 14;

        if(weapon.getSlot().isHardpoint()){
            spread = 7;
        }

        for (int i = 0; i < NBR_SHOT; i++) {
            float angle = projectile.getFacing() + MathUtils.getRandomNumberInRange(0, spread*2) - spread;
        Vector2f speed_var = new Vector2f(MathUtils.getRandomNumberInRange(-50, 50),MathUtils.getRandomNumberInRange(-50, 50));
        engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), "sikr_multishot_stage2", 
            projectile.getLocation(), angle, speed_var);
        }

        engine.removeEntity(projectile);
        
        
    }
    
}
