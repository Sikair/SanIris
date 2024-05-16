package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class sikr_shed_armor extends BaseShipSystemScript {

	private static final float HIGHER_ARMOR_KEPT = 0.8f;

	private int number_use = 5;
	private boolean activated = false;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		if(activated) return;

		activated = true;

		float ammo = ship.getSystem().getAmmo() + 1 > 10 ? 10 : ship.getSystem().getAmmo() + 1;
		float armor_percent = (ammo / 10) * (1 - ((5 - number_use) / 10 * 0.5f));
		float new_armor = armor_percent * ship.getArmorGrid().getArmorRating() / 15; //divided by 15 to match actual cell value i think

        for (int i = 0; i < ship.getArmorGrid().getGrid()[0].length; i++) {
            for (int j = 0; j < ship.getArmorGrid().getGrid()[1].length; j++) {
				if(ship.getArmorGrid().getArmorFraction(j, i) < new_armor){
					ship.getArmorGrid().setArmorValue(i, j, new_armor);
				}else{
					ship.getArmorGrid().setArmorValue(i, j, ship.getArmorGrid().getArmorFraction(j, i) * HIGHER_ARMOR_KEPT);
				}
            }
        }

		ship.syncWithArmorGridState();
		//if(armor_percent >= 0.5f) ship.clearDamageDecals();

		if(number_use > 0) number_use--;
		ship.getSystem().setAmmo(0);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		activated = false;
	}

}