enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Guava, Kyori event, bStats, AuthLib, LuckPerms
        maven("https://repo.william278.net/releases/") // VelocityScoreboardAPI
        maven("https://repo.papermc.io/repository/maven-public/") // Velocity
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate, Bungeecord-proxy
        maven("https://repo.purpurmc.org/snapshots") // Purpur
        maven("https://repo.spongepowered.org/repository/maven-public/") // Sponge
        maven("https://jitpack.io") // PremiumVanish, Vault, YamlAssist, RedisBungee
        maven("https://repo.md-5.net/content/groups/public/") // LibsDisguises
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "TAB"

include(":api")
include(":shared")
include(":bossbar:bossbar-bukkit")
include(":bossbar:bossbar-bungee")
include(":bossbar:bossbar-fabric")
include(":bossbar:bossbar-shared")
include(":bossbar:bossbar-sponge7")
include(":bossbar:bossbar-velocity")
include(":bukkit")
include(":bungeecord")
include(":component:component-bukkit")
include(":component:component-bungee")
include(":component:component-fabric")
include(":component:component-shared")
include(":component:component-sponge7")
include(":fabric")
include(":fabric:v1_14_4")
include(":fabric:v1_18_2")
include(":fabric:v1_20_3")
include(":fabric:v1_21_3")
include(":sponge7")
include(":sponge8")
include(":velocity")
include(":jar")
