/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionListener implements Listener {
    private final Set<UUID> haveConnected = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>(64, 0.65f, 2));

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        String type = HubMagic.getPlugin().getConfiguration().getString("connection-handler");

        switch (type) {
            case "none":
                return;
            //case "prefer-reconnect-handlers":
            //    if (HubMagic.getPlugin().getServers().contains(event.getTarget()))
            //        event.setTarget(HubMagic.getPlugin().getServerSelector().selectServer(event.getPlayer()));
            //    break;
            default:
                // Send all players that join to the hub
                if (haveConnected.contains(event.getPlayer().getUniqueId()))
                    return;

                event.setTarget(HubMagic.getPlugin().getServerSelector().selectServer(event.getPlayer()));
                haveConnected.add(event.getPlayer().getUniqueId());
                break;
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        haveConnected.remove(event.getPlayer().getUniqueId());
    }
}
