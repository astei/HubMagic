/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.regex.Pattern;

class HubCommand extends Command {
    private final HubCommandConfiguration configuration;

    HubCommand(String name, HubCommandConfiguration configuration) {
        super(name, configuration.isPermissionRequired() ? "hubmagic.hub" : null);
        this.configuration = configuration;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new ComponentBuilder("Non-players may not execute the hub connection commands.")
                    .color(ChatColor.RED).create());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        for (String pattern : configuration.getSkippingPatterns().get(getName())) {
            if (player.getServer().getInfo().getName().equals(pattern) || player.getServer().getInfo().getName().matches(pattern)) {
                player.chat("/" + getName() + " " + Joiner.on(" ").join(strings));
                return;
            }
        }

        if (HubMagic.getPlugin().getServers().contains(player.getServer().getInfo())) {
            commandSender.sendMessage(configuration.getMessages().get("already_connected"));
            return;
        }

        ServerInfo selected = HubMagic.getPlugin().getServerSelector().selectServer(player);

        if (selected == null)
        {
            // We might not have a hub available.
            commandSender.sendMessage(configuration.getMessages().get("no_hubs_available"));
            return;
        }

        player.connect(HubMagic.getPlugin().getServerSelector().selectServer(player));
    }
}
