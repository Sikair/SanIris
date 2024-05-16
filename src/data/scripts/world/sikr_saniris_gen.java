package data.scripts.world;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import data.scripts.world.systems.sikr_saniris_iris;

public class sikr_saniris_gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
	
        new sikr_saniris_iris().generate(sector);

        //SharedData.getData().getPersonBountyEventData().addParticipatingFaction("sikr_saniris");                      

        FactionAPI saniris = sector.getFaction("sikr_saniris");
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT); 
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);   	
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT); 
        FactionAPI kol = sector.getFaction(Factions.KOL);	
	    FactionAPI persean = sector.getFaction(Factions.PERSEAN);
        FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);
        FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
        FactionAPI derelict = sector.getFaction(Factions.DERELICT);
        
        //vanilla factions
        saniris.setRelationship(guard.getId(), RepLevel.NEUTRAL);    
        saniris.setRelationship(diktat.getId(), RepLevel.FAVORABLE); 
        saniris.setRelationship(player.getId(), RepLevel.SUSPICIOUS);
        saniris.setRelationship(independent.getId(), RepLevel.INHOSPITABLE);  
        saniris.setRelationship(tritachyon.getId(), RepLevel.INHOSPITABLE);      
        saniris.setRelationship(pirates.getId(), RepLevel.HOSTILE);
        saniris.setRelationship(persean.getId(), RepLevel.HOSTILE);	
        saniris.setRelationship(kol.getId(), RepLevel.INHOSPITABLE); 
        saniris.setRelationship(hegemony.getId(), RepLevel.FAVORABLE);
        saniris.setRelationship(path.getId(), RepLevel.HOSTILE);   
        saniris.setRelationship(church.getId(), RepLevel.FRIENDLY);   
        
        //environment
        saniris.setRelationship(remnant.getId(), RepLevel.HOSTILE);	
        saniris.setRelationship(derelict.getId(), RepLevel.HOSTILE);     
        
        //mods        
        saniris.setRelationship("sun_ici", RepLevel.FAVORABLE);       
        
        saniris.setRelationship("cabal", RepLevel.NEUTRAL);    
        saniris.setRelationship("crystanite", RepLevel.NEUTRAL); 
        saniris.setRelationship("mayorate", RepLevel.NEUTRAL);	 
        saniris.setRelationship("pirateAnar", RepLevel.NEUTRAL);	 
        saniris.setRelationship("exipirated", RepLevel.NEUTRAL);	 
        
        saniris.setRelationship("exigency", RepLevel.SUSPICIOUS);
        saniris.setRelationship("syndicate_asp", RepLevel.SUSPICIOUS);	   
        saniris.setRelationship("tiandong", RepLevel.SUSPICIOUS);        
        saniris.setRelationship("SCY", RepLevel.SUSPICIOUS);     
        saniris.setRelationship("neutrinocorp", RepLevel.SUSPICIOUS); 
        saniris.setRelationship("interstellarimperium", RepLevel.SUSPICIOUS);            
        
        saniris.setRelationship("6eme_bureau", RepLevel.INHOSPITABLE);	
        saniris.setRelationship("dassault_mikoyan", RepLevel.INHOSPITABLE);
        saniris.setRelationship("pack", RepLevel.INHOSPITABLE);     
        saniris.setRelationship("blackrock_driveyards", RepLevel.INHOSPITABLE);	   
        saniris.setRelationship("citadeldefenders", RepLevel.INHOSPITABLE);
        saniris.setRelationship("pn_colony", RepLevel.INHOSPITABLE);    
        saniris.setRelationship("junk_pirates", RepLevel.INHOSPITABLE);  
        saniris.setRelationship("sun_ice", RepLevel.INHOSPITABLE);          
            
        saniris.setRelationship("shadow_industry", RepLevel.HOSTILE);	
        saniris.setRelationship("ORA", RepLevel.HOSTILE);     
        saniris.setRelationship("blade_breakers", RepLevel.HOSTILE);   
        saniris.setRelationship("new_galactic_order", RepLevel.HOSTILE);	
        saniris.setRelationship("explorer_society", RepLevel.HOSTILE);
        
        saniris.setRelationship("Coalition", -0.2f);      
        saniris.setRelationship("metelson", -0.2f);     
        saniris.setRelationship("the_deserter", 0.35f);    
        saniris.setRelationship("noir", 0.0f);     
        saniris.setRelationship("Lte", 0.0f);  
        saniris.setRelationship("GKSec", 0.1f); 
        saniris.setRelationship("gmda", -0.1f);   
        saniris.setRelationship("oculus", -0.25f);     
        saniris.setRelationship("nomads", -0.25f); 
        saniris.setRelationship("thulelegacy", -0.25f); 
        saniris.setRelationship("infected", -0.99f);      
    }
}