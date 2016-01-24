/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package com.imaginarycode.minecraft.hubmagic;

import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

class HubCommand extends Command {
    private final HubCommandConfiguration configuration;

    HubCommand(String name, HubCommandConfiguration configuration) {
        super(name, configuration.isPermissionRequired() ? "hubmagic.hub" : null);
        this.configuration = configuration;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new ComponentBuilder("Non-players may not execute the hub connection commands.")
                    .color(ChatColor.RED).create());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;

        for (String pattern : configuration.getSkippingPatterns().get(getName())) {
            if (player.getServer().getInfo().getName().equals(pattern) || player.getServer().getInfo().getName().matches(pattern)) {
                player.chat("/" + getName() + (strings.length > 0 ? " " + Joiner.on(" ").join(strings) : ""));
                return;
            }
        }

        if (HubMagic.getPlugin().getServers().contains(player.getServer().getInfo())) {
            commandSender.sendMessage(configuration.getMessages().get("already_connected"));
            return;
        }

        ServerInfo selected = HubMagic.getPlugin().getServerSelector().chooseServer(player);

        if (selected == null)
        {
            // We might not have a hub available.
            commandSender.sendMessage(configuration.getMessages().get("no_hubs_available"));
            return;
        }

        player.connect(HubMagic.getPlugin().getServerSelector().chooseServer(player));
    }
}
