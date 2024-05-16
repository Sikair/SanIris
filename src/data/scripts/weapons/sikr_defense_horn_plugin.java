package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class sikr_defense_horn_plugin implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false;
    private ShipAPI ship;
    private ShipAPI parent;
    private SpriteAPI sprite;
    private boolean activated=false;
    private float armor_rating;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            parent=weapon.getShip().getParentStation();
            sprite = Global.getSettings().getSprite("fx","sikr_frost");
            armor_rating = ship.getArmorGrid().getArmorRating();
        }
        
        if(parent == Global.getCombatEngine().getPlayerShip()){
            MagicUI.drawInterfaceStatusBar(parent, ship.getHitpoints() / ship.getMaxHitpoints(), null, null, 0, "HORN", (int) ship.getHitpoints());
        }
    
        if (engine.isPaused() || parent == null || !ship.isAlive()) return;

        ship.getVelocity().set(parent.getVelocity());

        if(!activated && parent.getSystem().isChargeup()){  
            activated = true;
            MagicRender.objectspace(sprite, ship, new Vector2f(5,0), new Vector2f(), new Vector2f(390,62), new Vector2f(0,0),
                 180f, 0f, true, new Color(255,255,255,255), true, 2, 8, 2, true); 

            ship.getMutableStats().getHullDamageTakenMult().modifyMult("sikr_offense_horn", 0.2f);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("sikr_offense_horn", 0.2f);

        } else if (activated && !parent.getSystem().isActive()){
            activated = false;

            ship.getMutableStats().getHullDamageTakenMult().unmodify("sikr_offense_horn");
            ship.getMutableStats().getArmorDamageTakenMult().unmodify("sikr_offense_horn");
        }

        if(activated){
            Point point = DefenseUtils.getMostDamagedArmorCell(ship);
            if(point != null){
                float toHeal = 10f + (armor_rating - ship.getArmorGrid().getArmorValue(point.getX(), point.getY())) * 0.02f;
                ship.getArmorGrid().setArmorValue(point.getX(), point.getY(), toHeal);
            }
        }
    }

}
