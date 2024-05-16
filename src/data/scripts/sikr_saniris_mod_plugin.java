package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
//import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.json.JSONArray;
import org.json.JSONObject;

import data.scripts.sikr_trail_data_store.sikr_trailData;
import data.scripts.world.sikr_saniris_gen;
import exerelin.campaign.SectorManager;

public class sikr_saniris_mod_plugin extends BaseModPlugin {

    @Override
	public void onApplicationLoad() throws Exception {  
        super.onApplicationLoad();

        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.lazywizard.lazylib.ModUtils");
        } catch (ClassNotFoundException ex) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "LazyLib is required to run at least one of the mods you have installed."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download LazyLib at http://fractalsoftworks.com/forum/index.php?topic=5444"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }
        
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.magiclib.util.MagicAnim");
        } catch (ClassNotFoundException ex) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "MagicLib is required to run at least one of the mods you have installed."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download MagicLib at http://fractalsoftworks.com/forum/index.php?topic=13718.0"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }

        if (Global.getSettings().getModManager().isModEnabled("shaderLib")) { // && Global.getSettings().loadJSON("GRAPHICS_OPTIONS.ini").getBoolean("enableShaders")
            ShaderLib.init();
            TextureData.readTextureDataCSV("data/lights/sikr_sanIris_texture.csv");
            //LightData.readLightDataCSV("data/lights/yourlightdata.csv");
        }

        try {
            JSONArray trailspec = Global.getSettings().loadCSV("data/config/modFiles/sikr_trail_data.csv", "sikr");

            for (int i = 0; i < trailspec.length(); i++) {
                JSONObject j = trailspec.getJSONObject(i);

                if(j.getString("shipId").equals("") || j.getString("shipId").startsWith("#")) continue;

                sikr_trailData spec = new sikr_trailData(
                        j.getString("shipId"),
                        j.getString("startSpeed"),
                        j.getString("endSpeed"),
                        j.getString("startAngular"),
                        j.getString("endAngular"),
                        j.getString("startSize"),
                        j.getString("endSize"),
                        j.getString("colorIn"),
                        j.getString("colorOut"),
                        j.getString("inDuration"),
                        j.getString("mainDuration"),
                        j.getString("outDuration"),
                        j.getString("loopLength"),
                        j.getString("loopSpeed")
                );

                sikr_trail_data_store.addToTrailList(spec);
            }

        } catch (Exception ex) {
            throw new Exception("San-Iris trails failed to load.");
        }
    }	

    @Override
    public void onNewGame() {
        boolean have_nex = Global.getSettings().getModManager().isModEnabled("nexerelin");
        boolean have_indEvo = Global.getSettings().getModManager().isModEnabled("IndEvo");
        if(!have_nex || SectorManager.getManager().isCorvusMode()){
            new sikr_saniris_gen().generate(Global.getSector());
            //indEvo integration
            if(have_indEvo){
                MarketAPI sikr_iris_market = Global.getSector().getEconomy().getMarket("sikr_iris_market");
                sikr_iris_market.addIndustry("IndEvo_Artillery_mortar");
            }
        }
    }

    public void onNewGameAfterEconomyLoad(){

        sikr_lilies_gen.generate_lilies();
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        PersonAPI sikr_lily_yellow = ip.getPerson("sikr_lily_yellow");
        // PersonAPI sikr_lily_white = ip.getPerson("sikr_lily_white");
        // PersonAPI sikr_lily_pink = ip.getPerson("sikr_lily_pink");
        // PersonAPI sikr_lily_orange = ip.getPerson("sikr_lily_orange");
        // PersonAPI sikr_lily_red = ip.getPerson("sikr_lily_red");
        // PersonAPI sikr_lily_purple = ip.getPerson("sikr_lily_purple");
        // PersonAPI sikr_lily_blue = ip.getPerson("sikr_lily_blue");

        //yellow
        sikr_lilies_gen.spawnYellowToSanIris(sikr_lily_yellow);
        //white
        // sikr_lilies_gen.spawnWhiteToIndependent(sikr_lily_white);
        //pink
        // sikr_lilies_gen.spawnPinkToTriTachyon(sikr_lily_pink);
        //orange
        // sikr_lilies_gen.spawnOrangeToHegemony(sikr_lily_orange);
        //red
        // sikr_lilies_gen.spawnRedToPirates(sikr_lily_red);
        //purple
        // sikr_lilies_gen.spawnPurpleToChurch(sikr_lily_purple);
        //blue
        // sikr_lilies_gen.spawnBlueToRemnant(sikr_lily_blue);
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            default:        
        }
        return null;	
    }
}