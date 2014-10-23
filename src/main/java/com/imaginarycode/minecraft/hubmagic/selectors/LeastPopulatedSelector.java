package com.imaginarycode.minecraft.hubmagic.selectors;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LeastPopulatedSelector implements ServerSelector {
    @Override
    public ServerInfo selectServer(ProxiedPlayer player) {
        return HubMagic.getPlugin().getPingManager().lowestPopulation(player);
    }
}
