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
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ReconnectListener implements Listener {
    private final List<String> reasonList;
    private final List<String> message;
    private final ServerSelector serverSelector;

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerKick(final ServerKickEvent event) {
        // When running in single-server mode, we can't kick people the hub if they are on the hub.
        if (HubMagic.getPlugin().getServers().size() == 1 && Iterables.getOnlyElement(HubMagic.getPlugin().getServers()).equals(event.getKickedFrom())) {
            return;
        }

        boolean shouldReconnect = false;

        for (String pattern : reasonList) {
            if (event.getKickReason().contains(pattern) || Pattern.compile(pattern).matcher(event.getKickReason()).find()) {
                shouldReconnect = true;
                break;
            }
        }

        if (!shouldReconnect) {
            HubMagic.getPlugin().getLogger().log(Level.INFO, "Reason '" + event.getKickReason() + "' did not match regex. Bailing out.");
            return;
        }

        ServerInfo newServer;
        int tries = 0;

        do {
            newServer = serverSelector.chooseServer(event.getPlayer());
            tries++;
        } while (tries < 4 && newServer == null);

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
