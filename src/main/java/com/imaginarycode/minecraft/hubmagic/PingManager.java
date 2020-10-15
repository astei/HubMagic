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

import com.imaginarycode.minecraft.hubmagic.ping.PingResult;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PingManager {
    private final Map<ServerInfo, PingResult> pings = new ConcurrentHashMap<>();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private ScheduledTask task;

    PingManager() {
        task = HubMagic.getPlugin().getProxy().getScheduler().schedule(HubMagic.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (shutdown.get())
                    return;

                for (final ServerInfo info : HubMagic.getPlugin().getServers()) {
                    HubMagic.getPlugin().getPingStrategy().ping(info, new Callback<PingResult>() {
                        @Override
                        public void done(PingResult pingResult, Throwable throwable) {
                            // NB: throwable can be null and we have a DOWN pingresult
                            // so always use the pingresult
                            if (pingResult.isDown()) {
                                pings.remove(info);
                            } else {
                                pings.put(info, pingResult);
                            }
                        }
                    });
                }
            }
        }, 0, HubMagic.getPlugin().getConfiguration().getInt("ping-duration", 3), TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (task != null) {
            shutdown.set(true);
            task.cancel();
            task = null;
        }
    }

    public ServerInfo firstAvailable(ProxiedPlayer player) {
        if (player == null) {
            throw new NullPointerException("player");
        }

        for (Map.Entry<ServerInfo, PingResult> entry : pings.entrySet()) {
            if (entry.getValue() == null)
                continue;

            if (entry.getValue().getPlayerCount() >= entry.getValue().getPlayerMax())
                continue;

            if (!HubMagic.getPlugin().checkClientVersion(entry.getKey(), player))
                continue;

            if (player.getServer() != null && player.getServer().getInfo().equals(entry.getKey()))
                continue;

            return entry.getKey();
        }
        return null;
    }

    public ServerInfo lowestPopulation(ProxiedPlayer player) {
        if (player == null) {
            throw new NullPointerException("player");
        }

        Map.Entry<ServerInfo, PingResult> lowest = null;

        for (Map.Entry<ServerInfo, PingResult> entry : pings.entrySet()) {
            if (entry.getValue() == null)
                continue;

            if (entry.getValue().getPlayerCount() >= entry.getValue().getPlayerMax())
                continue;

            if (!HubMagic.getPlugin().checkClientVersion(entry.getKey(), player))
                continue;

            if (player.getServer() != null && player.getServer().getInfo().equals(entry.getKey()))
                continue;

            if (lowest == null || lowest.getValue().getPlayerCount() > entry.getValue().getPlayerCount()) {
                lowest = entry;
            }
        }

        return lowest != null ? lowest.getKey() : null;
    }

    public boolean consideredAvailable(ServerInfo serverInfo, ProxiedPlayer player) {
        if (serverInfo == null) {
            throw new NullPointerException("serverInfo");
        }

        if (player == null) {
            throw new NullPointerException("player");
        }

        PingResult ping = pings.get(serverInfo);
        return ping != null && !ping.isDown() && ping.getPlayerCount() < ping.getPlayerMax() &&
                (player.getServer() == null || !player.getServer().getInfo().equals(serverInfo)) && HubMagic.getPlugin().checkClientVersion(serverInfo, player);
    }
}
