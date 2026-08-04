package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * Formerly handled horn reinforcement logic, armor healing, and HP bar display.
 * All logic has been moved to sikr_leviathan_system_defense.
 * This stub is kept so existing .weapon file references remain valid.
 */
public class sikr_defense_horn_plugin implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        // No-op — see sikr_leviathan_system_defense
    }
}
