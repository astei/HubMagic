package com.imaginarycode.minecraft.hubmagic.handlers;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Random;

public class RandomReconnectHandler extends NonPersistingReconnectHandler {
    private static final Random random = new Random();

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer player) {
        ServerInfo info = null;
        int tries = 0;
        while (info == null || (!HubMagic.getPlugin().getPingManager().consideredAvailable(info) && tries < 5)) {
            info = HubMagic.getPlugin().getServers().get(random.nextInt(HubMagic.getPlugin().getServers().size()));
            tries++;
        }
        return info;
    }
}
