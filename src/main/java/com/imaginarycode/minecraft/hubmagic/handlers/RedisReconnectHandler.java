package com.imaginarycode.minecraft.hubmagic.handlers;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.AbstractReconnectHandler;
/*import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;*/

@RequiredArgsConstructor
public class RedisReconnectHandler /* extends AbstractReconnectHandler */ {
    /*
    private final JedisPool jedisPool;

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer player) {
        Jedis jedis = jedisPool.getResource();
        try {
            String result = jedis.hget("server:" + player.getUniqueId(), player.getPendingConnection().getListener().getHost().toString());
            return ProxyServer.getInstance().getServerInfo(result);
        } catch (JedisConnectionException e) {
            jedisPool.returnBrokenResource(jedis);
            return null;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void setServer(ProxiedPlayer player) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.hset("server:" + player.getUniqueId(), player.getPendingConnection().getListener().getHost().toString(), player.getServer().getInfo().getName());
        } catch (JedisConnectionException e) {
            jedisPool.returnBrokenResource(jedis);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void save() {
        // Nothing to save
    }

    @Override
    public void close() {
        jedisPool.destroy();
    }*/
}
