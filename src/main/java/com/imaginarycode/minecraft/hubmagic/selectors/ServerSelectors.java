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
package com.imaginarycode.minecraft.hubmagic.selectors;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ServerSelectors {
    private static final ServerSelector FIRST_AVAILABLE_SELECTOR = player -> HubMagic.getPlugin().getPingManager().firstAvailable(player);
    private static final ServerSelector LEAST_POPULATED_SELECTOR = player -> HubMagic.getPlugin().getPingManager().lowestPopulation(player);
    private static final ServerSelector RANDOM_SELECTOR = new ServerSelector() {
        private final Random random = new Random();

        @Override
        public ServerInfo chooseServer(ProxiedPlayer player) {
            List<ServerInfo> available = new ArrayList<>();

            for (ServerInfo info : HubMagic.getPlugin().getServers()) {
                if (HubMagic.getPlugin().getPingManager().consideredAvailable(info, player)) {
                    available.add(info);
                }
            }

            if (available.isEmpty()) {
                return null;
            }

            return available.get(random.nextInt(available.size()));
        }
    };

    public static final Supplier<ServerSelector> LEAST_POPULATED = () -> LEAST_POPULATED_SELECTOR;
    public static final Supplier<ServerSelector> FIRST_AVAILABLE = () -> FIRST_AVAILABLE_SELECTOR;
    public static final Supplier<ServerSelector> RANDOM = () -> RANDOM_SELECTOR;
    public static final Supplier<ServerSelector> SEQUENTIAL = SequentialSelector::new;

    public static ServerSelector parse(String type) {
        switch (type) {
            case "lowest":
                return LEAST_POPULATED.get();
            case "firstavailable":
                return FIRST_AVAILABLE.get();
            case "random":
                return RANDOM.get();
            case "sequential":
                return SEQUENTIAL.get();
            default:
                return null;
        }
    }

    private ServerSelectors() {
        throw new AssertionError();
    }
}
