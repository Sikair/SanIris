package data.scripts.listeners;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.util.Misc;

public class sikr_angle_damage_reduction implements DamageTakenModifier {

    private final ShipAPI fighter;
    private final float reductionPercent;   // e.g., 0.30f = 30% reduction
    private final float frontArc;           // degrees from center (e.g., 60° = 120° total arc)
    private final boolean explosiveWeakness;

    public sikr_angle_damage_reduction(ShipAPI fighter, float reductionPercent, float frontArc, boolean explosiveWeakness) {
        this.fighter = fighter;
        this.reductionPercent = reductionPercent;
        this.frontArc = frontArc;
        this.explosiveWeakness = explosiveWeakness;
    }

    @Override
    public String modifyDamageTaken(Object param,
                                    CombatEntityAPI target,
                                    DamageAPI damage,
                                    Vector2f point,
                                    boolean shieldHit) {

        // Only apply to hull/armor hits
        if (shieldHit || point == null) return null;

        // Compute angle of incoming hit relative to fighter facing
        float angleToHit = Misc.getAngleInDegrees(fighter.getLocation(), point);
        float relativeAngle = Math.abs(MathUtils.getShortestRotation(fighter.getFacing(), angleToHit));

        // Check if within front arc
        if (relativeAngle <= frontArc) {
            float mult;

            if(explosiveWeakness && damage.getType().equals(DamageType.HIGH_EXPLOSIVE)){
                mult = 1f - (reductionPercent / 2 );
            }else{
                mult = 1f - reductionPercent;
            }

            damage.getModifier().modifyMult("sikr_front_damage_reduction", mult);

            return "Front Damage Reduction";
        }

        return null;
    }
}