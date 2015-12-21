/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package com.imaginarycode.minecraft.hubmagic;

import com.google.common.collect.ImmutableList;
import com.imaginarycode.minecraft.hubmagic.ping.PingStrategy;
import com.imaginarycode.minecraft.hubmagic.ping.bungee.BungeePingStrategy;
import com.imaginarycode.minecraft.hubmagic.ping.zh32.Zh32PingStrategy;
import com.imaginarycode.minecraft.hubmagic.selectors.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            getProxy().getPluginManager().registerListener(this,
                    new ReconnectListener(configuration.getStringList("kicks-lead-to-hub.reasons"),
                            configuration.getStringList("kicks-lead-to-hub.servers"),
                            ReconnectDetermination.valueOf(configuration.getString("kicks-lead-to-hub.mode", "reasons").toUpperCase()),
                            ImmutableList.copyOf(reason), selector));
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

        // Shut Netty up when we disable ourselves.
        Logger.getLogger("io.netty.util.concurrent.DefaultPromise.rejectedExecution").setLevel(Level.OFF);
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
                getLogger().info("Unrecognized ping strategy " + configuration.getString("ping-strategy") + ", using fallback.");
                pingStrategy = new Zh32PingStrategy();
                break;
        }

        this.servers = new CopyOnWriteArrayList<>(servers);

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
