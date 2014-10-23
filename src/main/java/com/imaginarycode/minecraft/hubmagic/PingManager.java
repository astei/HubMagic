package com.imaginarycode.minecraft.hubmagic;

import com.imaginarycode.minecraft.hubmagic.util.ServerListPing;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PingManager {
    final Map<ServerInfo, ServerListPing.StatusResponse> pings = new HashMap<>();
    final ReadWriteLock lock = new ReentrantReadWriteLock();
    ScheduledTask task;

    PingManager() {
        task = HubMagic.getPlugin().getProxy().getScheduler().schedule(HubMagic.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (final ServerInfo info : HubMagic.getPlugin().getServers()) {
                    HubMagic.getPlugin().getProxy().getScheduler().runAsync(HubMagic.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            ServerListPing ping = new ServerListPing();
                            ping.setHost(info.getAddress());
                            try {
                                ServerListPing.StatusResponse reply = ping.fetchData();
                                lock.writeLock().lock();
                                try {
                                    pings.put(info, reply);
                                } finally {
                                    lock.writeLock().unlock();
                                }
                            } catch (IOException e) {
                                HubMagic.getPlugin().getLogger().warning("Unable to ping " + info.getName() + " (" + info.getAddress() + "): " + e.getMessage());
                                lock.writeLock().lock();
                                try {
                                    pings.remove(info);
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
            for (Map.Entry<ServerInfo, ServerListPing.StatusResponse> entry : pings.entrySet()) {
                if (entry.getValue() == null)
                    continue;

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
            Map.Entry<ServerInfo, ServerListPing.StatusResponse> lowest = null;

            for (Map.Entry<ServerInfo, ServerListPing.StatusResponse> entry : pings.entrySet()) {
                if (entry.getValue() == null)
                    continue;

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

    public boolean consideredAvailable(ServerInfo serverInfo) {
        lock.readLock().lock();
        try {
            if (!pings.containsKey(serverInfo))
                return false;

            ServerListPing.StatusResponse ping = pings.get(serverInfo);

            return ping != null && ping.getPlayers().getOnline() <= ping.getPlayers().getMax();
        } finally {
            lock.readLock().unlock();
        }
    }
}
