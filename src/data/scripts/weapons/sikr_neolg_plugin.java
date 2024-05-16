package data.scripts.weapons;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class sikr_neolg_plugin extends BaseEveryFrameCombatPlugin {

    private static final float DETECT_RANGE = 200;

    private DamagingProjectileAPI proj;
    private IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);

    public sikr_neolg_plugin(DamagingProjectileAPI projectile){
        this.proj = projectile;
    }

    public void init(CombatEngineAPI engine) {
		
	}

    @Override
    public void advance(float amount, List<InputEventAPI> events){

        if (Global.getCombatEngine() == null) {
			return;
		}
		if (Global.getCombatEngine().isPaused()) {
			amount = 0f;
		}

		if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}

        interval.advance(amount);
        if(interval.intervalElapsed()){

            for(ShipAPI targets : CombatUtils.getShipsWithinRange(proj.getLocation(), DETECT_RANGE)){
                if(targets.getOwner() == proj.getOwner() 
                || Math.abs(VectorUtils.getAngle(proj.getLocation(), targets.getLocation()) - proj.getFacing()) > 20 
                || MathUtils.getDistance(proj, targets) > DETECT_RANGE
                || targets.isHulk()){
                    continue;
                }
                if(targets != null){
                    CombatEngineAPI engine = Global.getCombatEngine();
                    engine.spawnExplosion(proj.getLocation(), new Vector2f(), new Color(245,180,40,255), 54, 0.3f);
                    engine.spawnExplosion(proj.getLocation(), new Vector2f(), new Color(245,245,245,255), 26, 0.4f);

                    for(int i = 0; i < 14;i++){
                        float angle = proj.getFacing() + MathUtils.getRandomNumberInRange(0, 30) - 15;
                        Vector2f speed_var = new Vector2f(MathUtils.getRandomNumberInRange(-100, 100),MathUtils.getRandomNumberInRange(-100, 100));
                        engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "sikr_neolg_stage2", 
                            proj.getLocation(), angle, speed_var);
                    }
        
                    engine.removeEntity(proj);
                }
                break;
            }
        }
        
        

    }
    
}
