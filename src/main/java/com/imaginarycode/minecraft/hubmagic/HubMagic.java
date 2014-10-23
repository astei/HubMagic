package com.imaginarycode.minecraft.hubmagic;

import com.google.common.collect.ImmutableList;
import com.imaginarycode.minecraft.hubmagic.bungee.HubMagicReconnectHandler;
import com.imaginarycode.minecraft.hubmagic.selectors.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HubMagic extends Plugin {
    @Getter
    private static HubMagic plugin;
    @Getter
    private List<ServerInfo> servers;
    @Getter
    private PingManager pingManager;
    @Getter
    private ReconnectHandler reconnectHandler;
    @Getter
    private Configuration configuration;

    @Override
    public void onEnable() {
        plugin = this;

        reloadPlugin();

        if (configuration.getBoolean("kicks-lead-to-hub.enabled")) {
            String[] reason = ChatColor.translateAlternateColorCodes('&', configuration.getString("kicks-lead-to-hub.message")).split("\n");

            List<BaseComponent[]> message = new ArrayList<>();

            for (String s : reason) {
                message.add(TextComponent.fromLegacyText(s));
            }

            getProxy().getPluginManager().registerListener(this, new ReconnectListener(configuration.getStringList("kicks-lead-to-hub.reasons"), ImmutableList.copyOf(message)));
        }

        if (configuration.getBoolean("hub-command.enabled")) {
            HubCommandConfiguration configuration1 = new HubCommandConfiguration(configuration);

            for (String alias : configuration.getStringList("hub-command.aliases")) {
                getProxy().getPluginManager().registerCommand(this, new HubCommand(alias, configuration1));
            }
        }
    }

    @Override
    public void onDisable() {
        if (pingManager != null)
            pingManager.shutdown();
    }

    void reloadPlugin() {
        try {
            getDataFolder().mkdir();
            configuration = createOrLoadConfig();
        } catch (IOException e) {
            throw new RuntimeException("Could not load config", e);
        }

        configurePlugin();
    }

    private Configuration createOrLoadConfig() throws IOException {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            Files.copy(getResourceAsStream("config.yml"), file.toPath());
        }
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    private void configurePlugin() {
        List<ServerInfo> servers = new ArrayList<>();

        for (String server : configuration.getStringList("servers")) {
            ServerInfo info = getProxy().getServerInfo(server);

            if (info != null)
                servers.add(info);
        }

        if (servers.size() < 2) {
            getLogger().info("Less than 2 servers were found. Please check your configuration.");
            throw new RuntimeException("Insufficient number of servers found in your configuration");
        }

        this.servers = ImmutableList.copyOf(servers);

        if (pingManager == null)
            pingManager = new PingManager();

        // Create our reconnect handler
        ServerSelector selector;

        switch (configuration.getString("type")) {
            case "lowest":
                selector = new LeastPopulatedSelector();
                break;
            case "firstavailable":
                selector = new FirstAvailableSelector();
                break;
            case "random":
                selector = new RandomReconnectSelector();
                break;
            case "sequential":
                selector = new SequentialSelector();
                break;
            default:
                getLogger().info("Unrecognized selector " + configuration.getString("type") + ", using lowest.");
                selector = new LeastPopulatedSelector();
                break;
        }

        reconnectHandler = new HubMagicReconnectHandler(selector);

        if (getProxy().getReconnectHandler() != null && !(getProxy().getReconnectHandler() instanceof HubMagicReconnectHandler)) {
            getLogger().info("Another reconnect handler is already installed. HubMagic will replace it.");
        }

        getProxy().setReconnectHandler(reconnectHandler);
    }
}
