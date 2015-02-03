package com.imaginarycode.minecraft.hubmagic;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class HubMagicPluginMessageListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals("HubMagic") && event.getReceiver() instanceof ProxiedPlayer &&
                event.getSender() instanceof ProxiedPlayer) {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

            String request = in.readUTF();

            if (request.equals("ConnectHub")) {
                ProxiedPlayer take = (ProxiedPlayer) event.getReceiver();
                take.connect(HubMagic.getPlugin().getServerSelector().chooseServer(take));
            }
        }
    }
}
