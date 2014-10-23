package com.imaginarycode.minecraft.hubmagic;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class HubMagicCommand extends Command {
    public HubMagicCommand() {
        super("hubmagic", "hubmagic.admin");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(new ComponentBuilder("/hubmagic <reload>").create());
            return;
        }

        HubMagic.getPlugin().reloadPlugin();

        commandSender.sendMessage(new ComponentBuilder("HubMagic has been reloaded.").color(ChatColor.YELLOW).create());
        commandSender.sendMessage(new ComponentBuilder("If you did not change anything other than servers and the type of selection, you should restart the proxy instead!")
                .color(ChatColor.YELLOW).create());
    }
}
