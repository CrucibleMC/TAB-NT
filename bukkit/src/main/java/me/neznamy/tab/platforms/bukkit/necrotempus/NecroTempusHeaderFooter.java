package me.neznamy.tab.platforms.bukkit.necrotempus;

import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import org.jetbrains.annotations.NotNull;

public class NecroTempusHeaderFooter extends HeaderFooter {

    @SuppressWarnings("deprecation")
    @Override
    public void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer) {
        player.getPlayer().setPlayerListHeaderFooter(
                player.getPlatform().toBukkitFormat(header),
                player.getPlatform().toBukkitFormat(footer)
        );
    }

}
