package me.neznamy.tab.platforms.bukkit.platform;

import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.component.shared.component.SimpleTextComponent;
import me.neznamy.component.shared.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.*;
import me.neznamy.tab.platforms.bukkit.features.BukkitTabExpansion;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.hook.BukkitPremiumVanishHook;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.PerformanceUtil;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Implementation of Platform interface for Bukkit platform
 */
@Getter
public class BukkitPlatform implements BackendPlatform {

    /** Plugin instance for registering tasks and events */
    @NotNull
    private final JavaPlugin plugin;

    /** Server version */
    @Getter
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);

    /** Variables checking presence of other plugins to hook into */
    private final boolean placeholderAPI = ReflectionUtils.classExists("me.clip.placeholderapi.PlaceholderAPI");

    /** Spigot field for tracking TPS, the array is final and only being modified instead of re-instantiated */
    private double[] recentTps;

    /** Detection for presence of Paper's TPS getter */
    private final boolean paperTps = ReflectionUtils.methodExists(Bukkit.class, "getTPS");

    /** Detection for presence of Paper's MSPT getter */
    private final boolean paperMspt = ReflectionUtils.methodExists(Bukkit.class, "getAverageTickTime");

    /**
     * Constructs new instance with given plugin.
     *
     * @param   plugin
     *          Plugin
     */
    public BukkitPlatform(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        long time = System.currentTimeMillis();
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            recentTps = ((double[]) server.getClass().getField("recentTps").get(server));
        } catch (ReflectiveOperationException ignored) {
            //not spigot
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            new BukkitPremiumVanishHook().register();
        }
        PingRetriever.tryLoad();
        ScoreboardLoader.findInstance();
        TabListBase.findInstance();
        if (BukkitReflection.getMinorVersion() >= 8) {
            HeaderFooter.findInstance();
            BukkitPipelineInjector.tryLoad();
        }
        BukkitUtils.sendCompatibilityMessage();
        Bukkit.getConsoleSender().sendMessage("[TAB] §7Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms");
    }

    @Override
    public void loadPlayers() {
        for (Player p : BukkitUtils.getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer(this, p));
        }
    }

    @Override
    public void registerPlaceholders() {
        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        manager.registerServerPlaceholder("%vault-prefix%", -1, () -> "");
        manager.registerServerPlaceholder("%vault-suffix%", -1, () -> "");
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) {
                Chat chat = rspChat.getProvider();
                int refresh = TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval();
                manager.registerPlayerPlaceholder("%vault-prefix%", refresh, p -> chat.getPlayerPrefix((Player) p.getPlayer()));
                manager.registerPlayerPlaceholder("%vault-suffix%", refresh, p -> chat.getPlayerSuffix((Player) p.getPlayer()));
            }
        }
        // Override for the PAPI placeholder to prevent console errors on unsupported server versions when ping field changes
        manager.registerPlayerPlaceholder("%player_ping%", manager.getRefreshInterval("%player_ping%"),
                p -> PerformanceUtil.toString(((TabPlayer) p).getPing()));
        BackendPlatform.super.registerPlaceholders();
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return BukkitReflection.getMinorVersion() >= 8 && BukkitPipelineInjector.isAvailable() ? new BukkitPipelineInjector() : null;
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        if (placeholderAPI) {
            BukkitTabExpansion expansion = new BukkitTabExpansion();
            expansion.register();
            return expansion;
        }
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return new PerWorldPlayerList(plugin, configuration);
    }

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        if (!placeholderAPI) {
            registerDummyPlaceholder(identifier);
            return;
        }
        PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
        int refresh = pl.getRefreshInterval(identifier);
        if (identifier.startsWith("%rel_")) {
            //relational placeholder
            TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(identifier, pl.getRefreshInterval(identifier), (viewer, target) ->
                    PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier));
        } else if (identifier.startsWith("%sync:")) {
            registerSyncPlaceholder(identifier, refresh);
        } else if (identifier.startsWith("%server_")) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, refresh,
                    () -> PlaceholderAPI.setPlaceholders(null, identifier));
        } else {
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh,
                    p -> PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), identifier));
        }
    }

    /**
     * Registers a sync placeholder with given identifier and refresh.
     *
     * @param   identifier
     *          Placeholder identifier
     * @param   refresh
     *          Placeholder refresh
     */
    public void registerSyncPlaceholder(@NotNull String identifier, int refresh) {
        String syncedPlaceholder = "%" + identifier.substring(6);
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh, p -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                long time = System.nanoTime();
                ppl[0].updateValue(p, placeholderAPI ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier);
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, System.nanoTime() - time);
            });
            return null;
        });
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        Bukkit.getConsoleSender().sendMessage("[TAB] " + message.toBukkitFormat(serverVersion.supportsRGB()));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        Bukkit.getConsoleSender().sendMessage("§c[TAB] [WARN] " + message.toBukkitFormat(serverVersion.supportsRGB()));
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Bukkit] " + Bukkit.getName() + " - " + Bukkit.getBukkitVersion().split("-")[0];
    }

    @Override
    public void registerListener() {
        Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), plugin);
    }

    @Override
    public void registerCommand() {
        PluginCommand command = Bukkit.getPluginCommand(getCommand());
        if (command != null) {
            BukkitTabCommand cmd = new BukkitTabCommand();
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        } else {
            logWarn(new SimpleTextComponent("Failed to register command, is it defined in plugin.yml?"));
        }
    }

    @Override
    public void startMetrics() {
        Metrics metrics = new Metrics(plugin, TabConstants.BSTATS_PLUGIN_ID_BUKKIT);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.PERMISSION_SYSTEM,
                () -> TAB.getInstance().getGroupManager().getPermissionPlugin()));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION,
                () -> "1." + BukkitReflection.getMinorVersion() + ".x"));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    @NotNull
    @SneakyThrows
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return ScoreboardLoader.getInstance().apply((BukkitTabPlayer) player);
    }

    @Override
    @NotNull
    @SneakyThrows
    public TabList createTabList(@NotNull TabPlayer player) {
        return TabListBase.getInstance().apply((BukkitTabPlayer) player);
    }

    @Override
    public boolean supportsNumberFormat() {
        return serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId();
    }

    @Override
    public boolean supportsListOrder() {
        return serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId();
    }

    @Override
    public boolean supportsScoreboards() {
        return true;
    }

    @Override
    public boolean isSafeFromPacketEventsBug() {
        return serverVersion.getMinorVersion() >= 13;
    }

    @Override
    @NotNull
    public GroupManager detectPermissionPlugin() {
        if (LuckPermsHook.getInstance().isInstalled()) {
            return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
            if (provider != null && !provider.getProvider().getName().equals("SuperPerms")) {
                return new GroupManager(provider.getProvider().getName(), p -> provider.getProvider().getPrimaryGroup((Player) p.getPlayer()));
            }
        }
        return new GroupManager("None", p -> TabConstants.NO_GROUP);
    }

    @Override
    public double getTPS() {
        if (recentTps != null) {
            return recentTps[0];
        } else if (paperTps) {
            return Bukkit.getTPS()[0];
        } else {
            return -1;
        }
    }

    @Override
    public double getMSPT() {
        if (paperMspt) return Bukkit.getAverageTickTime();
        return -1;
    }

    /**
     * Runs task in the main thread for given entity.
     *
     * @param   entity
     *          Entity's main thread
     * @param   task
     *          Task to run
     */
    public void runSync(@NotNull Entity entity, @NotNull Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}