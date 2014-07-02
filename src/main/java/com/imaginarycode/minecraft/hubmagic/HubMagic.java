package com.imaginarycode.minecraft.hubmagic;

import com.google.common.collect.ImmutableList;
import com.imaginarycode.minecraft.hubmagic.handlers.FirstAvailableReconnectHandler;
import com.imaginarycode.minecraft.hubmagic.handlers.LeastPopulatedReconnectHandler;
import com.imaginarycode.minecraft.hubmagic.handlers.RandomReconnectHandler;
import com.imaginarycode.minecraft.hubmagic.handlers.SequentialReconnectHandler;
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
import java.util.regex.Pattern;

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

        try {
            getDataFolder().mkdir();
            configuration = createOrLoadConfig();
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
            pingManager = new PingManager();
        } catch (IOException e) {
            throw new RuntimeException("Could not load config", e);
        }

        // Create our reconnect handler

        switch (configuration.getString("type")) {
            case "lowest":
                reconnectHandler = new LeastPopulatedReconnectHandler();
                break;
            case "firstavailable":
                reconnectHandler = new FirstAvailableReconnectHandler();
                break;
            case "random":
                reconnectHandler = new RandomReconnectHandler();
                break;
            case "sequential":
                reconnectHandler = new SequentialReconnectHandler();
                break;
        }

        if (reconnectHandler != null) {
            if (getProxy().getReconnectHandler() != null) {
                getLogger().info("Another reconnect handler is already installed. HubMagic will replace it.");
            }
            getProxy().setReconnectHandler(reconnectHandler);
        } else {
            getLogger().info("No valid reconnect handler was specified.");
            return;
        }

        if (configuration.getBoolean("kicks-lead-to-hub.enabled")) {
            List<Pattern> patterns = new ArrayList<>();

            for (String pat : configuration.getStringList("kicks-lead-to-hub.reasons")) {
                patterns.add(Pattern.compile(pat));
            }

            String[] reason = ChatColor.translateAlternateColorCodes('&', configuration.getString("kicks-lead-to-hub.message")).split("\n");

            List<BaseComponent[]> message = new ArrayList<>();

            for (String s : reason) {
                message.add(TextComponent.fromLegacyText(s));
            }

            getProxy().getPluginManager().registerListener(this, new ReconnectListener(ImmutableList.copyOf(patterns), ImmutableList.copyOf(message)));
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

    private Configuration createOrLoadConfig() throws IOException {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            Files.copy(getResourceAsStream("config.yml"), file.toPath());
        }
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }
}
