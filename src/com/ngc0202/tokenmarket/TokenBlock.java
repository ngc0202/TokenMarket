package com.ngc0202.tokenmarket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author ngc0202
 */
public class TokenBlock implements Serializable {

    private Material mat;
    private SLocation loc;
    private Data data;

    public TokenBlock(Material material, Location location, Data data) {
        mat = material;
        loc = new SLocation(location);
        this.data = data;
    }

    public TokenBlock(Material material, Location location) {
        mat = material;
        loc = new SLocation(location);
        data = null;
    }

    public TokenBlock(Location location) {
        mat = location.getBlock().getType();
        loc = new SLocation(location);
        data = null;
    }

    public Material getMaterial() {
        return mat;
    }

    public Location getLocation() {
        return loc.getLocation();
    }

    public Data getData() {
        return data;
    }

    public void setData(Data dat) {
        data = dat;
    }

    public TokenMarket getPlugin() {
        return TokenMarket.plugin;
    }

    public void run(Player ply) {
        ArrayList<String> cmds = new ArrayList<String>();
        ArrayList<String> rcmds = new ArrayList<String>();
        String playerName = ply.getName();
        String cur, rcur;
        Data dat = getData();
        BufferedReader read = dat.getStartFileReader();
        BufferedReader rread = dat.getRemoveFileReader();
        try {
            cur = read.readLine();
            if (cur == null || cur.isEmpty()) {
                ply.sendMessage(ChatColor.RED + "This block has yet to be set up by an admin.");
                return;
            }
            while (cur != null && !cur.isEmpty()) {
                cur = cur.replace("%p", playerName);
                cmds.add(cur);
                cur = read.readLine();
            }
            rcur = rread.readLine();
            if (rcur == null || rcur.isEmpty()) {
                ply.sendMessage(ChatColor.RED + "This block has yet to be configured by an admin.");
                return;
            }
            while (rcur != null && !rcur.isEmpty()) {
                rcur = rcur.replace("%p", playerName);
                rcmds.add(rcur);
                rcur = rread.readLine();
            }
            getPlugin().addTokens(playerName, (getData().getPrice() * -1));
            Data2 gottenData = getPlugin().getData(playerName, rcmds);
            if (gottenData != null) {
                getPlugin().extendTime(gottenData, dat.getTime());
                ply.sendMessage(ChatColor.BLUE + "Timer extended!");
                return;
            }
        } catch (IOException ex) {
            getPlugin().getLogger().log(Level.SEVERE, null, ex);
        }
        for (String cmd : cmds) {
            if (cmd != null && !cmd.isEmpty()) {
                getPlugin().getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), cmd);
            }
        }
        Data2 add = new Data2(new ArrayList<String>(rcmds), playerName, System.currentTimeMillis() + (TimeUnit.SECONDS.toMillis(dat.getTime())));
        getPlugin().endLog.add(add);
        add.setID(getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new EndRun(add), dat.getTickTime()));
    }
}
