/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic.bungee;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import com.imaginarycode.minecraft.hubmagic.selectors.ServerSelector;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public class HubMagicReconnectHandler extends AbstractReconnectHandler {
    private final ServerSelector selector;

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer player) {
        return selector.selectServer(player);
    }

    @Override
    public final void setServer(ProxiedPlayer player) {

    }

    @Override
    public final void save() {

    }

    @Override
    public final void close() {
        HubMagic.getPlugin().getPingManager().shutdown();
    }
}
