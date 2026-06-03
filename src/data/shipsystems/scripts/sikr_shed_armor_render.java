package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.*;

/**
 * BrokenSpriteRenderer
 *
 * Plugin de combat qui génère visuellement un effet de sprite "brisé" au-dessus d'un vaisseau.
 * Les morceaux de forme irrégulière proviennent du sprite du vaisseau et se dispersent
 * vers l'extérieur selon leur position relative au centre.
 *
 * Usage :
 *   Instancier et enregistrer via CombatEngineAPI.addLayeredRenderingPlugin(new BrokenSpriteRenderer(ship))
 *   ou appeler depuis un EveryFrameCombatPlugin.
 */
public class sikr_shed_armor_render extends BaseCombatLayeredRenderingPlugin {

    // -----------------------------------------------------------------------
    // Constantes
    // -----------------------------------------------------------------------

    /** Nombre de fragments générés. */
    private static final int FRAGMENT_COUNT = 18;

    /** Durée totale de l'animation en secondes. */
    private static final float ANIM_DURATION = 2.2f;

    /** Vitesse maximale de dispersion (unités monde / seconde). */
    private static final float MAX_SPEED = 180f;

    /** Rotation maximale d'un fragment (degrés / seconde). */
    private static final float MAX_ROTATION_SPEED = 220f;

    /** Facteur de réduction alpha en fin d'animation. */
    private static final float FADE_START = 0.55f; // fraction de ANIM_DURATION

    // -----------------------------------------------------------------------
    // Structures internes
    // -----------------------------------------------------------------------

    /**
     * Représente un fragment polygonal du sprite.
     * Les coordonnées UV sont normalisées [0..1].
     * Les sommets locaux sont en pixels centrés sur le fragment.
     */
    private static class Fragment {
        /** Sommets du polygone en espace UV (texture). */
        float[] uvX, uvY;
        /** Sommets du polygone en espace local (pixels). */
        float[] lx, ly;
        /** Nombre de sommets. */
        int count;

        /** Position monde au moment du spawn. */
        float worldX, worldY;
        /** Vecteur vitesse de dispersion (pixels monde / s). */
        float velX, velY;
        /** Vitesse de rotation en degrés/s. */
        float rotSpeed;
        /** Angle courant en degrés. */
        float angle;
        /** Temps écoulé depuis le spawn. */
        float age;
        /** Taille de référence pour l'échelle. */
    }

    // -----------------------------------------------------------------------
    // État
    // -----------------------------------------------------------------------

    private final ShipAPI ship;
    private SpriteAPI sprite;
    private final List<Fragment> fragments = new ArrayList<>();
    private boolean initialized = false;
    private boolean finished = false;
    private final Random rng = new Random();

    // -----------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------

    /**
     * @param ship Le vaisseau dont on veut simuler le sprite brisé.
     */
    public sikr_shed_armor_render(ShipAPI ship) {
        this.ship = ship;
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    /**
     * Charge le sprite du vaisseau et génère les fragments polygonaux aléatoires.
     * Appelé une seule fois au premier rendu.
     */
    private void init() {
        // Récupération du sprite depuis les settings Starsector
        // String spritePath = ship.getHullSpec().getSpriteName();
        // sprite = Global.getSettings().getSprite(spritePath);
        sprite = Global.getSettings().getSprite("fx", "sikr_ship_bara");

        float sw = sprite.getWidth();
        float sh = sprite.getHeight();

        // Génération d'un ensemble de "centres" de Voronoï en espace UV [0..1]
        // pour créer des régions irrégulières
        int seedCount = FRAGMENT_COUNT + 4;
        float[] seedU = new float[seedCount];
        float[] seedV = new float[seedCount];
        for (int i = 0; i < seedCount; i++) {
            seedU[i] = rng.nextFloat();
            seedV[i] = rng.nextFloat();
        }

        // Pour chaque fragment : construire un polygone convexe approximant
        // la cellule de Voronoï autour du seed i (méthode par echantillonnage angulaire)
        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            Fragment f = buildVoronoiFragment(seedU, seedV, i, sw, sh);
            if (f == null) continue;

            // Position monde = position du vaisseau + offset selon UV
            float offsetX = (f.worldX - sw * 0.5f);
            float offsetY = (f.worldY - sh * 0.5f);

            // Rotation selon l'angle du vaisseau
            float shipAngleRad = (float) Math.toRadians(ship.getFacing() - 90f);
            float cosA = (float) Math.cos(shipAngleRad);
            float sinA = (float) Math.sin(shipAngleRad);

            float rotOffX = offsetX * cosA - offsetY * sinA;
            float rotOffY = offsetX * sinA + offsetY * cosA;

            f.worldX = ship.getLocation().x + rotOffX;
            f.worldY = ship.getLocation().y + rotOffY;

            // Vitesse : dispersion radiale depuis le centre du vaisseau
            float dx = f.worldX - ship.getLocation().x;
            float dy = f.worldY - ship.getLocation().y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 1f) dist = 1f;

            float speed = MAX_SPEED * (0.4f + rng.nextFloat() * 0.6f);
            f.velX = (dx / dist) * speed + (rng.nextFloat() - 0.5f) * 40f;
            f.velY = (dy / dist) * speed + (rng.nextFloat() - 0.5f) * 40f;
            f.rotSpeed = (rng.nextFloat() - 0.5f) * 2f * MAX_ROTATION_SPEED;
            f.angle = rng.nextFloat() * 360f;
            f.age = 0f;

            fragments.add(f);
        }

