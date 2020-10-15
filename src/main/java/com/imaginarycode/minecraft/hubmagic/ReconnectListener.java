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

import com.google.common.collect.Iterables;
import com.imaginarycode.minecraft.hubmagic.selectors.ServerSelector;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.regex.Pattern;

public class ReconnectListener implements Listener {
    private final List<String> reasonList;
    private final List<String> serverList;
    private final ReconnectDetermination mode;
    private final List<String> message;
    private final ServerSelector serverSelector;
    private final boolean blacklist;

    public ReconnectListener(List<String> reasonList, List<String> serverList, ReconnectDetermination mode,
                             List<String> message, ServerSelector serverSelector, boolean blacklist) {
        this.reasonList = reasonList;
        this.serverList = serverList;
        this.mode = mode;
        this.message = message;
        this.serverSelector = serverSelector;
        this.blacklist = blacklist;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerKick(final ServerKickEvent event) {
        // When running in single-server mode, we can't kick people to the hub if they are on the hub.
        if (HubMagic.getPlugin().getServers().size() == 1 && Iterables.getOnlyElement(HubMagic.getPlugin().getServers()).equals(event.getKickedFrom())) {
            return;
        }

        if (event.getPlayer().getServer() == null) {
            // Have not connected before, so we can't do much.
            return;
        }

        if (!event.getPlayer().getServer().getInfo().equals(event.getKickedFrom())) {
            // We aren't even on that server, so ignore it.
            return;
        }

        boolean matched = false;

        switch (mode) {
            case REASONS:
                for (String pattern : reasonList) {
                    if (Pattern.compile(pattern).matcher(event.getKickReason()).find()) {
                        matched = true;
                        break;
                    }
                }
                break;
            case SERVERS:
                for (String pattern : serverList) {
                    if (Pattern.compile(pattern).matcher(event.getKickedFrom().getName()).find()) {
                        matched = true;
                        break;
                    }
                }
                break;
        }

        if (blacklist) {
            // If we matched, then we will not reconnect
            matched = !matched;
        }

        if (!matched) {
            return;
        }

        ServerInfo newServer = serverSelector.chooseServer(event.getPlayer());

        if (newServer == null) {
            // TODO: Force a disconnect?
            return;
        }

        event.setCancelled(true);
        event.setCancelServer(newServer);

        for (String components : message) {
            event.getPlayer().sendMessage(components.replace("%kick-reason%", event.getKickReason())
                    .replace("%server%", event.getKickedFrom().getName()));
        }
    }
}
