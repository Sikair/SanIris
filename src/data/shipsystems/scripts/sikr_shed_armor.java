package data.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class sikr_shed_armor extends BaseShipSystemScript {

	private static final float HIGHER_ARMOR_KEPT = 0.8f;

	private int number_use = 5;
	private boolean fired = false;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI) stats.getEntity();
		if (ship == null) return;
	
		// Fire once at the start of activation
		if (!fired) {
			fired = true;
	
			float ammo = Math.min(ship.getSystem().getAmmo() + 1, 10);
			float armor_percent = (ammo / 10f) * (1f - ((5 - number_use) / 10f * 0.5f));
			float new_armor = armor_percent * ship.getArmorGrid().getArmorRating() / 15f;
	
			ArmorGridAPI grid = ship.getArmorGrid();
			float[][] cells = grid.getGrid();
	
			for (int x = 0; x < cells.length; x++) {
				for (int y = 0; y < cells[0].length; y++) {
	
					float current = grid.getArmorValue(x, y);
	
					if (current * HIGHER_ARMOR_KEPT < new_armor) {
						grid.setArmorValue(x, y, new_armor);
					} else {
						grid.setArmorValue(x, y, current * HIGHER_ARMOR_KEPT);
					}
				}
			}
	
			ship.syncWithArmorGridState();
	
			if (number_use > 0) number_use--;
			ship.getSystem().setAmmo(0);
	
			// Trigger the armor‑shedding effect
			spawnBreakingSprite(ship);
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		fired = false;
	}

	private void spawnBreakingChunk(ShipAPI ship){
		Global.getCombatEngine().addLayeredRenderingPlugin(
					new sikr_shed_armor_render(ship)
				);
	}

	private void spawnBreakingSprite(ShipAPI ship) {
		int slicesX = 7;
		int slicesY = 7;
	
		SpriteAPI baseSprite = Global.getSettings().getSprite("fx", "sikr_ship_bara");
		// OU votre sprite custom : Global.getSettings().getSprite("fx", "sikr_ship_bara");
	
		float texW = baseSprite.getWidth();   // largeur monde du sprite entier
		float texH = baseSprite.getHeight();  // hauteur monde du sprite entier
	
		float sliceW = texW / slicesX;
		float sliceH = texH / slicesY;
	
		for (int ix = 0; ix < slicesX; ix++) {
			for (int iy = 0; iy < slicesY; iy++) {
	
				// Offset centré sur le vaisseau
				float offsetX = (ix - (slicesX - 1f) / 2f) * sliceW;
				// L'axe V de la texture croît vers le bas, mais l'axe Y monde vers le haut
				float offsetY = -((iy - (slicesY - 1f) / 2f) * sliceH);
	
				// Rotation de l'offset selon l'orientation du vaisseau
				float rad = (float) Math.toRadians(ship.getFacing() - 90f);
				float rotX = offsetX * (float) Math.cos(rad) - offsetY * (float) Math.sin(rad);
				float rotY = offsetX * (float) Math.sin(rad) + offsetY * (float) Math.cos(rad);
	
				Vector2f pos = new Vector2f(
					ship.getLocation().x + rotX,
					ship.getLocation().y + rotY
				);
	
				// Direction d'éjection : du centre du vaisseau vers la position du morceau
				Vector2f drift = new Vector2f(
					pos.x - ship.getLocation().x,
					pos.y - ship.getLocation().y
				);

				// Si trop proche du centre, donner une direction aléatoire
				if (drift.length() < 0.01f) {
					drift = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(0f, 360f));
				} else {
					drift.normalise();
				}

				// Ajouter un angle aléatoire léger pour que ce soit moins uniforme
				float scatter = MathUtils.getRandomNumberInRange(-25f, 25f);
				drift = Misc.rotateAroundOrigin(drift, scatter);

				float speed = MathUtils.getRandomNumberInRange(40f, 120f);
				drift.scale(speed);

				// Ajouter la vélocité du vaisseau
				Vector2f.add(drift, ship.getVelocity(), drift);
	
				float srcX = ix * (baseSprite.getTextureWidth() / slicesX);
				float srcY = iy * (baseSprite.getTextureHeight() / slicesY);
	
				Global.getCombatEngine().addLayeredRenderingPlugin(
					new sikr_shed_armor_plugin(
						baseSprite,
						srcX, srcY,
						baseSprite.getTextureWidth() / slicesX,
						baseSprite.getTextureHeight() / slicesY,
						sliceW,
						sliceH,
						pos, drift,
						ship.getFacing()
					)
				);
			}
		}
	}
}