package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;

import org.lwjgl.util.vector.Vector2f;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class sikr_offense_horn_plugin implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false;
    private ShipAPI ship;
    private ShipAPI parent;
    private SpriteAPI sprite;
    private boolean activated=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            parent=weapon.getShip().getParentStation();
            sprite = Global.getSettings().getSprite("fx","sikr_heat");
        }
        
        if(parent == Global.getCombatEngine().getPlayerShip()){
            MagicUI.drawInterfaceStatusBar(parent, ship.getHitpoints() / ship.getMaxHitpoints(), null, null, 0, "HORN", (int) ship.getHitpoints());
        }

        if (engine.isPaused() || parent == null || !ship.isAlive()) return;

        ship.getVelocity().set(parent.getVelocity());

        if(!activated && parent.getSystem().isChargeup()){  
            activated = true;
            MagicRender.objectspace(sprite, ship, new Vector2f(53,0), new Vector2f(), new Vector2f(206,218), new Vector2f(0,0),
                 180f, 0f, true, new Color(255,255,255,255), true, 2, 6, 6, true);    //new Color(255,255,255,255)    

            ship.getMutableStats().getHullDamageTakenMult().modifyMult("sikr_offense_horn", 0.54f);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("sikr_offense_horn", 0.3f);
            parent.getMutableStats().getHullDamageTakenMult().modifyMult("sikr_offense_horn", 0.5f);
            parent.getMutableStats().getArmorDamageTakenMult().modifyMult("sikr_offense_horn", 0.1f);
            ship.setMass(24000f);
            ship.setHullSize(HullSize.CAPITAL_SHIP);

        } else if (activated && !parent.getSystem().isActive()){
            activated = false;

            ship.getMutableStats().getHullDamageTakenMult().unmodify("sikr_offense_horn");
            ship.getMutableStats().getArmorDamageTakenMult().unmodify("sikr_offense_horn");
            parent.getMutableStats().getHullDamageTakenMult().unmodify("sikr_offense_horn");
            parent.getMutableStats().getArmorDamageTakenMult().unmodify("sikr_offense_horn");
            ship.setMass(1100f);
            ship.setHullSize(HullSize.FRIGATE);
        }
    }

}
