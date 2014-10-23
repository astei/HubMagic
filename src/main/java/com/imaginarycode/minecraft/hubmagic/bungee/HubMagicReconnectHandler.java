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
        HubMagic.getPlugin().getPingManager().shutdown();
    }

    @Override
    public final void close() {

    }
}
