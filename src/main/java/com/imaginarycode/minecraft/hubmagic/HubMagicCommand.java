/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
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
        if (strings.length < 1 || !strings[0].equalsIgnoreCase("reload")) {
            commandSender.sendMessage(new ComponentBuilder("/hubmagic <reload>").create());
            return;
        }

        HubMagic.getPlugin().reloadPlugin();

        commandSender.sendMessage(new ComponentBuilder("HubMagic has been reloaded.").color(ChatColor.YELLOW).create());
        commandSender.sendMessage(new ComponentBuilder("See http://goo.gl/NTxOuE for caveats.").color(ChatColor.YELLOW).create());
    }
}
