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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class HubMagicCommand extends Command {
    public HubMagicCommand() {
        super("hubmagic", "hubmagic.admin");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 1 || !strings[0].equalsIgnoreCase("reload")) {
            commandSender.sendMessage(new ComponentBuilder("/hubmagic <reload>").create());
            return;
        }

        HubMagic.getPlugin().reloadPlugin();

        commandSender.sendMessage(new ComponentBuilder("HubMagic has been reloaded.").color(ChatColor.YELLOW).create());
        commandSender.sendMessage(new ComponentBuilder("See http://goo.gl/NTxOuE for caveats.").color(ChatColor.YELLOW).create());
    }
}
