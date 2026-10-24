package lobby.fluffylobby;

import lobby.fluffylobby.commands.BuildCommand;
import lobby.fluffylobby.commands.ClearChatCommand;
import lobby.fluffylobby.commands.FlyCommand;
import lobby.fluffylobby.commands.LobbyCommand;
import lobby.fluffylobby.commands.SetSpawnCommand;
import lobby.fluffylobby.commands.SpawnCommand;
import lobby.fluffylobby.commands.VanishCommand;
import lobby.fluffylobby.config.ConfigFixer;
import lobby.fluffylobby.effects.ParticleHaloManager;
import lobby.fluffylobby.gui.GuiClickListener;
import lobby.fluffylobby.gui.GuiConfigLoader;
import lobby.fluffylobby.gui.GuiManager;
import lobby.fluffylobby.inventory.LobbyItemManager;
import lobby.fluffylobby.listeners.*;
import lobby.fluffylobby.managers.SpawnManager;
import lobby.fluffylobby.placeholders.FluffyLobbyExpansion;
import lobby.fluffylobby.world.worldsettings;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FluffyLobby extends JavaPlugin implements Listener {

    private static FluffyLobby instance;
    private LobbyItemManager lobbyItemManager;
    private ParticleHaloManager particleHaloManager;
    private SpawnManager spawnManager;
    private boolean placeholderAPIEnabled;

    private GuiConfigLoader guiConfigLoader;
    private GuiManager guiManager;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Update the config for user with github
        new ConfigFixer(this).fix();


        lobbyItemManager = new LobbyItemManager(this);
        particleHaloManager = new ParticleHaloManager();
        spawnManager = new SpawnManager(this);

        // Some Velocity Stuff
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");


        // Register listeners
        getServer().getPluginManager().registerEvents(new EnderPearlListener(this), this);
        getServer().getPluginManager().registerEvents(new BuildListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this, particleHaloManager, spawnManager), this);
        getServer().getPluginManager().registerEvents(new CommandPermissionListener(this), this);
        getServer().getPluginManager().registerEvents(new worldsettings(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(this, spawnManager), this);
        ModrinthUpdateChecker updateChecker = new ModrinthUpdateChecker(this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        updateChecker.start();
        getServer().getPluginManager().registerEvents(new ChatDisableListener(this), this);
        getServer().getPluginManager().registerEvents(new TablistUpdater(this), this);



        // Register commands
        getCommand("fluffylobby").setExecutor(new LobbyCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this, spawnManager));
        getCommand("spawn").setExecutor(new SpawnCommand(this, spawnManager));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("clearchat").setExecutor(new ClearChatCommand(this));

        // Register GUI *New System*
        guiConfigLoader = new GuiConfigLoader(this);
        guiManager = new GuiManager(this);

        getServer().getPluginManager().registerEvents(new GuiClickListener(this), this);

        // Check if PlaceholderAPI is available and register placeholders if it is
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FluffyLobbyExpansion(this).register();
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI detected and enabled.");
        } else {
            placeholderAPIEnabled = false;
            getLogger().info("PlaceholderAPI not detected, continuing without it.");
        }

        displayStartupMessage();

    }

    @Override
    public void onDisable() {
    }

    public static FluffyLobby getInstance() {
        return instance;
    }

    public LobbyItemManager getLobbyItemManager() {
        return lobbyItemManager;
    }

    public ParticleHaloManager getParticleHaloManager() {
        return particleHaloManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public GuiConfigLoader getGuiConfigLoader() {
        return guiConfigLoader;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public Set<UUID> getVanishedPlayers() { return vanishedPlayers; }

    // Method to reload the configuration
    public void reloadConfigFile() {
        reloadConfig();
    }

    private void displayStartupMessage() {
        getLogger().info(YELLOW + "===============================================================" + RESET);
        getLogger().info(YELLOW + "   __       _     _           __           _                 " + RESET);
        getLogger().info(YELLOW + "  / /  ___ | |__ | |__  _   _/ _\\_   _ ___| |_ ___ _ __ ___  " + RESET);
        getLogger().info(YELLOW + " / /  / _ \\| '_ \\| '_ \\| | | \\ \\| | | / __| __/ _ \\ '_ ` _ \\ " + RESET);
        getLogger().info(YELLOW + "/ /__| (_) | |_) | |_) | |_| |\\ \\ |_| \\__ \\ ||  __/ | | | | |" + RESET);
        getLogger().info(YELLOW + "\\____/\\___/|_.__/|_.__/ \\__, \\__/\\__, |___/\\__\\___|_| |_| |_|" + RESET);
        getLogger().info(YELLOW + "                        |___/    |___/                        " + RESET);
        getLogger().info(YELLOW + "===============================================================" + RESET);
        getLogger().info(GREEN + "      FluffyLobby Plugin v1.7 Successfully Loaded! " + RESET);
        getLogger().info(CYAN + "      Created by: DieSuperKatze | Running on PaperMC   " + RESET);
        getLogger().info(YELLOW + "===============================================================" + RESET);
    }
}
