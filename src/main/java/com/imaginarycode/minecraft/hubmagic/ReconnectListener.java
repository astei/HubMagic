/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import com.imaginarycode.minecraft.hubmagic.selectors.ServerSelector;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ReconnectListener implements Listener {
    private final List<String> reasonList;
    private final List<String> message;
    private final ServerSelector serverSelector;

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerKick(final ServerKickEvent event) {
        // When running in single-server mode, we can't kick people to hubs.
        if (HubMagic.getPlugin().getServers().size() < 2 && HubMagic.getPlugin().getServers().get(0).equals(event.getKickedFrom()))
            return;

        boolean shouldReconnect = false;

        for (String pattern : reasonList) {
            if (event.getKickReason().contains(pattern) || Pattern.compile(pattern).matcher(event.getKickReason()).find()) {
                shouldReconnect = true;
                break;
            }
        }

        if (!shouldReconnect)
            return;

        ServerInfo newServer;
        int tries = 0;

        do {
            newServer = serverSelector.chooseServer(event.getPlayer());
            tries++;
        } while (tries < 4 && (newServer == null || newServer.equals(event.getKickedFrom())));

        event.setCancelled(true);
        event.setCancelServer(newServer);

        for (String components : message) {
            event.getPlayer().sendMessage(components.replace("%kick-reason%", event.getKickReason())
                    .replace("%server%", event.getKickedFrom().getName()));
        }
    }
}
