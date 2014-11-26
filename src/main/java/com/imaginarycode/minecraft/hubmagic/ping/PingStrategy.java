/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic.ping;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;

public interface PingStrategy {
    void ping(ServerInfo info, Callback<PingResult> callback);
}
