package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.magiclib.util.MagicFakeBeam;

import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class sikr_rhodophyta_plugin extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin{

    private static final int ANGLE_VAR = 10;
    private static final int DAMAGE = 200;
    private static final int EMP = 150;

    private DamagingProjectileAPI proj1;
    private DamagingProjectileAPI proj2;

    private IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);

    public sikr_rhodophyta_plugin(){}

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        MissileAPI missile1 = (MissileAPI) engine.spawnProjectile(weapon.getShip(), weapon, "sikr_rhodophyta", 
        projectile.getLocation(), projectile.getFacing()-ANGLE_VAR, new Vector2f());
        MissileAPI missile2 = (MissileAPI) engine.spawnProjectile(weapon.getShip(), weapon, "sikr_rhodophyta", 
        projectile.getLocation(), projectile.getFacing()+ANGLE_VAR, new Vector2f());

        //set max flight time (range)
		float flight_time = weapon.getRange() / weapon.getProjectileSpeed();
		missile1.setMaxFlightTime(flight_time);
        missile2.setMaxFlightTime(flight_time);

        sikr_rhodophyta_plugin plugin = new sikr_rhodophyta_plugin(missile1, missile2);
		engine.addLayeredRenderingPlugin(plugin);
        engine.removeEntity(projectile);
    }

    public sikr_rhodophyta_plugin(CombatEntityAPI projectile1, CombatEntityAPI projectile2){
        proj1 = (DamagingProjectileAPI) projectile1;
        proj2 = (DamagingProjectileAPI) projectile2;
    }

    public float getRenderRadius() {
		return 1000f;
	}

    public void init(CombatEntityAPI entity) {
		super.init(entity);
	}

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        interval.advance(amount);
        proj1.setFacing(VectorUtils.getAngle(proj1.getLocation(), proj2.getLocation()));
        proj2.setFacing(VectorUtils.getAngle(proj2.getLocation(), proj1.getLocation()));

        if(interval.intervalElapsed()){
            for( CombatEntityAPI target : CombatUtils.getShipsWithinRange(MathUtils.getMidpoint(proj1.getLocation(), proj2.getLocation()
            ), MathUtils.getDistance(proj1, proj2))){

            if(target.getOwner() == proj1.getOwner()) continue;
            if(CollisionUtils.getCollisionPoint(proj1.getLocation(), proj2.getLocation(), target) != null){
                MagicFakeBeam.spawnFakeBeam(Global.getCombatEngine(), proj1.getLocation(), MathUtils.getDistance(proj1, proj2), VectorUtils.getAngle(proj1.getLocation(), proj2.getLocation()), 10, 0.2f, 0.1f, 10, 
                    new Color(40,190,255,255), new Color(40,130,255,200), DAMAGE, DamageType.ENERGY, EMP, proj1.getSource());
                MagicFakeBeam.spawnFakeBeam(Global.getCombatEngine(), proj2.getLocation(), MathUtils.getDistance(proj2, proj1), VectorUtils.getAngle(proj2.getLocation(), proj1.getLocation()), 10, 0.2f, 0.1f, 10, 
                    new Color(40,190,255,255), new Color(40,130,255,200), DAMAGE, DamageType.ENERGY, EMP, proj1.getSource());

                Global.getCombatEngine().removeEntity(proj1);
                Global.getCombatEngine().removeEntity(proj2);

                break;
            }
        }
        }
    
    }

    @Override
    public boolean isExpired() {
        if (proj1 == null || proj1.didDamage() || proj1.isFading() || !Global.getCombatEngine().isEntityInPlay(proj1)) {
			return true;
		}
        return false;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
      
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(200,0,165);
        GL11.glVertex2f(proj1.getLocation().getX(), proj1.getLocation().getY());
        GL11.glVertex2f(proj2.getLocation().getX(), proj2.getLocation().getY());
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

    }
    
}
