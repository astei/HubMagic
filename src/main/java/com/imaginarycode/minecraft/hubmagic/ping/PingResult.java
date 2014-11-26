/**
 * Copyright © 2014 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.hubmagic.ping;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "from")
@Getter
public class PingResult {
    /**
     * Static singleton for servers that are down.
     */
    public static final PingResult DOWN = new PingResult(true);

    private final boolean down;
    private final int playerCount;
    private final int playerMax;

    private PingResult(boolean down) {
        this.down = down;
        playerCount = 0;
        playerMax = 0;
    }
}
