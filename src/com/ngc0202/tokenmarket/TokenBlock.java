package com.ngc0202.tokenmarket;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author ngc0202
 */
public class TokenBlock implements PluginSerializable<TokenBlock> {

    private TokenMarket plugin;
    private Material mat;
    private SLocation loc;
    private TokenBlockData tokenBlockData;

    public TokenBlock(TokenMarket plugin, Material material, SLocation location, TokenBlockData tokenBlockData) {
        this.plugin = plugin;
        mat = material;
        loc = location;
        this.tokenBlockData = tokenBlockData;
    }

    public TokenBlock(TokenMarket plugin, Material material, Location location) {
        this.plugin = plugin;
        mat = material;
        loc = new SLocation(location);
        tokenBlockData = null;
    }

    public TokenBlock(TokenMarket plugin, Location location) {
        this.plugin = plugin;
        mat = location.getBlock().getType();
        loc = new SLocation(location);
        tokenBlockData = null;
    }

    public Material getMaterial() {
        return mat;
    }

    public Location getLocation() {
        return loc.getLocation();
    }

    public SLocation getSLocation() {
        return loc;
    }

    public TokenBlockData getTokenBlockData() {
        return tokenBlockData;
    }

    public void setTokenBlockData(TokenBlockData dat) {
        tokenBlockData = dat;
    }

    public void run(Player ply) {
        Collection<String> cmds = new ArrayList<String>();
        ArrayList<String> rcmds = new ArrayList<String>();
        String playerName = ply.getName();
        String cur, rcur;
        TokenBlockData dat = getTokenBlockData();
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
            plugin.addTokens(playerName, -dat.getPrice());
            PlayerCommandTimer gottenData = plugin.getCommandTimer(playerName, rcmds);
            if (gottenData != null) {
                plugin.extendTime(gottenData, dat.getTime());
                ply.sendMessage(ChatColor.BLUE + "Timer extended!");
                return;
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
        for (String cmd : cmds) {
            if (cmd != null && !cmd.isEmpty()) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            }
        }
        PlayerCommandTimer add = new PlayerCommandTimer(new ArrayList<String>(rcmds), playerName, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(dat.getTime()));
        plugin.addCommandTimer(add);
        add.setID(plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EndRun(plugin, add), dat.getTickTime()));
    }

    @Override
    public Factory<TokenBlock> getSerializationFactory() {
        return MyFactory.instance;
    }

    static class MyFactory implements Factory<TokenBlock> {
        static final MyFactory instance = new MyFactory();

        @Override
        public void serialize(TokenBlock o, DataOutput output, int version) throws IOException {
            output.writeShort(o.mat.getId());
            o.loc.getSerializationFactory().serialize(o.loc, output, version);
            o.tokenBlockData.getSerializationFactory().serialize(o.tokenBlockData, output, version);
        }

        @Override
        public TokenBlock deserialize(DataInput input, int version, TokenMarket plugin) throws IOException {
            Material mat = Material.getMaterial(input.readShort());
            SLocation loc = SLocation.MyFactory.instance.deserialize(input, version, plugin);
            TokenBlockData tokenBlockData = TokenBlockData.MyFactory.instance.deserialize(input, version, plugin);
            return new TokenBlock(plugin, mat, loc, tokenBlockData);
        }
    }
}
