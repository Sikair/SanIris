package data.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.magiclib.util.MagicCampaign;
import org.magiclib.util.MagicVariables;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;

public class sikr_lilies_gen {
    public static void generate_lilies(){

        //Yellow Lily
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        PersonAPI sikr_lily_yellow = Global.getFactory().createPerson();
        sikr_lily_yellow.getName().setFirst("Lily");
        //sikr_lily_yellow.getName().setFirst("Aurelia");
        sikr_lily_yellow.getName().setLast("");
        sikr_lily_yellow.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_yellow"));
        sikr_lily_yellow.setGender(Gender.FEMALE);
        sikr_lily_yellow.setFaction("sikr_saniris");
        sikr_lily_yellow.setRankId(Ranks.FACTION_LEADER);
        sikr_lily_yellow.setPostId(Ranks.POST_ADMINISTRATOR);
        sikr_lily_yellow.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
        sikr_lily_yellow.setPersonality(Personalities.TIMID);
        sikr_lily_yellow.setVoice(Voices.ARISTO);
        sikr_lily_yellow.setId("sikr_lily_yellow");

        ip.addPerson(sikr_lily_yellow);
        
        //White Lily
        PersonAPI sikr_lily_white = Global.getFactory().createPerson();
        sikr_lily_white.getName().setFirst("Alina");
        sikr_lily_white.getName().setLast("");
        sikr_lily_white.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_white"));
        sikr_lily_white.setGender(Gender.FEMALE);
        sikr_lily_white.setFaction("sikr_saniris");
        sikr_lily_white.setRankId(Ranks.AGENT);
        sikr_lily_white.setPostId(Ranks.POST_AGENT);
        //sikr_lily_white.getStats().setSkillLevel(Skills., 1);
        sikr_lily_white.setPersonality(Personalities.STEADY);
        sikr_lily_white.setVoice(Voices.OFFICIAL);
        sikr_lily_white.setId("sikr_lily_white");

        ip.addPerson(sikr_lily_white);

        //Pink Lily
        PersonAPI sikr_lily_pink = Global.getFactory().createPerson();
        sikr_lily_pink.getName().setFirst("Primrose");
        sikr_lily_pink.getName().setLast("");
        sikr_lily_pink.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_pink"));
        sikr_lily_pink.setGender(Gender.FEMALE);
        sikr_lily_pink.setFaction("sikr_saniris");
        sikr_lily_pink.setRankId(Ranks.AGENT);
        sikr_lily_pink.setPostId(Ranks.POST_AGENT);
        //sikr_lily_pink.getStats().setSkillLevel(Skills., 1);
        sikr_lily_pink.setPersonality(Personalities.CAUTIOUS);
        sikr_lily_pink.setVoice(Voices.BUSINESS);
        sikr_lily_pink.setId("sikr_lily_pink");

        ip.addPerson(sikr_lily_pink);

        //Orange Lily
        PersonAPI sikr_lily_orange = Global.getFactory().createPerson();
        sikr_lily_orange.getName().setFirst("Marigold");
        sikr_lily_orange.getName().setLast("");
        sikr_lily_orange.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_orange"));
        sikr_lily_orange.setGender(Gender.FEMALE);
        sikr_lily_orange.setFaction("sikr_saniris");
        sikr_lily_orange.setRankId(Ranks.AGENT);
        sikr_lily_orange.setPostId(Ranks.POST_AGENT);
        //sikr_lily_orange.getStats().setSkillLevel(Skills., 1);
        sikr_lily_orange.setPersonality(Personalities.AGGRESSIVE);
        sikr_lily_orange.setVoice(Voices.SOLDIER);
        sikr_lily_orange.setId("sikr_lily_orange");

        ip.addPerson(sikr_lily_orange);

        //Red Lily
        PersonAPI sikr_lily_red = Global.getFactory().createPerson();
        sikr_lily_red.getName().setFirst("Scarlett");
        sikr_lily_red.getName().setLast("");
        sikr_lily_red.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_red"));
        sikr_lily_red.setGender(Gender.FEMALE);
        sikr_lily_red.setFaction("sikr_saniris");
        sikr_lily_red.setRankId(Ranks.AGENT);
        sikr_lily_red.setPostId(Ranks.POST_AGENT);
        //sikr_lily_red.getStats().setSkillLevel(Skills., 1);
        sikr_lily_red.setPersonality(Personalities.AGGRESSIVE);
        sikr_lily_red.setVoice(Voices.SPACER);
        sikr_lily_red.setId("sikr_lily_red");

        ip.addPerson(sikr_lily_red);

        //Purple Lily
        PersonAPI sikr_lily_purple = Global.getFactory().createPerson();
        sikr_lily_purple.getName().setFirst("Violet");
        sikr_lily_purple.getName().setLast("");
        sikr_lily_purple.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_purple"));
        sikr_lily_purple.setGender(Gender.FEMALE);
        sikr_lily_purple.setFaction("sikr_saniris");
        sikr_lily_purple.setRankId(Ranks.AGENT);
        sikr_lily_purple.setPostId(Ranks.POST_AGENT);
        //sikr_lily_purple.getStats().setSkillLevel(Skills., 1);
        sikr_lily_purple.setPersonality(Personalities.STEADY);
        sikr_lily_purple.setVoice(Voices.FAITHFUL);
        sikr_lily_purple.setId("sikr_lily_purple");

        ip.addPerson(sikr_lily_purple);

        //Blue Lily
        PersonAPI sikr_lily_blue = Global.getFactory().createPerson();
        sikr_lily_blue.getName().setFirst("Lapis");
        sikr_lily_blue.getName().setLast("");
        sikr_lily_blue.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sikr_lily_blue"));
        sikr_lily_blue.setGender(Gender.FEMALE);
        sikr_lily_blue.setFaction("sikr_saniris");
        sikr_lily_blue.setRankId(Ranks.AGENT);
        sikr_lily_blue.setPostId(Ranks.POST_AGENT);
        //sikr_lily_blue.getStats().setSkillLevel(Skills., 1);
        sikr_lily_blue.setPersonality(Personalities.AGGRESSIVE);
        sikr_lily_blue.setVoice(Voices.FAITHFUL);
        sikr_lily_blue.setId("sikr_lily_blue");

        ip.addPerson(sikr_lily_blue);

        //log.debug("added character name of " + spec.name);
    }

