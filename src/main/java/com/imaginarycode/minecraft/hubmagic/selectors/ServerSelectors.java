package com.imaginarycode.minecraft.hubmagic.selectors;

import com.imaginarycode.minecraft.hubmagic.HubMagic;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Random;

public enum ServerSelectors implements ServerSelector {
    FIRST_AVAILABLE {
        @Override
        public ServerInfo chooseServer(ProxiedPlayer player) {
            return HubMagic.getPlugin().getPingManager().firstAvailable(player);
        }
    },
    LEAST_POPULATED {
        @Override
        public ServerInfo chooseServer(ProxiedPlayer player) {
            return HubMagic.getPlugin().getPingManager().lowestPopulation(player);
        }
    },
    RANDOM {
        private final Random random = new Random();

        @Override
        public ServerInfo chooseServer(ProxiedPlayer player) {
            ServerInfo info = null;
            int tries = 0;
            while (info == null || (!HubMagic.getPlugin().getPingManager().consideredAvailable(info, player) && tries < 5)) {
                info = HubMagic.getPlugin().getServers().get(random.nextInt(HubMagic.getPlugin().getServers().size()));
                tries++;
            }
            return info;
        }
    },
    SEQUENTIAL {
        @Override
        public ServerInfo chooseServer(ProxiedPlayer player) {
            throw new RuntimeException("Please use get() to get a proper ServerSelector.");
        }

        @Override
        public ServerSelector get() {
            return new SequentialSelector();
        }
    };

    public ServerSelector get() {
        return this;
    }

    public static ServerSelector parse(String type) {
        switch (type) {
            case "lowest":
                return LEAST_POPULATED;
            case "firstavailable":
                return FIRST_AVAILABLE;
            case "random":
                return RANDOM;
            case "sequential":
                return SEQUENTIAL.get();
            default:
                return null;
        }
    }
}
