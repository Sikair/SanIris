package data.scripts.listeners;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.util.Misc;

public class sikr_durnir_shield implements DamageTakenModifier {

    private final ShipAPI fighter;
    private final float frontArc;           // degrees from center (e.g., 60° = 120° total arc)

    private WeaponAPI shieldDeco;
    private float shieldHp;

    public sikr_durnir_shield(ShipAPI fighter, float frontArc, WeaponAPI shieldDeco, float shieldHp) {
        this.fighter = fighter;
        this.frontArc = frontArc;

        this.shieldDeco = shieldDeco;
        this.shieldHp = shieldHp;
    }

    @Override
    public String modifyDamageTaken(Object param,
                                    CombatEntityAPI target,
                                    DamageAPI damage,
                                    Vector2f point,
                                    boolean shieldHit) {

        if (shieldHp <= 0f) return null;     // shield depleted
        if (shieldHit || point == null) return null;

        float angleToHit = Misc.getAngleInDegrees(fighter.getLocation(), point);
        float relativeAngle = Math.abs(MathUtils.getShortestRotation(fighter.getFacing(), angleToHit));

        if (relativeAngle > frontArc) return null; // not in front arc

        float originalDamage = damage.getDamage();

        if (shieldHp > originalDamage){
            damage.getModifier().modifyMult("front_shield", 0f);
            shieldHp -= originalDamage;
        }else{
            float mult = 1 - (shieldHp / originalDamage);
            damage.getModifier().modifyMult("front_shield", mult);
            shieldHp = 0;
        }

        if (originalDamage > 50f){
            float intensity = Math.min(originalDamage / 300f, 1f);  
            // scales 0 → 1 based on damage absorbed (200 dmg = full flash)
    
            // Color scales with intensity
            Color flashColor = new Color(
                    (int)(150 * intensity),
                    (int)(200 * intensity),
                    (int)(255 * intensity),
                    255
            );
    
            // Spawn flash at hit point
            Global.getCombatEngine().addHitParticle(
                    point,
                    fighter.getVelocity(),
                    60f + 120f * intensity,   // size scales with damage
                    1f,
                    0.15f + 0.25f * intensity, // duration scales
                    flashColor
            );
    
            // Add a softer glow
            Global.getCombatEngine().addSmoothParticle(
                    point,
                    fighter.getVelocity(),
                    80f + 200f * intensity,
                    0.8f,
                    0.4f,
                    flashColor
            );
        }

        if (shieldHp <= 0f) {
            shieldHp = 0f;
            
            if (shieldDeco != null) {
                shieldDeco.getAnimation().setFrame(1);
                shieldDeco.setCurrAngle(fighter.getFacing()+70);

                Vector2f left = Misc.getUnitVectorAtDegreeAngle(fighter.getFacing() + 90f);
                left.scale(100f); 

                MagicRender.battlespace(
                Global.getSettings().getSprite("fx", "sikr_durnir_shield"),
                shieldDeco.getLocation(),
                Vector2f.add(
                    fighter.getVelocity(),
                    left,
                    new Vector2f()
                ),
                new Vector2f(17, 54),
                new Vector2f(-2f, -4f),
                360 * (float) Math.random(),
                MathUtils.getRandomNumberInRange(-270f,270f),
                new Color(255, 255, 255, 255),
                true,
                0.0f,
                2f,
                MathUtils.getRandomNumberInRange(2f,4f)
                );

                fighter.getMutableStats().getMaxSpeed().modifyFlat("sikr_durnir_shield_dropped", 20);
            }
        }

        return "Shield Damage Reduction";
    }
}