    //DEBUG
    static MarketAPI sikr_iris_market = Global.getSector().getEconomy().getMarket("sikr_iris_market");

    public static void spawnYellowToSanIris(PersonAPI yellow){
        MarketAPI sikr_iris_market = Global.getSector().getEconomy().getMarket("sikr_iris_market");
        MarketAPI sikr_lily_market = Global.getSector().getEconomy().getMarket("sikr_lily_market");

        if(sikr_iris_market != null){
            sikr_iris_market.setAdmin(yellow);
            sikr_iris_market.addPerson(yellow);
            //sikr_iris_market.getCommDirectory().addPerson(person, 0);
            if(sikr_lily_market != null){
                sikr_lily_market.setAdmin(yellow);
                sikr_lily_market.addPerson(yellow);
                //sikr_lily_market.getCommDirectory().addPerson(person, 0);
            }
        }else{
            //if no main planet settle for the largest market
            SectorEntityToken target = null;
            for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
                if(m.getFaction().getId().equals("sikr_saniris")){
                    if(target==null || m.getSize() > target.getMarket().getSize()){
                        target = m.getPrimaryEntity();
                    }
                }
            }
            if(target != null) {
                target.getMarket().setAdmin(yellow);
                target.getMarket().addPerson(yellow);
            }
        }
    }

    public static void spawnWhiteToIndependent(PersonAPI white){
        //settle for the largest military market
        SectorEntityToken target = null;
        
        for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
            if(m.getFaction().getId().equals(Factions.INDEPENDENT)){
                if(target==null || (!m.hasSubmarket(Submarkets.GENERIC_MILITARY) && (target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY) || m.getSize()>target.getMarket().getSize()))){
                    target=m.getPrimaryEntity();
                }
            }
        }

        if(target != null){
            Map<String, Integer> supportFleet = new HashMap<>();
            supportFleet.put("sikr_veko_standard",1);
            supportFleet.put("sikr_bara_standard",1);
            supportFleet.put("sikr_shion_standard",2);


            CampaignFleetAPI whiteFleet = MagicCampaign.createFleetBuilder()
            .setFleetName("White Fleet")
            .setFleetFaction(Factions.INDEPENDENT)
            .setFleetType(FleetTypes.PATROL_LARGE)
            .setFlagshipName("Ender Garden")
            .setFlagshipVariant("sikr_leviathan_off_assault")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(false)
            .setCaptain(white)
            .setSupportFleet(supportFleet)
            .setSupportAutofit(true)
            .setMinFP(200)
            .setReinforcementFaction(Factions.INDEPENDENT)
            .setQualityOverride(2f)
            .setSpawnLocation(null)
            .setAssignment(FleetAssignment.PATROL_SYSTEM)
            .setAssignmentTarget(target)
            .setIsImportant(true)
            .setTransponderOn(true)
            .create();
            
            whiteFleet.setDiscoverable(false);
            whiteFleet.addTag(Tags.NEUTRINO);
            //whiteFleet.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat("id_unique", -2000f);
            //whiteFleet.addEventListener(new Diableavionics_gulfLoot());

            //DEBUG
            //target.getStarSystem().setBaseName("WHITE");
            return;
        }
    }

    public static void spawnPinkToTriTachyon(PersonAPI pink){
        //settle for the largest market
        SectorEntityToken target=null;
        
        for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
            if(m.getFaction().getId().equals(Factions.TRITACHYON)){
                if(target == null || m.getSize() > target.getMarket().getSize()){
                    target = m.getPrimaryEntity();
                }
            }
        }

        if(target != null){
            Map<String, Integer> supportFleet = new HashMap<>();
            supportFleet.put("sikr_unnr_escort",2);
            supportFleet.put("sikr_himin_disrupt",3);


            CampaignFleetAPI pinkFleet = MagicCampaign.createFleetBuilder()
            .setFleetName("Pink Fleet")
            .setFleetFaction(Factions.TRITACHYON)
            .setFleetType(FleetTypes.TASK_FORCE)
            .setFlagshipName("Emporium")
            .setFlagshipVariant("sikr_manta_medium")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(false)
            .setCaptain(pink)
            .setSupportFleet(supportFleet)
            .setSupportAutofit(true)
            .setMinFP(200)
            .setReinforcementFaction(Factions.TRITACHYON)
            .setQualityOverride(2f)
            .setSpawnLocation(null)
            .setAssignment(FleetAssignment.PATROL_SYSTEM)
            .setAssignmentTarget(target)
            .setIsImportant(true)
            .setTransponderOn(true)
            .create();
            
            pinkFleet.setDiscoverable(false);
            pinkFleet.addTag(Tags.NEUTRINO);
            //orangeFleet.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat("id_unique", -2000f);
            //orangeFleet.addEventListener(new Diableavionics_gulfLoot());
            return;
        }
    }

    public static void spawnOrangeToHegemony(PersonAPI orange){
        //settle for the largest military market
        SectorEntityToken target=null;
        
        for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
            if(m.getFaction().getId().equals(Factions.HEGEMONY)){
                if(target == null || 
                    (m.hasSubmarket(Submarkets.GENERIC_MILITARY) && 
                        (!target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY) ||
                            m.getSize() > target.getMarket().getSize()))){
                    target=m.getPrimaryEntity();
                }
            }
        }

        if(target != null){
            Map<String, Integer> supportFleet = new HashMap<>();
            supportFleet.put("sikr_veko_standard",2);
            supportFleet.put("sikr_vine_standard",2);


            CampaignFleetAPI orangeFleet = MagicCampaign.createFleetBuilder()
            .setFleetName("Orange Fleet")
            .setFleetFaction(Factions.HEGEMONY)
            .setFleetType(FleetTypes.TASK_FORCE)
            .setFlagshipName("Brazier")
            .setFlagshipVariant("sikr_leviathan_def_assault")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(false)
            .setCaptain(orange)
            .setSupportFleet(supportFleet)
            .setSupportAutofit(true)
            .setMinFP(200)
            .setReinforcementFaction(Factions.HEGEMONY)
            .setQualityOverride(2f)
            .setSpawnLocation(null)
            .setAssignment(FleetAssignment.PATROL_SYSTEM)
            .setAssignmentTarget(target)
            .setIsImportant(true)
            .setTransponderOn(true)
            .create();
            
            orangeFleet.setDiscoverable(false);
            orangeFleet.addTag(Tags.NEUTRINO);
            //orangeFleet.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat("id_unique", -2000f);
            //orangeFleet.addEventListener(new Diableavionics_gulfLoot());
            return;
        }
    }

    public static void spawnRedToPirates(PersonAPI red){
        //settle for the largest non military market
        SectorEntityToken target=null;
        
        for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
            if(m.getFaction().getId().equals(Factions.PIRATES)){
                if(target == null || 
                    (!m.hasSubmarket(Submarkets.GENERIC_MILITARY) && 
                        (target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY) ||
                            m.getSize() > target.getMarket().getSize()))){
                    target=m.getPrimaryEntity();
                }
            }
        }

        if(target!=null){
            Map<String, Integer> supportFleet = new HashMap<>();
            supportFleet.put("sikr_bara_pressure",1);
            supportFleet.put("sikr_marus_standard",1);
            supportFleet.put("sikr_echin_escort",2);
            supportFleet.put("sikr_dufa_breaker",1);


            CampaignFleetAPI redFleet = MagicCampaign.createFleetBuilder()
            .setFleetName("Red Fleet")
            .setFleetFaction(Factions.PIRATES)
            .setFleetType(FleetTypes.TASK_FORCE)
            .setFlagshipName("Scarlet Tear")
            .setFlagshipVariant("sikr_leviathan_off_breaker")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(false)
            .setCaptain(red)
            .setSupportFleet(supportFleet)
            .setSupportAutofit(true)
            .setMinFP(180)
            .setReinforcementFaction(Factions.PIRATES)
            .setQualityOverride(2f)
            .setSpawnLocation(null)
            .setAssignment(FleetAssignment.ORBIT_AGGRESSIVE)
            .setAssignmentTarget(target)
            .setIsImportant(true)
            .setTransponderOn(true)
            .create();

            //DEBUG
            //target.getStarSystem().setBaseName("RED");
            
            redFleet.setDiscoverable(false);
            redFleet.addTag(Tags.NEUTRINO);
            //orangeFleet.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat("id_unique", -2000f);
            //orangeFleet.addEventListener(new Diableavionics_gulfLoot());
            return;
        }
    }

    public static void spawnPurpleToChurch(PersonAPI purple){
        //settle for the largest market
        SectorEntityToken target = null;
        
        for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
            if(m.getFaction().getId().equals(Factions.LUDDIC_CHURCH)){
                if(target == null || m.getSize() > target.getMarket().getSize()){
                    target = m.getPrimaryEntity();
                }
            }
        }

        if(target!=null){
            Map<String, Integer> supportFleet = new HashMap<>();
            supportFleet.put("sikr_unnr_escort",2);
            supportFleet.put("sikr_veko_standard",2);


            CampaignFleetAPI purpleFleet = MagicCampaign.createFleetBuilder()
            .setFleetName("Purple Fleet")
            .setFleetFaction(Factions.LUDDIC_CHURCH)
            .setFleetType(FleetTypes.TASK_FORCE)
            .setFlagshipName("Lost Lamb")
            .setFlagshipVariant("sikr_veko_artillery")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(false)
            .setCaptain(purple)
            .setSupportFleet(supportFleet)
            .setSupportAutofit(true)
            .setMinFP(200)
            .setReinforcementFaction(Factions.LUDDIC_CHURCH)
            .setQualityOverride(2f)
            .setSpawnLocation(null)
            .setAssignment(FleetAssignment.ORBIT_PASSIVE)
            .setAssignmentTarget(target)
            .setIsImportant(true)
            .setTransponderOn(true)
            .create();
            
            purpleFleet.setDiscoverable(false);
            purpleFleet.addTag(Tags.NEUTRINO);
            //orangeFleet.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat("id_unique", -2000f);
            //orangeFleet.addEventListener(new Diableavionics_gulfLoot());
            return;
        }
    }

    public static void spawnBlueToRemnant(PersonAPI blue){
        //settle for a market
        SectorEntityToken target = null;
        
        // for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
        //     if(m.getFaction().getId().equals(Factions.REMNANTS)){
        //         if(target==null){
        //             target=m.getPrimaryEntity();
        //             break;
        //         }
        //     }
        // }

        for (int i = 0; i < 9; i++) {
            List<String> market = new ArrayList<>();
            market.add(Factions.REMNANTS);

            List<String> themes = new ArrayList<>();
            themes.add(Tags.BEACON_HIGH);
            themes.add(Tags.THEME_REMNANT);

            List<String> notThemes = new ArrayList<>();
            notThemes.add(MagicVariables.AVOID_BLACKHOLE_PULSAR);
            notThemes.add(Tags.THEME_SPECIAL);

            List<String> entities = new ArrayList<>();
            themes.add(Tags.PLANET);

            SectorEntityToken token = MagicCampaign.findSuitableTarget(null, null, "CLOSE", themes, notThemes, entities, false, false, false);

            if(token != null){
                target = token;
                break;
            }
        }

        
        if(target!=null){
            Map<String, Integer> supportFleet = new HashMap<>();
            supportFleet.put("sikr_lehal_disrupt",2);

            CampaignFleetAPI blueFleet = MagicCampaign.createFleetBuilder()
            .setFleetName("Blue Fleet")
            .setFleetFaction(Factions.REMNANTS)
            .setFleetType(FleetTypes.TASK_FORCE)
            .setFlagshipName("Flying Fish")
            .setFlagshipVariant("sikr_nuyre_standard")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(false)
            .setCaptain(blue)
            .setSupportFleet(supportFleet)
            .setSupportAutofit(true)
            .setMinFP(200)
            .setReinforcementFaction(Factions.REMNANTS)
            .setQualityOverride(2f)
            .setSpawnLocation(null)
            .setAssignment(FleetAssignment.PATROL_SYSTEM)
            .setAssignmentTarget(target)
            .setIsImportant(true)
            .setTransponderOn(true)
            .create();
            
            blueFleet.setDiscoverable(true);
            blueFleet.addTag(Tags.NEUTRINO);
            //orangeFleet.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat("id_unique", -2000f);
            //orangeFleet.addEventListener(new Diableavionics_gulfLoot());

            //DEBUG
            target.getStarSystem().setBaseName("BLUE");

            return;
        }else{
            sikr_iris_market.getCommDirectory().addPerson(blue, 0);
        }
    }

}
