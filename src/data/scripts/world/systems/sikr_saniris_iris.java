package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import data.scripts.world.sikr_market_gen;

import java.awt.Color;
import java.util.Arrays;

public class sikr_saniris_iris implements SectorGeneratorPlugin {

        private final float IRIS_DISTANCE = 3200;
        private final float LILY_DISTANCE = 4600;
        private final float HYACINTH_DISTANCE = 6200;

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Edel");
        //LocationAPI hyper = Global.getSector().getHyperspace();
        system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");

        // create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("sikr_edel", // unique id for this star
                                                "star_white", // id in planets.json
						550f,
                                                350);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(255, 250, 40)); // light color in entire system, affects all entities

	system.getLocation().set(-4000, -26000);
        /*
         * addPlanet() parameters:
         * 1. What the planet orbits (orbit is always circular)
         * 2. Name
         * 3. Planet type id in planets.json
         * 4. Starting angle in orbit, i.e. 0 = to the right of the star
         * 5. Planet radius, pixels at default zoom
         * 6. Orbit radius, pixels at default zoom
         * 7. Days it takes to complete an orbit. 1 day = 10 seconds.
         */
        /*
         * addAsteroidBelt() parameters:
         * 1. What the belt orbits
         * 2. Number of asteroids
         * 3. Orbit radius
         * 4. Belt width
         * 6/7. Range of days to complete one orbit. Value picked randomly for each asteroid. 
         */
        /*
         * addRingBand() parameters:
         * 1. What it orbits
         * 2. Category under "graphics" in settings.json
         * 3. Key in category
         * 4. Width of band within the texture
         * 5. Index of band
         * 6. Color to apply to band
         * 7. Width of band (in the game)
         * 8. Orbit radius (of the middle of the band)
         * 9. Orbital period, in days
         */
//        private void addMarketplace(
//                    String factionID, 
//                    SectorEntityToken primaryEntity, 
//                    ArrayList<SectorEntityToken> connectedEntities, 
//                    String name, 
//                    int size, 
//                    ArrayList<String> marketConditions, 
//                    ArrayList<String> submarkets, 
//                    float tarriff    

        system.addRingBand(star, "misc", "rings_ice0", 256f, 3, Color.gray, 256f, IRIS_DISTANCE - 200, 250f, Terrain.RING, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 0, Color.gray, 256f, IRIS_DISTANCE, 350f, Terrain.RING, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.gray, 256f, IRIS_DISTANCE + 200, 400f, Terrain.RING , null); 
        

        PlanetAPI sikr_iris = system.addPlanet("sikr_iris",
                star,
                "Iris",
                "water",
                360f * (float) Math.random(),
                120,
                IRIS_DISTANCE,
                120
        );
        sikr_iris.setCustomDescriptionId("sikr_iris_desc");

        MarketAPI sikr_iris_market = sikr_market_gen.addMarketplace("sikr_saniris", sikr_iris, null, "Iris", 8, 
        Arrays.asList(
                Conditions.POPULATION_8,
                Conditions.WATER_SURFACE,
                "sikr_water_tech",
                Conditions.MILD_CLIMATE,
                Conditions.REGIONAL_CAPITAL,
                Conditions.CLOSED_IMMIGRATION,
                Conditions.HABITABLE,
                Conditions.STEALTH_MINEFIELDS,
                Conditions.ORE_MODERATE,
                Conditions.RARE_ORE_SPARSE
        ), 
        Arrays.asList(
                Submarkets.SUBMARKET_OPEN,
                Submarkets.SUBMARKET_STORAGE,
                Submarkets.GENERIC_MILITARY,
                Submarkets.SUBMARKET_BLACK
        ), 
        Arrays.asList(
                Industries.POPULATION,
                Industries.AQUACULTURE,
                Industries.HEAVYBATTERIES,
                Industries.STARFORTRESS_HIGH,
                Industries.MEGAPORT,
                //Industries.WAYSTATION,
                Industries.ORBITALWORKS,
                //"sikr_underwater_industry",
                Industries.MINING,
                Industries.HIGHCOMMAND
                //"sikr_palace"
        ), 0.18f, false, true);

        sikr_iris.setMarket(sikr_iris_market);
        
            //JUMP POINT
            JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("sikr_iris_sys_jp1",
                    "Edel Jump-point"
            );
            OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 85, 2000, 150);
            jumpPoint1.setOrbit(orbit);
            jumpPoint1.setRelatedPlanet(sikr_iris);
            jumpPoint1.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jumpPoint1);
        
            
        PlanetAPI sikr_lily = system.addPlanet("sikr_lily",
                star,
                "Lily",
                "water",
                360f * (float) Math.random(),
                210,
                LILY_DISTANCE,
                130
        );
        sikr_lily.setCustomDescriptionId("sikr_lily_desc");
        
        MarketAPI sikr_lily_market = sikr_market_gen.addMarketplace("sikr_saniris", sikr_lily, null, "Lily", 6, 
        Arrays.asList(
                Conditions.POPULATION_6,
                Conditions.WATER_SURFACE,
                "sikr_water_tech",
                Conditions.MILD_CLIMATE,
                Conditions.CLOSED_IMMIGRATION,
                Conditions.HABITABLE, 
                Conditions.ORE_ABUNDANT,
                Conditions.RARE_ORE_RICH,
                Conditions.ORGANICS_COMMON
        ), 
        Arrays.asList(
                Submarkets.SUBMARKET_OPEN,
                Submarkets.SUBMARKET_STORAGE,
                Submarkets.GENERIC_MILITARY,
                Submarkets.SUBMARKET_BLACK
        ), 
        Arrays.asList(
                Industries.POPULATION,
                Industries.AQUACULTURE,
                Industries.HEAVYBATTERIES,
                Industries.STARFORTRESS_HIGH,
                Industries.MEGAPORT,
                //Industries.WAYSTATION,
                Industries.LIGHTINDUSTRY,
                Industries.MINING,
                Industries.REFINING,
                Industries.PATROLHQ
                //"sikr_embassy"
        ), 0.18f, false, true);

        sikr_lily.setMarket(sikr_lily_market);
        
        PlanetAPI sikr_hyacinth = system.addPlanet("sikr_hyacinth",
                star,
                "Hyacinth",
                "gas_giant",
                360f * (float) Math.random(),
                350,
                HYACINTH_DISTANCE,
                190
        );
        //sikr_hyacinth.setCustomDescriptionId("sikr_hyacinth");
        PlanetConditionGenerator.generateConditionsForPlanet(sikr_hyacinth, StarAge.AVERAGE);
        
        
        StarSystemGenerator.addStableLocations(system, 3);
        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                        1, 3, // min/max entities to add
                        HYACINTH_DISTANCE + 600, // radius to start adding at 
                        2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                        true); // whether to use custom or system-name based names
            
        system.autogenerateHyperspaceJumpPoints(true, true, true);

        cleanup(system);
    }
    
    void cleanup(StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
	NebulaEditor editor = new NebulaEditor(plugin);        
        float minRadius = plugin.getTileSize() * 2f;
        
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}
