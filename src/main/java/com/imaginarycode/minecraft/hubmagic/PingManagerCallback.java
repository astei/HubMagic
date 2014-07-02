package com.imaginarycode.minecraft.hubmagic;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

@RequiredArgsConstructor
class PingManagerCallback implements Callback<ServerPing> {
    private final ServerInfo info;
    private final PingManager pingManager;

    // ProGuard doesn't preserve this function...
    @Override
    public void done(ServerPing ping, Throwable throwable) {
        pingManager.lock.writeLock().lock();
        try {
            if (throwable != null) {
                pingManager.pings.remove(info);
            } else {
                pingManager.pings.put(info, ping);
            }
        } finally {
            pingManager.lock.writeLock().unlock();
        }
    }
}