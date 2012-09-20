package com.ngc0202.tokenmarket;

import java.util.ArrayList;

/**
 *
 * @author ngc0202
 */
class EndRun implements Runnable {

    private final String ply;
    private final ArrayList<String> cmds;
    private Data2 data;

    public EndRun(String ply, ArrayList<String> cmds) {
        this.ply = ply;
        this.cmds = cmds;
        this.data = getData(ply, cmds);
    }

    public EndRun(Data2 data) {
        this.data = data;
        ply = data.getPlayerName();
        cmds = data.getCommands();
    }

    public void run() {
        if (!getPlugin().getServer().getOfflinePlayer(ply).isOnline()) {
            getPlugin().joinCmds.put(cmds, ply);
            return;
        }
        for (String cmd : cmds) {
            getPlugin().getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), cmd);
        }
        getPlugin().endLog.remove(data);
    }

    private static TokenMarket getPlugin() {
        return TokenMarket.plugin;
    }

    private static Data2 getData(String ply, ArrayList<String> cmds) {
        for (Data2 dat : getPlugin().endLog) {
            if (ply.equalsIgnoreCase(dat.getPlayerName()) && cmds.equals(dat.getCommands())) {
                return dat;
            }
        }
        return null;
    }
}
