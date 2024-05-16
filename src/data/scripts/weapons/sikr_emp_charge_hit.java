package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class sikr_emp_charge_hit implements OnHitEffectPlugin{

    private static final int ARC_CHANCE = 20;
    private static final int ARC_PIERCE_CHANCE = 20;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldhit,
           ApplyDamageResultAPI damage, CombatEngineAPI engine) {
               if(target instanceof ShipAPI){
                    float emp = projectile.getEmpAmount();
                   if(!shieldhit){
                        if((float) Math.random() < ARC_CHANCE/100){
                            engine.spawnEmpArc(projectile.getSource(), point, projectile.getSource(), target, DamageType.ENERGY, 
                                0, emp, 1000f, null, 20f, new Color(195,45,215,255), new Color(240,115,255,255));
                        }
                   }else{
                    ShipAPI ship = (ShipAPI) target;
                    float pierceChance = (ship).getHardFluxLevel() - (1 - (ARC_PIERCE_CHANCE/100));
                    pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
                    
                    boolean piercedShield = (float) Math.random() < pierceChance;

                    if(piercedShield){
                        engine.spawnEmpArcPierceShields(projectile.getSource(), point, projectile.getSource(), target, DamageType.ENERGY, 
                            0, emp, 10000f, "tachyon_lance_emp_impact", 20f, new Color(195,45,215,255), new Color(240,115,255,255));
                    }
                   }
                   
               }
           } 
    
}
