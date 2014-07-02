package com.imaginarycode.minecraft.hubmagic;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.regex.Pattern;

class HubCommand extends Command {
    private final HubCommandConfiguration configuration;

    HubCommand(String name, HubCommandConfiguration configuration) {
        super(name, configuration.isPermissionRequired() ? "hubmagic.hub" : null);
        this.configuration = configuration;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new ComponentBuilder("Non-players may not execute the hub connection commands. Perhaps you meant /hubmagic?")
                    .color(ChatColor.RED).create());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        for (Pattern pattern : configuration.getSkippingPatterns().get(getName())) {
            if (player.getServer().getInfo().getName().equals(pattern.pattern()) || pattern.matcher(player.getServer().getInfo().getName()).find()) {
                player.chat("/" + getName() + " " + Joiner.on(" ").join(strings));
                return;
            }
        }

        if (HubMagic.getPlugin().getServers().contains(player.getServer().getInfo())) {
            commandSender.sendMessage(new ComponentBuilder("You are already on a hub.")
                    .color(ChatColor.RED).create());
            return;
        }

        player.connect(HubMagic.getPlugin().getReconnectHandler().getServer(player));
    }
}