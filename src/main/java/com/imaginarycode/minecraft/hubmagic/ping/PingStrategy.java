package com.imaginarycode.minecraft.hubmagic.ping;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;

public interface PingStrategy {
    void ping(ServerInfo info, Callback<PingResult> callback);
}
