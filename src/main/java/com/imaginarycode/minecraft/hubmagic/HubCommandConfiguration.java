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
    private final Multimap<String, Pattern> skippingPatterns;
    private final boolean permissionRequired;

    HubCommandConfiguration(Configuration configuration) {
        permissionRequired = configuration.getBoolean("hub-command.requires-permission", false);
        ImmutableMultimap.Builder<String, Pattern> builder = ImmutableMultimap.builder();
        Configuration configuration1 = configuration.getSection("hub-command.forwarding");

        List<Pattern> global;

        if (configuration1.getKeys().contains("global")) {
            global = new ArrayList<>();
            for (String s : configuration1.getStringList("global")) {
                global.add(Pattern.compile(s));
            }
        } else {
            global = Collections.emptyList();
        }

        for (String alias : configuration.getStringList("hub-command.aliases")) {
            for (String s : configuration1.getStringList(alias)) {
                builder.put(alias, Pattern.compile(s));
            }

            builder.putAll(alias, global);
        }

        skippingPatterns = builder.build();
    }
}
