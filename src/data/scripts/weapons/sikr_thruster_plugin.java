package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import data.scripts.sikr_trail_data_store;
import data.scripts.sikr_trail_data_store.sikr_trailData;

import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicAnim;

public class sikr_thruster_plugin  implements EveryFrameWeaponEffectPlugin{   
    
    private boolean runOnce=false;
    private float trail_id;
    private SpriteAPI sprite;
    private ShipAPI ship;
    private float side_var = 1;
    private float num;
    private Vector2f offset;
    private float alpha = 1;
    //private IntervalUtil interval = new IntervalUtil(0.4f, 0.8f);

    private sikr_trailData trail_data;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(engine.isPaused() || !weapon.getShip().isAlive()) return;

        if(!runOnce){
            runOnce = true;
            trail_id = MagicTrailPlugin.getUniqueID();
            ship = weapon.getShip();
            if(weapon.getSlot().getId().endsWith("L")) side_var = -1;
            sprite = Global.getSettings().getSprite("fx","trails_trail_twin");

            trail_data = sikr_trail_data_store.getTrailWithId(ship.getHullSpec().getBaseHullId());
        }

        if(ship.isPhased()){
            alpha = 0.3f;
        }else{
            alpha = 0.8f;
        }

        if(num <= 120)
        {
            num++;
        }else{
            num = 0;
        }

        //get where the ship is going to generate the offset vector
        float facing_dif = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(new Vector2f(0,0), ship.getVelocity()));
        facing_dif = Math.abs(facing_dif);

        offset = new Vector2f(side_var*(MagicAnim.smoothReturnNormalizeRange(num, 0, 120)*(ship.getVelocity().length()+1)),MagicAnim.normalizeRange(facing_dif, 0 ,80)*-80 + -120);
        //offset = new Vector2f(side_var*(MagicAnim.smoothReturnNormalizeRange(num, 0, 120)*(ship.getVelocity().length()+20)),-120);

        //MagicTrailPlugin.AddTrailMemberAdvanced(linkedEntity, ID, sprite, position, startSpeed, endSpeed, angle, startAngularVelocity, endAngularVelocity, 
            //startSize, endSize, startColor, endColor, opacity, inDuration, mainDuration, outDuration, additive, textureLoopLength, textureScrollSpeed, textureOffset, offsetVelocity, advancedOptions, layerToRenderOn, frameOffsetMult);    
        
        if(trail_data != null){
            MagicTrailPlugin.addTrailMemberAdvanced((CombatEntityAPI) ship, trail_id, sprite, weapon.getLocation(), 0f, 5f, Misc.getAngleInDegrees(new Vector2f(ship.getVelocity())) + 1 + weapon.getSlot().getAngle() , trail_data.startAngular, trail_data.endAngular, 
                trail_data.startSize, trail_data.endSize, new Color(200,0,165,255), new Color(255,0,210,255), alpha, trail_data.inDuration, trail_data.mainDuration, trail_data.outDuration, false, 0f, 0f, VectorUtils.rotate(offset, ship.getFacing()-90), null, null, 1f);
        }else{
            switch(ship.getHullSize()){
                case FIGHTER: MagicTrailPlugin.addTrailMemberAdvanced((CombatEntityAPI) ship, trail_id, sprite, weapon.getLocation(), 0f, 5f, Misc.getAngleInDegrees(new Vector2f(ship.getVelocity())) + 1 + weapon.getSlot().getAngle() , 0f, 0f, 
                        20, 1, new Color(200,0,165,255), new Color(255,0,210,255), alpha, 0.2f, 0.1f, 0.3f, false, 0f, 0f, VectorUtils.rotate(offset, ship.getFacing()-90), null, null, 1f);
                        break;
                case FRIGATE: MagicTrailPlugin.addTrailMemberAdvanced((CombatEntityAPI) ship, trail_id, sprite, weapon.getLocation(), 0f, 5f, Misc.getAngleInDegrees(new Vector2f(ship.getVelocity())) + 1 + weapon.getSlot().getAngle() , 0f, 0f, 
                        65, 1, new Color(200,0,165,255), new Color(255,0,210,255), alpha, 0.2f, 0.2f, 0.2f, false, 0f, 0f, VectorUtils.rotate(offset, ship.getFacing()-90), null, null, 1f);
                        break;
                case DESTROYER: MagicTrailPlugin.addTrailMemberAdvanced((CombatEntityAPI) ship, trail_id, sprite, weapon.getLocation(), 0f, 5f, Misc.getAngleInDegrees(new Vector2f(ship.getVelocity())) + 1 + weapon.getSlot().getAngle() , 0f, 0f, 
                        80, 1, new Color(200,0,165,255), new Color(255,0,210,255), alpha, 0.2f, 0.4f, 0.3f, false, 0f, 0f, VectorUtils.rotate(offset, ship.getFacing()-90), null, null, 1f);
                        break;
                case CRUISER: MagicTrailPlugin.addTrailMemberAdvanced((CombatEntityAPI) ship, trail_id, sprite, weapon.getLocation(), 0f, 5f, Misc.getAngleInDegrees(new Vector2f(ship.getVelocity())) + 1 + weapon.getSlot().getAngle() , 0f, 0f, 
                        80, 1, new Color(200,0,165,255), new Color(255,0,210,255), alpha, 0.2f, 0.8f, 0.3f, false, 0f, 0f, VectorUtils.rotate(offset, ship.getFacing()-90), null, null, 1f);
                        break;
                case CAPITAL_SHIP: MagicTrailPlugin.addTrailMemberAdvanced((CombatEntityAPI) ship, trail_id, sprite, weapon.getLocation(), 0f, 5f, Misc.getAngleInDegrees(new Vector2f(ship.getVelocity())) + 1 + weapon.getSlot().getAngle() , 0f, 0f, 
                        130, 1, new Color(200,0,165,255), new Color(255,0,210,255), alpha, 0.2f, 1.2f, 0.3f, false, 0f, 0f, VectorUtils.rotate(offset, ship.getFacing()-90), null, null, 1f);
                        break;
                default:
                    break;
            }
        }
    }
}
