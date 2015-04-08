/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic.selectors;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SequentialSelector implements ServerSelector {
    private int index = 0;

    private synchronized int wrapIndex() {
        int oldIdx = index;
        if (index++ >= HubMagic.getPlugin().getServers().size()) {
            oldIdx = 0;
            index = 0;
        }
        return oldIdx;
    }

    @Override
    public ServerInfo chooseServer(ProxiedPlayer player) {
        ServerInfo info = null;
        int tries = 0;

        while (info == null || (!HubMagic.getPlugin().getPingManager().consideredAvailable(info, player) && tries < 5)) {
            info = HubMagic.getPlugin().getServers().get(wrapIndex());
            tries++;
        }

        return info;
    }
}
