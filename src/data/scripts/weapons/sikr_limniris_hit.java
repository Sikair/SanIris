package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class sikr_limniris_hit implements OnHitEffectPlugin {

    /** Color of the initial burst at the hit point. */
    private static final Color BURST_OUTER = new Color(190, 80, 255, 255);
    private static final Color BURST_INNER = new Color(240, 200, 255, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point,
                      boolean shieldHit, ApplyDamageResultAPI damage, CombatEngineAPI engine) {

        WeaponAPI weapon = projectile.getWeapon();
        if (weapon == null) return;

        // Burst particles at the impact point
        engine.addHitParticle(point, new Vector2f(0f, 0f), 22f, 1f, 0.20f, BURST_OUTER);
        engine.addHitParticle(point, new Vector2f(0f, 0f), 10f, 1f, 0.30f, BURST_INNER);

        // Compute the bounce direction: perpendicular to the projectile's travel direction,
        // randomized left or right, with ±35° of angular jitter for variety.
        float facingRad = (float) Math.toRadians(projectile.getFacing());
        // Perpendicular to facing: facing ± 90°
        float side       = Math.random() > 0.5 ? 1f : -1f;
        float jitterRad  = (float) ((Math.random() - 0.5) * Math.toRadians(70.0)); // ±35°
        float bounceAngle = facingRad + side * (float) Math.toRadians(90.0) + jitterRad;

        float bounceDirX = (float) Math.cos(bounceAngle);
        float bounceDirY = (float) Math.sin(bounceAngle);

        // Energy orb: launches sideways, decelerates to a stop, then homes to the weapon
        engine.addLayeredRenderingPlugin(
                new sikr_limniris_orb(weapon, point, bounceDirX, bounceDirY));
    }
}
