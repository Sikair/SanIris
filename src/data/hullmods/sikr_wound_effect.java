package data.hullmods;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

//import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

public class sikr_wound_effect extends BaseCombatLayeredRenderingPlugin implements DamageTakenModifier{

	public static final float MIN_DAMAGE = 250;
	public static final float DISSIPATION_PERCENT = 3;

	// each tick is on average .9 seconds
	// ticks can't be longer than a second or floating damage numbers separate
	public static int NUM_TICKS = 6;
	public static float MIN_FLUX = 0.1f;

    //LISTENER
    public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
        if(shieldHit) return null;
		if(!(target instanceof ShipAPI)) return null;
		if(damage.getDamage() < MIN_DAMAGE) return null;
		//if(MathUtils.getRandomNumberInRange(0, 20) != 1) return null;

        Vector2f offset = Vector2f.sub(point, target.getLocation(), new Vector2f());
        offset = Misc.rotateAroundOrigin(offset, -target.getFacing());
        
		sikr_wound_effect effect = new sikr_wound_effect((ShipAPI) target, offset);
		CombatEntityAPI e = Global.getCombatEngine().addLayeredRenderingPlugin(effect);
		e.getLocation().set(point);
        return null;
    }

    public static class ParticleData {
		public SpriteAPI sprite;
		public Vector2f offset = new Vector2f();
		public Vector2f vel = new Vector2f();
		public float scale = 1f;
		public float scaleIncreaseRate = 1f;
		public float turnDir = 1f;
		public float angle = 1f;
		
		public float maxDur;
		public FaderUtil fader;
		public float elapsed = 0f;
		public float baseSize;
		
		public ParticleData(float baseSize, float maxDur, float endSizeMult) {
			sprite = Global.getSettings().getSprite("misc", "wormhole_corona");
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
			sprite.setAdditiveBlend();
			
			angle = (float) Math.random() * 360f;
			
			this.maxDur = maxDur;
			scaleIncreaseRate = endSizeMult / maxDur;
			if (endSizeMult < 1f) {
				scaleIncreaseRate = -1f * endSizeMult;
			}
			scale = 0.5f;
			
			this.baseSize = baseSize;
			turnDir = Math.signum((float) Math.random() - 0.5f) * 20f * (float) Math.random();
			
			float driftDir = (float) Math.random() * 360f;
			vel = Misc.getUnitVectorAtDegreeAngle(driftDir);
			vel.scale(0.25f * baseSize / maxDur * (1f + (float) Math.random() * 1f));
			
			fader = new FaderUtil(0f, 0.5f, 0.5f);
			fader.forceOut();
			fader.fadeIn();
		}
		public void advance(float amount) {
			scale += scaleIncreaseRate * amount;
			
			offset.x += vel.x * amount;
			offset.y += vel.y * amount;
				
			angle += turnDir * amount;
			
			elapsed += amount;
			if (maxDur - elapsed <= fader.getDurationOut() + 0.1f) {
				fader.fadeOut();
			}
			fader.advance(amount);
		}
	}

	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	protected FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f); 
	protected DamagingProjectileAPI proj;
	protected ShipAPI target;
	protected int ticks = 0;
    protected Vector2f offset;
	protected IntervalUtil interval;
	protected FluxTrackerAPI flux_tracker;
	protected float flux_bleed_dissipation = 0;

	public sikr_wound_effect(ShipAPI target, Vector2f offset) {
		this.target = target;
        this.offset = offset;
        this.flux_tracker = target.getFluxTracker();
		this.flux_bleed_dissipation = flux_tracker.getMaxFlux() * (DISSIPATION_PERCENT / 100);
		
		interval = new IntervalUtil(0.8f, 1f);
		interval.forceIntervalElapsed();
	}
	
	public sikr_wound_effect() {
	}

	public float getRenderRadius() {
		return 500f;
	}
	
	
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_INDICATORS_LAYER);
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}

	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}
	
	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;

        Vector2f loc = new Vector2f(offset);
		loc = Misc.rotateAroundOrigin(loc, target.getFacing());
		Vector2f.add(target.getLocation(), loc, loc);
		entity.getLocation().set(loc);

		List<ParticleData> remove = new ArrayList<ParticleData>();
		for (ParticleData p : particles) {
			p.advance(amount);
			if (p.elapsed >= p.maxDur) {
				remove.add(p);
			}
		}
		particles.removeAll(remove);

		//float volume = 1f;
		if (ticks >= NUM_TICKS || !target.isAlive() || flux_tracker.getCurrFlux() < flux_tracker.getMaxFlux()*MIN_FLUX || !Global.getCombatEngine().isEntityInPlay(target)) {
			fader.fadeOut();
			fader.advance(amount);
			//volume = fader.getBrightness();
		}
		//Global.getSoundPlayer().playLoop("disintegrator_loop", target, 1f, volume, loc, target.getVelocity());

		interval.advance(amount);
		if (interval.intervalElapsed() && ticks < NUM_TICKS && target.isAlive()) {
			sikr_bleed_flux();
            ticks++;
		} 
	}

	protected void sikr_bleed_flux(){
        // Float curr_flux = flux_tracker.getCurrFlux();
        // Float hard_flux = flux_tracker.getHardFlux();
        // flux_tracker.setHardFlux(hard_flux-hard_flux*0.003f);
        // flux_tracker.setCurrFlux(curr_flux-curr_flux*0.003f);

		flux_tracker.decreaseFlux(flux_bleed_dissipation);
        
		int num = 3;
		for (int i = 0; i < num; i++) {
			ParticleData p = new ParticleData(70f, 3f + (float) Math.random() * 2f, 2f);
			particles.add(p);
			p.offset = Misc.getPointWithinRadius(p.offset, 20f);
		}
	}

	public boolean isExpired() {
		return particles.isEmpty() && 
		(ticks >= NUM_TICKS || !target.isAlive() || flux_tracker.getCurrFlux() < flux_tracker.getMaxFlux()*MIN_FLUX|| !Global.getCombatEngine().isEntityInPlay(target));
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;

		Color color = new Color(220,5,5,255);
		float b = viewport.getAlphaMult();

		GL14.glBlendEquation(GL14.GL_BLEND_SRC_ALPHA);
		
		for (ParticleData p : particles) {
			//float size = proj.getProjectileSpec().getWidth() * 0.6f;
			float size = (p.baseSize * p.scale) / 5f;
			
			Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
			
			float alphaMult = 10f;
			
			p.sprite.setAngle(p.angle);
			p.sprite.setSize(size, size);
			p.sprite.setAlphaMult(b * alphaMult); // * p.fader.getBrightness()
			p.sprite.setColor(color);
			p.sprite.renderAtCenter(loc.x, loc.y);
		}
		
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
	}

    
}
