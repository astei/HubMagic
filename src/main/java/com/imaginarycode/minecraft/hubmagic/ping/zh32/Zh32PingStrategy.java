package com.imaginarycode.minecraft.hubmagic.ping.zh32;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import com.imaginarycode.minecraft.hubmagic.ping.PingResult;
import com.imaginarycode.minecraft.hubmagic.ping.PingStrategy;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;

public class Zh32PingStrategy implements PingStrategy {
    @Override
    public void ping(final ServerInfo info, final Callback<PingResult> callback) {
        // zh32's library is synchronous, so we emulate asynchronous pings
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ServerListPing ping = new ServerListPing();
                ping.setHost(info.getAddress());
                try {
                    ServerListPing.StatusResponse reply = ping.fetchData();
                    callback.done(reply == null ? PingResult.DOWN : PingResult.from(false, reply.getPlayers().getOnline(),
                            reply.getPlayers().getMax()), null);
                } catch (IOException e) {
                    HubMagic.getPlugin().getLogger().warning("Unable to ping " + info.getName() + " (" + info.getAddress() + "): " + e.getMessage());
                    callback.done(PingResult.DOWN, e);
                }
            }
        };

        ProxyServer.getInstance().getScheduler().runAsync(HubMagic.getPlugin(), runnable);
    }
}
