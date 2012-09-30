package com.ngc0202.tokenmarket;

import java.util.Collection;

/**
 * @author ngc0202
 */
class EndRun implements Runnable {

    private final TokenMarket plugin;
    private final String ply;
    private final Collection<String> cmds;
    private PlayerCommandTimer data;

    public EndRun(TokenMarket plugin, String ply, Collection<String> cmds) {
        this.plugin = plugin;
        this.ply = ply;
        this.cmds = cmds;
        this.data = plugin.getCommandTimer(ply, cmds);
    }

    public EndRun(TokenMarket plugin, PlayerCommandTimer data) {
        this.plugin = plugin;
        this.data = data;
        ply = data.getPlayerName();
        cmds = data.getCommands();
    }

    @Override
    public void run() {
        if (plugin.getServer().getOfflinePlayer(ply).isOnline()) {
            for (String cmd : cmds) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            }
            plugin.removeCommandTimer(data);
        } else {
            plugin.addJoinCommands(new QueuedCommands(ply, cmds));
        }
    }
}
