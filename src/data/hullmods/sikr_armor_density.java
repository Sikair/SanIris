package data.hullmods;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import javax.swing.ImageIcon;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

import org.lwjgl.util.vector.Vector2f;

import org.magiclib.util.MagicAnim;

public class sikr_armor_density extends BaseHullMod{

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        ColorModel armor_density = null;
        BufferedImage img = null;
        ArmorGridAPI armor_grid = ship.getArmorGrid();
        //int size = 0;
        /*try {
            //ship.getHullSpec().getShipFilePath();
            //img = ImageIO.read(new File("graphics/ships/"+ship.getHullSpec().getSpriteName()+"_ad1.png"));
            img = ImageIO.read(new File("n:\\Projet perso\\Mods starsector\\Starsector\\mods\\San-Iris\\graphics\\ships\\sikr_bara_ad1.png"));    
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        
        ImageIcon icon = new ImageIcon("n:\\Projet perso\\Mods starsector\\Starsector\\mods\\San-Iris\\graphics\\ships\\sikr_bara_ad2.png");
        img = toBufferedImage(icon.getImage());

        armor_density = img.getColorModel();
        if(armor_density != null){
            for(int x = 0; x < img.getWidth(); x++){
                for(int y = 0; y < img.getHeight(); y++){
                    //pixel   = rgbArray[offset + (y-startY)*scansize + (x-startX)];
                    int pixel = img.getRGB(x, y);
                    //Global.getCombatEngine().addFloatingText(Vector2f.add(ship.getLocation(), new Vector2f(x*5,y*5), new Vector2f()), "o", 10, new Color(255,255,255), ship, 5, 5);
                    int red = armor_density.getRed(pixel);
                    float alpha = MagicAnim.normalizeRange(red, 0, 255);
                    armor_grid.setArmorValue(x, y, (alpha) * (armor_grid.getArmorRating() / 15)); //divided by 15 for reason
                }
            }
        }
        showGrid(ship);
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public void showGrid(ShipAPI ship){
        ArmorGridAPI grid = ship.getArmorGrid();
        if (grid == null) return;
        int gridWidth = grid.getGrid().length;
        int gridHeight = grid.getGrid()[0].length;
        //float maxArmorInCell = grid.getMaxArmorInCell();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Vector2f point = grid.getLocation(x, y);
                float cellsize = grid.getCellSize();
                Global.getCombatEngine().addNebulaParticle(point, ship.getVelocity(), cellsize, 1,1,2,10,new Color(255,255,255)); 
                //Global.getCombatEngine().addFloatingText(point, ((int) ship.getArmorGrid().getArmorValue(x, y)) +"", 10, new Color(255,255,255), ship, 10, 10);
            }
        }
    }
}
