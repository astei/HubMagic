package com.imaginarycode.minecraft.hubmagic.handlers;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SequentialReconnectHandler extends NonPersistingReconnectHandler {
    private int index;

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer player) {
        ServerInfo info = null;
        int tries = 0;

        while (info == null || (!HubMagic.getPlugin().getPingManager().consideredAvailable(info) && tries < 5)) {
            info = HubMagic.getPlugin().getServers().get(wrapIndex());
            tries++;
        }

        return info;
    }

    private synchronized int wrapIndex() {
        int res = index;
        ++index;
        if (index > HubMagic.getPlugin().getServers().size()) {
            index = 0; // reset to normal
        }
        return res;
    }
}
