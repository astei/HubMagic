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

@RequiredArgsConstructor
public class ReconnectListener implements Listener {
    private final List<String> reasonList;
    private final List<String> message;
    private final ServerSelector serverSelector;

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerKick(final ServerKickEvent event) {
        // May we reconnect the server to the hub?
        boolean mayReconnect = HubMagic.getPlugin().getPingManager().firstAvailable(event.getPlayer()) != null;

        if (mayReconnect) {
            boolean shouldReconnect = false;

            for (String pattern : reasonList) {
                if (event.getKickReason().contains(pattern) || event.getKickReason().matches(pattern)) {
                    shouldReconnect = true;
                    break;
                }
            }

            if (!shouldReconnect)
                return;

            ServerInfo newServer = serverSelector.chooseServer(event.getPlayer());

            if (newServer == null)
                return;

            event.setCancelled(true);
            event.setCancelServer(newServer);

            for (String components : message) {
                event.getPlayer().sendMessage(components.replace("%kick-reason%", event.getKickReason())
                        .replace("%server%", event.getKickedFrom().getName()));
            }
        }
    }
}
