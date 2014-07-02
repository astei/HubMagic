package com.imaginarycode.minecraft.hubmagic.handlers;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LeastPopulatedReconnectHandler extends NonPersistingReconnectHandler {
    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer player) {
        return HubMagic.getPlugin().getPingManager().lowestPopulation(player);
    }
}
