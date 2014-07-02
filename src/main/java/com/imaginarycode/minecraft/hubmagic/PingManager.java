package com.imaginarycode.minecraft.hubmagic;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PingManager {
    final Map<ServerInfo, ServerPing> pings = new HashMap<>();
    final ReadWriteLock lock = new ReentrantReadWriteLock();
    ScheduledTask task;

    PingManager() {
        task = HubMagic.getPlugin().getProxy().getScheduler().schedule(HubMagic.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (ServerInfo info : HubMagic.getPlugin().getServers()) {
                    try {
                        info.ping(new PingManagerCallback(info, PingManager.this));
                    } catch (RejectedExecutionException e) {
                        // Shut it up
                        shutdown();
                        return;
                    }
                }
            }
        }, 0, HubMagic.getPlugin().getConfiguration().getInt("ping-duration", 3), TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public ServerInfo firstAvailable(ProxiedPlayer player) {
        lock.readLock().lock();
        try {
            for (Map.Entry<ServerInfo, ServerPing> entry : pings.entrySet()) {
                if (entry.getValue().getPlayers().getOnline() >= entry.getValue().getPlayers().getMax())
                    continue;

                if (player.getServer() != null && player.getServer().getInfo().equals(entry.getKey()))
                    continue;

                return entry.getKey();
            }
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public ServerInfo lowestPopulation(ProxiedPlayer player) {
        lock.readLock().lock();
        try {
            Map.Entry<ServerInfo, ServerPing> lowest = null;

            for (Map.Entry<ServerInfo, ServerPing> entry : pings.entrySet()) {
                if (entry.getValue().getPlayers().getOnline() >= entry.getValue().getPlayers().getMax())
                    continue;

                if (player.getServer() != null && player.getServer().getInfo().equals(entry.getKey()))
                    continue;

                if (lowest == null || lowest.getValue().getPlayers().getOnline() > entry.getValue().getPlayers().getOnline()) {
                    lowest = entry;
                }
            }

            return lowest != null ? lowest.getKey() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean consideredOnline(ServerInfo serverInfo, ProxiedPlayer player) {
        lock.readLock().lock();
        try {
            if (!pings.containsKey(serverInfo))
                return false;

            ServerPing ping = pings.get(serverInfo);

            return !(ping.getPlayers().getOnline() >= ping.getPlayers().getMax() || (player != null && player.getServer().getInfo().equals(serverInfo)));
        } finally {
            lock.readLock().unlock();
        }
    }
}
