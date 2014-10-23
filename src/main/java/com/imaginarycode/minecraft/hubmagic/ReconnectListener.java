package com.imaginarycode.minecraft.hubmagic;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

@RequiredArgsConstructor
public class ReconnectListener implements Listener {
    private final List<String> reasonList;
    private final List<BaseComponent[]> message;

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerKick(ServerKickEvent event) {
        ServerInfo kickedFrom = event.getKickedFrom();

        // May we reconnect the server to the hub?
        boolean mayReconnect;
        HubMagic.getPlugin().getPingManager().lock.readLock().lock();
        try {
            mayReconnect = HubMagic.getPlugin().getServers().contains(kickedFrom) ||
                    !HubMagic.getPlugin().getPingManager().pings.isEmpty();
        } finally {
            HubMagic.getPlugin().getPingManager().lock.readLock().unlock();
        }

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

            ServerInfo newServer = ProxyServer.getInstance().getReconnectHandler().getServer(event.getPlayer());

            if (newServer == null)
                return;

            event.setCancelled(true);
            event.setCancelServer(newServer);
            for (BaseComponent[] components : message) {
                event.getPlayer().sendMessage(components);
            }
        }
    }
}
