package me.neznamy.tab.platforms.bukkit.necrotempus;

public class NecroTempusAsk {

    public static boolean isAvailable() {
        try {
            NecroTempusAsk.class.getClassLoader().loadClass("io.github.cruciblemc.necrotempus.NecroTempus");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }


}
