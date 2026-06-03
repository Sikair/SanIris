package data.shipsystems.scripts;

import java.awt.Color;
import java.util.EnumSet;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class sikr_shed_armor_plugin extends BaseCombatLayeredRenderingPlugin {

    private Vector2f loc;
    private final Vector2f drift;
    private float angle;
    private final SpriteAPI sprite;

    // UV normalisés (0.0 - 1.0)
    private final float uvX, uvY, uvW, uvH;

    // Taille en pixels dans le monde
    private final float worldW, worldH;

    private float time = 0f;
    private final float maxTime = 3f;

    // Rotation aléatoire pour chaque morceau
    private final float spinRate;

    public sikr_shed_armor_plugin(
            SpriteAPI sprite,
            float srcX, float srcY,   // pixels dans la texture
            float srcW, float srcH,   // pixels dans la texture
            float sliceW,
            float sliceH,
            Vector2f loc,
            Vector2f drift,
            float angle
    ) {
        this.sprite = sprite;

        float texW = sprite.getTextureWidth();   // largeur réelle de la texture en pixels
        float texH = sprite.getTextureHeight();  // hauteur réelle de la texture en pixels

        // Conversion pixels → UV normalisés
        this.uvX = srcX / texW;
        this.uvY = srcY / texH;
        this.uvW = srcW / texW;
        this.uvH = srcH / texH;

        // Taille monde du morceau = taille du sprite / nb de tranches
        this.worldW = sliceW;
        this.worldH = sliceH;

        this.loc = new Vector2f(loc);
        this.drift = new Vector2f(drift);
        this.angle = angle;
        this.spinRate = MathUtils.getRandomNumberInRange(-120f, 120f);
    }

    @Override
    public void advance(float amount) {
        time += amount;

        // Déplacement du morceau
        Vector2f delta = new Vector2f(drift);
        delta.scale(amount);
        Vector2f.add(loc, delta, loc);

        // Ralentissement progressif (clampé à 0 pour éviter d'inverser la direction)
        drift.scale(Math.max(0f, 1f - amount * 2f));

        // Rotation du morceau
        angle += spinRate * amount;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {

        //if (Global.getCombatEngine().isPaused()) return;

        float progress = time / maxTime;
        float alpha = progress < 0.5f ? 1f : 1f - ((progress - 0.5f) / 0.5f);
        if (alpha <= 0f) return;

        float hw = worldW / 2f;
        float hh = worldH / 2f;

        sprite.bindTexture();

        GL11.glPushMatrix();
        GL11.glTranslatef(loc.x, loc.y, 0f);  // ← coordonnées monde
        GL11.glRotatef(angle, 0f, 0f, 1f);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, alpha);

        GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(uvX,       1f - uvY      );  GL11.glVertex2f(-hw, -hh);
            GL11.glTexCoord2f(uvX + uvW, 1f - uvY      );  GL11.glVertex2f( hw, -hh);
            GL11.glTexCoord2f(uvX + uvW, 1f - uvY - uvH);  GL11.glVertex2f( hw,  hh);
            GL11.glTexCoord2f(uvX,       1f - uvY - uvH);  GL11.glVertex2f(-hw,  hh);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    @Override
    public boolean isExpired() {
        return time >= maxTime;
    }

    @Override
    public float getRenderRadius() {
        return 1200f;
    }

    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return layers;
    }
}
