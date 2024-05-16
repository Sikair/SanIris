package data.missions.saniris_test;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		api.initFleet(FleetSide.PLAYER, "SIDN", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);
		
		api.setFleetTagline(FleetSide.PLAYER, "San-Iris Testing Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "The void");
		
		api.addToFleet(FleetSide.PLAYER, "sikr_himin_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_dufa_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_lehal_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_shion_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_bara_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_echin_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_nuyre_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_veko_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_marus_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_unnr_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_leviathan_off_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_leviathan_def_Hull", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sikr_manta_Hull", FleetMemberType.SHIP, true);
		
		float width = 20000f;
        float height = 20000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);
	}
}
