/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import com.google.common.collect.ImmutableList;
import com.imaginarycode.minecraft.hubmagic.ping.PingStrategy;
import com.imaginarycode.minecraft.hubmagic.ping.bungee.BungeePingStrategy;
import com.imaginarycode.minecraft.hubmagic.ping.zh32.Zh32PingStrategy;
import com.imaginarycode.minecraft.hubmagic.selectors.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
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
    private ServerSelector serverSelector;
    @Getter
    private PingStrategy pingStrategy;
    @Getter
    private Configuration configuration;

    @Override
    public void onEnable() {
        plugin = this;

        reloadPlugin();

        if (configuration.getBoolean("kicks-lead-to-hub.enabled")) {
            String[] reason = ChatColor.translateAlternateColorCodes('&', configuration.getString("kicks-lead-to-hub.message")).split("\n");
            ServerSelector selector = ServerSelectors.parse(configuration.getString("kicks-lead-to-hub.selector", "sequential"));
            selector = selector == null ? ServerSelectors.SEQUENTIAL.get() : selector;
            getProxy().getPluginManager().registerListener(this, new ReconnectListener(configuration.getStringList("kicks-lead-to-hub.reasons"), ImmutableList.copyOf(reason), selector));
        }

        if (configuration.getBoolean("hub-command.enabled")) {
            HubCommandConfiguration configuration1 = new HubCommandConfiguration(configuration);

            for (String alias : configuration.getStringList("hub-command.aliases")) {
                getProxy().getPluginManager().registerCommand(this, new HubCommand(alias, configuration1));
            }
        }

        getProxy().getPluginManager().registerListener(this, new ConnectionListener());
        getProxy().getPluginManager().registerCommand(this, new HubMagicCommand());

        getProxy().registerChannel("HubMagic");
        getProxy().getPluginManager().registerListener(this, new HubMagicPluginMessageListener());
    }

    @Override
    public void onDisable() {
        if (pingManager != null)
            pingManager.shutdown();

        getProxy().getScheduler().cancel(this);
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
            else
                getLogger().info("Server " + server + " does not exist!");
        }

        if (servers.size() < 2) {
            if (servers.isEmpty()) {
                getLogger().severe("No servers were found in your configuration. Please specify one.");
                throw new RuntimeException("No servers were found in your configuration.");
            }
            getLogger().info("Less than 2 servers were found in your configuration. Hub balancing has been disabled.");
        }

        switch (configuration.getString("ping-strategy")) {
            case "bungee":
                pingStrategy = new BungeePingStrategy();
                break;
            case "fallback":
                pingStrategy = new Zh32PingStrategy();
                break;
            default:
                getLogger().info("Unrecognized ping strategy " + configuration.getString("ping-strategy") + ", using bungee.");
                pingStrategy = new BungeePingStrategy();
                break;
        }

        this.servers = ImmutableList.copyOf(servers);

        if (pingManager == null)
            pingManager = new PingManager();

        // Create our reconnect handler
        if (servers.size() > 1) {
            ServerSelector selector = ServerSelectors.parse(configuration.getString("type"));

            if (selector == null) {
                getLogger().info("Unrecognized selector " + configuration.getString("type") + ", using lowest.");
                serverSelector = ServerSelectors.LEAST_POPULATED;
            } else {
                serverSelector = selector;
            }
        } else {
            // Technically, since HubMagic is a "headless chicken" in single-hub mode, use the FIRST_AVAILABLE
            // selector as it is best suited to this case.
            serverSelector = ServerSelectors.FIRST_AVAILABLE;
        }

        switch (configuration.getString("connection-handler")) {
            case "none":
            case "reconnect":
                break;
            default:
                getLogger().info("Unrecognized connection handler " + configuration.getString("connection-handler") + ", using reconnect.");
                break;
        }
    }
}
