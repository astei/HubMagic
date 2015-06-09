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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

import java.util.*;
import java.util.regex.Pattern;

@Getter
class HubCommandConfiguration {
    private final Multimap<String, String> skippingPatterns;
    private final boolean permissionRequired;
    private final Map<String, String> messages = new HashMap<String, String>() {
        {
            put("already_connected", ChatColor.RED + "You are already on a hub.");
            put("no_hubs_available", ChatColor.RED + "No hubs are currently available to connect to.");
        }
    };

    HubCommandConfiguration(Configuration configuration) {
        permissionRequired = configuration.getBoolean("hub-command.requires-permission", false);
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        Configuration configuration1 = configuration.getSection("hub-command.forwarding");

        List<String> global = configuration1.getStringList("global");

        for (String alias : configuration.getStringList("hub-command.aliases")) {
            for (String s : configuration1.getStringList(alias)) {
                builder.put(alias, s);
            }

            builder.putAll(alias, global);
        }

        skippingPatterns = builder.build();

        Object msgs = configuration.get("hub-command.messages");
        if (msgs != null && msgs instanceof Map)
        {
            Map<?, ?> msgMap = (Map) msgs;

            for (Map.Entry<?, ?> entry : msgMap.entrySet())
            {
                messages.put(entry.getKey().toString(), ChatColor.translateAlternateColorCodes('&', entry.getValue().toString()));
            }
        }
    }
}