        initialized = true;
    }

    /**
     * Construit un fragment polygonal convexe approximant la cellule de Voronoï
     * du seed [idx] parmi tous les seeds.
     *
     * @param su   Coordonnées U des seeds
     * @param sv   Coordonnées V des seeds
     * @param idx  Index du seed cible
     * @param sw   Largeur du sprite (pixels)
     * @param sh   Hauteur du sprite (pixels)
     * @return Fragment ou null si trop petit.
     */
    private Fragment buildVoronoiFragment(float[] su, float[] sv,
                                          int idx, float sw, float sh) {
        int rays = 8 + rng.nextInt(5); // 8 à 12 rayons
        float cx = su[idx];
        float cy = sv[idx];

        float[] polyU = new float[rays];
        float[] polyV = new float[rays];

        for (int r = 0; r < rays; r++) {
            float angle = (float) (2.0 * Math.PI * r / rays);
            // Chercher la frontière Voronoï dans cette direction
            float radius = findVoronoiBoundary(su, sv, idx, cx, cy, angle);
            // Ajouter un peu de bruit organique
            radius *= (0.75f + rng.nextFloat() * 0.25f);
            polyU[r] = cx + radius * (float) Math.cos(angle);
            polyV[r] = cy + radius * (float) Math.sin(angle);
            // Clamper en [0..1]
            polyU[r] = Math.max(0f, Math.min(1f, polyU[r]));
            polyV[r] = Math.max(0f, Math.min(1f, polyV[r]));
        }

        // Convertir UV → pixels locaux centrés sur le centroïde
        float centU = 0f, centV = 0f;
        for (int r = 0; r < rays; r++) { centU += polyU[r]; centV += polyV[r]; }
        centU /= rays;
        centV /= rays;

        float[] lx = new float[rays];
        float[] ly = new float[rays];
        for (int r = 0; r < rays; r++) {
            lx[r] = (polyU[r] - centU) * sw;
            ly[r] = (polyV[r] - centV) * sh;
        }

        // Vérifier que le fragment a une surface minimale
        float area = polygonArea(lx, ly, rays);
        if (area < 4f) return null;

        Fragment f = new Fragment();
        f.uvX = polyU;
        f.uvY = polyV;
        f.lx = lx;
        f.ly = ly;
        f.count = rays;
        f.worldX = centU * sw;
        f.worldY = centV * sh;
        return f;
    }

    /**
     * Cherche la distance à la frontière Voronoï depuis (cx,cy) dans la direction (angle),
     * en cherchant le point mi-chemin vers le voisin le plus proche dans cette direction.
     */
    private float findVoronoiBoundary(float[] su, float[] sv,
                                      int idx, float cx, float cy, float angle) {
        float minDist = Float.MAX_VALUE;
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);

        for (int j = 0; j < su.length; j++) {
            if (j == idx) continue;
            float dx = su[j] - cx;
            float dy = sv[j] - cy;
            // Projection sur la direction
            float proj = dx * cosA + dy * sinA;
            if (proj <= 0f) continue;
            // Frontière de Voronoï ≈ proj / 2 si le point est dans la direction
            if (proj < minDist) minDist = proj;
        }
        return (minDist == Float.MAX_VALUE) ? 0.4f : minDist * 0.5f;
    }

    /** Calcule l'aire d'un polygone (formule du lacet). */
    private float polygonArea(float[] xs, float[] ys, int n) {
        float area = 0f;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            area += (xs[j] + xs[i]) * (ys[j] - ys[i]);
        }
        return Math.abs(area) * 0.5f;
    }

    // -----------------------------------------------------------------------
    // CombatLayeredRenderingPlugin
    // -----------------------------------------------------------------------

    @Override
    public boolean isExpired() {
        return finished;
    }

    @Override
    public float getRenderRadius() {
        // Rayon englobant suffisamment grand pour couvrir la dispersion
        return ship.getCollisionRadius() + MAX_SPEED * ANIM_DURATION + 200f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (finished) return;
        if (!initialized) init();

        float dt = Global.getCombatEngine().getElapsedInLastFrame();
        boolean allDone = true;

        // --- Sauvegarde de l'état OpenGL ---
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        sprite.bindTexture();

        // --- Rendu de chaque fragment ---
        for (Fragment f : fragments) {
            if (f.age >= ANIM_DURATION) continue;
            allDone = false;

            f.age += dt;
            // Mouvement
            f.worldX += f.velX * dt;
            f.worldY += f.velY * dt;
            // Amortissement progressif
            f.velX *= (1f - 0.6f * dt);
            f.velY *= (1f - 0.6f * dt);
            f.angle += f.rotSpeed * dt;

            // Alpha : fade-out dans la seconde moitié de l'animation
            float progress = f.age / ANIM_DURATION;
            float alpha;
            if (progress < FADE_START) {
                alpha = 1f;
            } else {
                alpha = 1f - (progress - FADE_START) / (1f - FADE_START);
            }
            alpha = Math.max(0f, alpha);

            GL11.glColor4f(1f, 1f, 1f, alpha);

            // Transformation locale du fragment
            GL11.glPushMatrix();
            GL11.glTranslatef(f.worldX, f.worldY, 0f);
            GL11.glRotatef(f.angle, 0f, 0f, 1f);

            // Rendu du polygone avec coordonnées de texture
            float texW = sprite.getTextureWidth();
            float texH = sprite.getTextureHeight();

            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            // Centre du fan = centroïde en UV
            float cuv = 0f, cvv = 0f;
            for (int v = 0; v < f.count; v++) { cuv += f.uvX[v]; cvv += f.uvY[v]; }
            cuv /= f.count; cvv /= f.count;
            GL11.glTexCoord2f(cuv * texW, (1f - cvv) * texH);
            GL11.glVertex2f(0f, 0f);

            for (int v = 0; v <= f.count; v++) {
                int vi = v % f.count;
                float u = f.uvX[vi] * texW;
                float tv = (1f - f.uvY[vi]) * texH;
                GL11.glTexCoord2f(u, tv);
                GL11.glVertex2f(f.lx[vi], f.ly[vi]);
            }
            GL11.glEnd();

            GL11.glPopMatrix();
        }

        // --- Restauration de l'état OpenGL ---
        GL11.glPopAttrib();

        if (allDone) finished = true;
    }

    // -----------------------------------------------------------------------
    // API publique
    // -----------------------------------------------------------------------

    /**
     * Enregistre ce plugin dans le moteur de combat.
     * À appeler depuis un EveryFrameCombatPlugin ou un ShipSystemScript.
     *
     * Exemple d'utilisation :
     * <pre>
     *   BrokenSpriteRenderer fx = new BrokenSpriteRenderer(ship);
     *   fx.register(engine);
     * </pre>
     */
    public void register(CombatEngineAPI engine) {
        engine.addLayeredRenderingPlugin(this);
    }

    /**
     * Réinitialise l'animation (recrée les fragments).
     * Utile pour rejouer l'effet sans réinstancier.
     */
    public void reset() {
        fragments.clear();
        initialized = false;
        finished = false;
    }
}