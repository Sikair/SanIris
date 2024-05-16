package data.scripts.weapons;

//import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import org.magiclib.util.MagicUI;


public class sikr_unnr_plugin3 implements EveryFrameWeaponEffectPlugin{

    private boolean run_once = false;
    private boolean activated = false;

    private ShipAPI ship;
    private float ship_base_flux;
    private ShipAPI ship_shield;
    private float dissipation;
    private float shield_max_flux;
    private float shield_curr_flux;
  
    public static String variantId = "sikr_shield_emitter_wing";
    private static float shield_range = 600;
    private float actual_range = 200;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if(!run_once){
            run_once = true;
            ship = weapon.getShip();
            dissipation = ship.getMutableStats().getFluxDissipation().getModifiedValue() / 100;
            shield_max_flux = ship.getMutableStats().getFluxCapacity().getModifiedValue();
            ship_base_flux = ship.getMutableStats().getFluxCapacity().getBaseValue();
            shield_curr_flux = 0;
        } 

        if(ship == Global.getCombatEngine().getPlayerShip()){
            MagicUI.drawInterfaceStatusBar(ship, shield_curr_flux / shield_max_flux, null, null, 0, "SHIELD", (int) shield_curr_flux);
        }

        if(engine.isPaused() || !weapon.getShip().isAlive()) return;

        //set data for system ai
        ship.setCustomData(ship.getId()+"shield_flux_remaining", shield_max_flux - shield_curr_flux);

        //spawn the shield drone
        if(!activated && ship.getSystem().isChargeup()){
            activated = true;
            actual_range = 200;
            engine.getFleetManager(ship.getOriginalOwner()).setSuppressDeploymentMessages(true);
            ship_shield = engine.getFleetManager(ship.getOriginalOwner()).spawnShipOrWing(variantId, ship.getLocation(), ship.getFacing(), 0f, null);
            ship_shield.setLaunchingShip(ship);
            //ship_shield.getMutableStats().getHighExplosiveShieldDamageTakenMult().modifyMult(ship.getId()+"_he", 1.5f);
            ship_shield.getMutableStats().getVentRateMult().modifyMult(ship.getId()+"_vent", 0f);
            ship_shield.getMutableStats().getFluxCapacity().modifyFlat(ship.getId()+"_flux", shield_max_flux - ship_base_flux);
            ship_shield.getFluxTracker().increaseFlux(shield_curr_flux, false);
            engine.getFleetManager(ship.getOriginalOwner()).setSuppressDeploymentMessages(false);

        } else if (activated && !ship.getSystem().isActive()){
            activated = false;
            if(engine.isEntityInPlay(ship_shield)) engine.removeEntity(ship_shield);
        } else if (!activated){
            //regen the max shield flux
            if(shield_curr_flux != 0) shield_curr_flux = shield_curr_flux - dissipation > 0 ? shield_curr_flux - dissipation : 0;
        }

        if(ship_shield == null) return;
        if(activated){

            //Color colorToUse = new Color(255,0,210, Math.round((1 - (shield_flux / shield_max_flux)) * 155 + 100));
            //weapon.getSprite().setColor(colorToUse);

            if(actual_range < shield_range){
                ship_shield.setCollisionRadius(actual_range+=3);
                ship_shield.getShield().setRadius(actual_range+=3);
            }

            shield_curr_flux = ship_shield.getCurrFlux();

            //dissipate some flux from the drone
            if(ship_shield.getCurrFlux() > 0 && ship.getCurrFlux() < ship.getMaxFlux() * 0.8f){
                //ensure the main ship flux dont raise if not necessary
                float flux_dissipated = ship_shield.getCurrFlux() >= dissipation ? dissipation : ship_shield.getCurrFlux();
                
                ship.getFluxTracker().increaseFlux(flux_dissipated / 2, true);
                ship_shield.getFluxTracker().decreaseFlux(flux_dissipated);
            }
            if(ship_shield.getFluxTracker().isOverloaded()) {
                ship.getFluxTracker().beginOverloadWithTotalBaseDuration(2f);
                if(engine.isEntityInPlay(ship_shield)) engine.removeEntity(ship_shield);
            }
        }
        if(activated && ship_shield != null){
            ship_shield.getLocation().set(ship.getLocation());
            ship_shield.setFacing(ship.getFacing());
        }
    }
}

