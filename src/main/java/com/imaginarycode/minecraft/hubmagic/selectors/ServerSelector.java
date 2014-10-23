package com.imaginarycode.minecraft.hubmagic.selectors;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface ServerSelector {
    ServerInfo selectServer(ProxiedPlayer player);
}
