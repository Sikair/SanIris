package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class sikr_commission extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float speed = stats.getMaxSpeed().getBaseValue();
        speed = speed > 150 ? 150 : speed;
        speed = speed < 10 ? 10 : speed;
        if(speed < 150){
            int maneuvrability_bonus = (int) (150 - speed) / 4;
            maneuvrability_bonus = maneuvrability_bonus < 10 ? 10 : maneuvrability_bonus;
            stats.getAcceleration().modifyPercent(id, maneuvrability_bonus * 2f);
		    stats.getDeceleration().modifyPercent(id, maneuvrability_bonus);
		    stats.getTurnAcceleration().modifyPercent(id, maneuvrability_bonus * 2f);
		    stats.getMaxTurnRate().modifyPercent(id, maneuvrability_bonus);
        }
        if(speed > 50){
            int armor_bonus = (int) ((speed - 50) * 0.8f);
            armor_bonus = armor_bonus < 15 ? 15 : armor_bonus;
            stats.getArmorBonus().modifyFlat(id, armor_bonus);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        float opad = 10f;
		Color h = Misc.getHighlightColor();
    
        float speed = ship.getMutableStats().getMaxSpeed().getBaseValue();
        speed = speed > 200 ? 200 : speed;
        speed = speed < 10 ? 10 : speed;

        tooltip.addSectionHeading("Current bonus", Alignment.MID, opad);
        if(speed < 150){
            int maneuvrability_bonus = (int) (150 - speed) / 4;
            maneuvrability_bonus = maneuvrability_bonus < 10 ? 10 : maneuvrability_bonus;
            LabelAPI label = tooltip.addPara("Increases the ship's maneuverability by " + maneuvrability_bonus + " percent. ", opad, h, maneuvrability_bonus + "");
            label.setHighlight(maneuvrability_bonus + "");
        }
        if(speed > 50){
            int armor_bonus = (int) ((speed - 50) * 0.8f);
            armor_bonus = armor_bonus < 15 ? 15 : armor_bonus;
            LabelAPI label = tooltip.addPara("Increases the ship's armor by " + armor_bonus + ".", opad, h, armor_bonus + "");
            label.setHighlight(armor_bonus + "");
        }
    }

	//Oh these are cool colors below introduced in 0.95a, to match with your tech type and stuff. Just nice to have!

    // @Override
    // public Color getBorderColor() {
    //     return new Color(235,200,30,255);
    // }

    @Override
    public Color getNameColor() {
        return new Color(235,200,30,255);
    }
}
