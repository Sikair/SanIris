package data.scripts.weapons;

import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class sikr_lehal_plugin implements BeamEffectPlugin,EveryFrameWeaponEffectPlugin{

    private static final int REQUIRED_CHARGE = 30;

    private IntervalUtil beam_interval = new IntervalUtil(0.3f, 0.3f);
    private IntervalUtil interval = new IntervalUtil(1, 1.1f);

    private boolean run_once = false;
    private ShipAPI ship;
    private WeaponAPI missile;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        if(target instanceof ShipAPI || target instanceof AsteroidAPI){
            beam_interval.advance(amount);
            if(beam_interval.intervalElapsed()){
                int charge_amount = 1;
                int charge = 0;
                ShipAPI ship = beam.getSource();
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                if(!hitShield) charge_amount = 3;
                if(ship.getCustomData().containsKey(ship.getId()+"lehal_bi")){
                    charge = (int) ship.getCustomData().get(ship.getId()+"lehal_bi");
                    
                    if(charge+charge_amount > REQUIRED_CHARGE){
                        charge = REQUIRED_CHARGE;
                        ship.setCustomData(ship.getId()+"lehal_bi", charge);
                        return;
                    }
                }
                ship.setCustomData(ship.getId()+"lehal_bi", charge+charge_amount);
            }
        }
        
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!run_once){
            run_once = true;
            ship=weapon.getShip();
            for(WeaponAPI w : ship.getAllWeapons()){
                if(w.getSlot().getWeaponType() == WeaponType.MISSILE){
                    missile = w;
                    break;
                }
            }
        }

        if(missile == null) return;

        if(engine.isPaused()) return;
        if(missile.getAmmo() == missile.getMaxAmmo()) return;
        interval.advance(amount);
        if(interval.intervalElapsed()){
            if(weapon.getShip().getCustomData().containsKey(ship.getId()+"lehal_bi")){
                int charge = (int) ship.getCustomData().get(ship.getId()+"lehal_bi");
                while(charge >= REQUIRED_CHARGE){
                    charge -= REQUIRED_CHARGE;
                    missile.setAmmo(missile.getAmmo() + 1); 
                }
                ship.setCustomData(ship.getId()+"lehal_bi", charge);
            }
        }
        
    }
    
}
