package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.opengl.GL11;

import java.util.EnumSet;

public class sikr_shed_armor_overlay extends BaseCombatLayeredRenderingPlugin {

    // Target color at effectLevel = 1: gray at 60% brightness
    private static final float TARGET_R = 0.6f;
    private static final float TARGET_G = 0.6f;
    private static final float TARGET_B = 0.6f;

    private final ShipAPI  ship;
    private final SpriteAPI sprite;
    private float   effectLevel = 0f;
    private boolean expired     = false;

    public sikr_shed_armor_overlay(ShipAPI ship) {
        this.ship   = ship;
        this.sprite = Global.getSettings().getSprite("fx", "sikr_ship_bara");
    }

    public void setEffectLevel(float level) {
        this.effectLevel = level;
    }

    public void expire() {
        this.expired = true;
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public float getRenderRadius() {
        return Float.MAX_VALUE;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    private boolean isShipInvalid() {
        if (ship == null) return true;
        if (!ship.isAlive() || ship.isHulk()) return true;
        if (!Global.getCombatEngine().isEntityInPlay(ship)) return true;
        return false;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (expired) return;
        if (isShipInvalid()) { expired = true; return; }

        // Fade-in alpha: 0 at the start of IN, 1 at the end (progressively hides damage decals)
        float alpha = effectLevel;
        // Lerp white → 60% gray over effectLevel
        float r = 1f + (TARGET_R - 1f) * effectLevel;
        float g = 1f + (TARGET_G - 1f) * effectLevel;
        float b = 1f + (TARGET_B - 1f) * effectLevel;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Hull
        renderHull(r, g, b, alpha);

        // Weapons (under sprite → barrel → main sprite)
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            renderWeapon(weapon, r, g, b, alpha);
        }

        GL11.glPopAttrib();
    }

    private void renderHull(float r, float g, float b, float alpha) {
        float sw   = sprite.getWidth();
        float sh   = sprite.getHeight();
        float texW = sprite.getTextureWidth();
        float texH = sprite.getTextureHeight();

        SpriteAPI shipSprite = ship.getSpriteAPI();
        float anchorX = shipSprite.getCenterX(); // pixels from left
        float anchorY = shipSprite.getCenterY(); // pixels from bottom

        float left   = -anchorX;
        float right  =  sw - anchorX;
        float bottom = -anchorY;
        float top    =  sh - anchorY;

        sprite.bindTexture();
        GL11.glPushMatrix();
        GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0f);
        GL11.glRotatef(ship.getFacing() - 90f, 0f, 0f, 1f);
        GL11.glColor4f(r, g, b, alpha);

        GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0f,   0f  ); GL11.glVertex2f(left,  bottom);
            GL11.glTexCoord2f(texW, 0f  ); GL11.glVertex2f(right, bottom);
            GL11.glTexCoord2f(texW, texH); GL11.glVertex2f(right, top);
            GL11.glTexCoord2f(0f,   texH); GL11.glVertex2f(left,  top);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    private void renderWeapon(WeaponAPI weapon, float r, float g, float b, float alpha) {
        float wx    = weapon.getLocation().x;
        float wy    = weapon.getLocation().y;
        float angle = weapon.getCurrAngle() - 90f;

        // Under sprite (mount cover) — rendered before the main sprite
        SpriteAPI under = weapon.getUnderSpriteAPI();
        if (under != null) {
            renderWeaponSprite(under, wx, wy, weapon.getShip().getFacing() - 90f, r, g, b, alpha);
        }

        // Barrel rendered below the main sprite if applicable
        SpriteAPI barrel = weapon.getBarrelSpriteAPI();
        if (barrel != null && weapon.isRenderBarrelBelow()) {
            renderWeaponSprite(barrel, wx, wy, angle, r, g, b, alpha);
        }

        // Main sprite (turret / gun)
        SpriteAPI main = weapon.getSprite();
        if (main != null) {
            renderWeaponSprite(main, wx, wy, angle, r, g, b, alpha);
        }

        // Barrel rendered above the main sprite
        if (barrel != null && !weapon.isRenderBarrelBelow()) {
            renderWeaponSprite(barrel, wx, wy, angle, r, g, b, alpha);
        }
    }

    private void renderWeaponSprite(SpriteAPI spr, float wx, float wy, float angleDeg,
                                    float r, float g, float b, float alpha) {
        float texW, texH;
        try {
            texW = spr.getTextureWidth();
            texH = spr.getTextureHeight();
        } catch (Exception e) {
            return; // texture not loaded (decorative weapon, empty slot, etc.)
        }

        float sw = spr.getWidth();
        float sh = spr.getHeight();

        float anchorX = spr.getCenterX();
        float anchorY = spr.getCenterY();

        float left   = -anchorX;
        float right  =  sw - anchorX;
        float bottom = -anchorY;
        float top    =  sh - anchorY;

        spr.bindTexture();
        GL11.glPushMatrix();
        GL11.glTranslatef(wx, wy, 0f);
        GL11.glRotatef(angleDeg, 0f, 0f, 1f);
        GL11.glColor4f(r, g, b, alpha);

        GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0f,   0f  ); GL11.glVertex2f(left,  bottom);
            GL11.glTexCoord2f(texW, 0f  ); GL11.glVertex2f(right, bottom);
            GL11.glTexCoord2f(texW, texH); GL11.glVertex2f(right, top);
            GL11.glTexCoord2f(0f,   texH); GL11.glVertex2f(left,  top);
        GL11.glEnd();

        GL11.glPopMatrix();
    }
}
