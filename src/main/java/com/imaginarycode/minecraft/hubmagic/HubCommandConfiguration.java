/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Getter
class HubCommandConfiguration {
    private final Multimap<String, String> skippingPatterns;
    private final boolean permissionRequired;

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
    }
}
