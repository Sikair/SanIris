package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class sikr_mech_sniper_hit implements OnHitEffectPlugin{

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldhit,
           ApplyDamageResultAPI damage, CombatEngineAPI engine) {
               if(target instanceof ShipAPI){
                   if(!shieldhit){
                       engine.applyDamage(target, point, 200, DamageType.FRAGMENTATION, 0, false, false, projectile.getSource());
                   }  
                   
               }
           } 
}