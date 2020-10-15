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
package com.imaginarycode.minecraft.hubmagic.ping.zh32;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import com.imaginarycode.minecraft.hubmagic.ping.PingResult;
import com.imaginarycode.minecraft.hubmagic.ping.PingStrategy;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;

public class Zh32PingStrategy implements PingStrategy {
    @Override
    public void ping(final ServerInfo info, final Callback<PingResult> callback) {
        // zh32's library is synchronous, so we emulate asynchronous pings
        Runnable runnable = () -> {
            ServerListPing ping = new ServerListPing();
            ping.setHost(info.getAddress());
            try {
                ServerListPing.StatusResponse reply = ping.fetchData();
                callback.done(reply == null ? PingResult.DOWN : PingResult.from(false, reply.getPlayers().getOnline(),
                        reply.getPlayers().getMax()), null);
            } catch (IOException e) {
                HubMagic.getPlugin().getLogger().warning("Unable to ping " + info.getName() + " (" + info.getAddress() + "): " + e.getMessage());
                callback.done(PingResult.DOWN, e);
            }
        };

        ProxyServer.getInstance().getScheduler().runAsync(HubMagic.getPlugin(), runnable);
    }
}
