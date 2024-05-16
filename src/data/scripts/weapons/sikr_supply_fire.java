package data.scripts.weapons;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class sikr_supply_fire implements OnFireEffectPlugin{

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        ShipAPI ship = weapon.getShip();
        Vector2f potential_point = null;
        Vector2f target_point = projectile.getLocation();

        for(FighterWingAPI w : ship.getAllWings()){
            for(ShipAPI f : w.getWingMembers()){
                if(f.getHullSpec().getMinCrew() == 0) continue;
                if(potential_point == null){
                    potential_point = new Vector2f(f.getLocation());
                }else{
                    if(MathUtils.getDistance(potential_point, f.getLocation()) > 200){
                        potential_point = MathUtils.getMidpoint(potential_point, f.getLocation());
                    }else{
                        continue;
                    }
                }
            }
        }

        if(potential_point != null) target_point = MathUtils.getMidpoint(MathUtils.getMidpoint(potential_point, projectile.getLocation()), potential_point);

        projectile.setFacing(ship.getFacing());
        Global.getCombatEngine().addPlugin(new sikr_supply_plugin(projectile, target_point));
    }

    
    
}