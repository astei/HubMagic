/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic.ping.bungee;

import com.imaginarycode.minecraft.hubmagic.ping.PingResult;
import com.imaginarycode.minecraft.hubmagic.ping.PingStrategy;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeePingStrategy implements PingStrategy {
    @Override
    public void ping(ServerInfo info, final Callback<PingResult> callback) {
        info.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing serverPing, Throwable throwable) {
                if (throwable != null) {
                    callback.done(PingResult.DOWN, throwable);
                    return;
                }

                callback.done(PingResult.from(false, serverPing.getPlayers().getOnline(), serverPing.getPlayers().getMax()), null);
            }
        });
    }
}
