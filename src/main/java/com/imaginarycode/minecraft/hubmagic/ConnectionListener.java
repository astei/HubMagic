/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ConnectionListener implements Listener {
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        String type = HubMagic.getPlugin().getConfiguration().getString("connection-handler");

        switch (type) {
            case "none":
                return;
            default:
                // Send all players that join to the hub
                if (event.getPlayer().getServer() != null)
                    return;

                event.setTarget(HubMagic.getPlugin().getServerSelector().chooseServer(event.getPlayer()));
                break;
        }
    }
}
