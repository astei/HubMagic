/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import com.imaginarycode.minecraft.hubmagic.ping.PingResult;
import com.imaginarycode.minecraft.hubmagic.ping.zh32.ServerListPing;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PingManager {
    final Map<ServerInfo, PingResult> pings = new HashMap<>();
    final ReadWriteLock lock = new ReentrantReadWriteLock();
    ScheduledTask task;

    PingManager() {
        task = HubMagic.getPlugin().getProxy().getScheduler().schedule(HubMagic.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (final ServerInfo info : HubMagic.getPlugin().getServers()) {
                    HubMagic.getPlugin().getPingStrategy().ping(info, new Callback<PingResult>() {
                        @Override
                        public void done(PingResult pingResult, Throwable throwable) {
                            // NB: throwable can be null and we have a DOWN pingresult
                            // so always use the pingresult
                            if (pingResult.isDown()) {
                                lock.writeLock().lock();
                                try {
                                    pings.remove(info);
                                } finally {
                                    lock.writeLock().unlock();
                                }
                            } else {
                                lock.writeLock().lock();
                                try {
                                    pings.put(info, pingResult);
                                } finally {
                                    lock.writeLock().unlock();
                                }
                            }
                        }
                    });
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
            for (Map.Entry<ServerInfo, PingResult> entry : pings.entrySet()) {
                if (entry.getValue() == null)
                    continue;

                if (entry.getValue().getPlayerCount() >= entry.getValue().getPlayerMax())
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
            Map.Entry<ServerInfo, PingResult> lowest = null;

            for (Map.Entry<ServerInfo, PingResult> entry : pings.entrySet()) {
                if (entry.getValue() == null)
                    continue;

                if (entry.getValue().getPlayerCount() >= entry.getValue().getPlayerMax())
                    continue;

                if (player.getServer() != null && player.getServer().getInfo().equals(entry.getKey()))
                    continue;

                if (lowest == null || lowest.getValue().getPlayerCount() > entry.getValue().getPlayerCount()) {
                    lowest = entry;
                }
            }

            return lowest != null ? lowest.getKey() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean consideredAvailable(ServerInfo serverInfo) {
        lock.readLock().lock();
        try {
            if (!pings.containsKey(serverInfo))
                return false;

            PingResult ping = pings.get(serverInfo);

            return ping != null && !ping.isDown() && ping.getPlayerCount() <= ping.getPlayerMax();
        } finally {
            lock.readLock().unlock();
        }
    }
}
