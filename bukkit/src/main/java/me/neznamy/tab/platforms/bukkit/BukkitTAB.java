package me.neznamy.tab.platforms.bukkit;

import me.neznamy.bossbar.bukkit.BukkitBossBarAPI;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.component.bukkit.BukkitComponentConverter;
import me.neznamy.component.shared.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for Bukkit.
 */
public class BukkitTAB extends JavaPlugin {

    @Override
    public void onEnable() {
        ComponentConverter.setInstance(BukkitComponentConverter.findInstance());
        BossBarAPI.setInstance(new BukkitBossBarAPI());
        boolean folia = ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
        TAB.create(folia ? new FoliaPlatform(this) : new BukkitPlatform(this));
    }

    @Override
    public void onDisable() {
        TAB.getInstance().unload();
    }
}