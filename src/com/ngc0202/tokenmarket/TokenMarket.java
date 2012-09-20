package com.ngc0202.tokenmarket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;

/**
 *
 * @author ngc0202
 */
public class TokenMarket extends JavaPlugin implements Listener {

    public static TokenMarket plugin;
    private File dataFile;
    private File endLogFile;
    private File joinsFile;
    private PropertiesFile tokens;
    private PropertiesFile prop;
    private long startBal = 0;
    private ArrayList<TokenBlock> blocks;
    ArrayList<Data2> endLog;
    Map<ArrayList<String>, String> joinCmds = new HashMap<ArrayList<String>, String>();
    private static Map<String, SLocation> active = new HashMap<String, SLocation>();
    private static PermissionManager pex;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(this, this);
        getDataFolder().mkdirs();
        prop = new PropertiesFile((new File(getDataFolder(), "TokenMarket.prop")).getAbsolutePath());
        startBal = prop.getLong("startingBalance", 0);
        dataFile = new File(getDataFolder(), "data.dat");
        endLogFile = new File(getDataFolder(), "permlog.dat");
        joinsFile = new File(getDataFolder(), "offline.dat");
        try {
            if (dataFile.createNewFile()) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile));
                oos.writeObject(new ArrayList<TokenBlock>());
                oos.close();
                getLogger().log(Level.INFO, "data.dat file not found, creating new one.");
            }
            if (endLogFile.createNewFile()) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(endLogFile));
                oos.writeObject(new ArrayList<Data2>());
                oos.close();
                getLogger().log(Level.INFO, "permlog.dat file not found, creating new one.", endLogFile.getName());
            }
            if (joinsFile.createNewFile()) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(joinsFile));
                oos.writeObject(new HashMap<ArrayList<String>, String>());
                oos.close();
                getLogger().log(Level.INFO, "offline.dat file not found, creating new one.", joinsFile.getName());
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        blocks = readData();
        endLog = readData2();
        joinCmds = readJoins();
        File tokenFile = new File(getDataFolder(), "tokens.dat");
        tokens = new PropertiesFile(tokenFile.getAbsolutePath());
        pex = new PermissionManager(getConfig());

        endCheckAll();
//        updateDatabase();
    }

    @Override
    public void onDisable() {
        saveData();
        saveData2();
        saveJoins();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isConsole = true;
        Player ply = null;
        if (sender instanceof Player) {
            isConsole = false;
            ply = (Player) sender;
        }
        if (cmd.getName().equalsIgnoreCase("tokens")) {
            if (args.length == 0) {
                if (isConsole) {
                    sender.sendMessage("This command cannot be executed from the console.");
                }
                long toks = this.getTokens(ply.getName());
                sender.sendMessage(ChatColor.BLUE + "You have " + ChatColor.GOLD + toks + ChatColor.BLUE + " tokens.");
                return true;
            } else if (args.length == 1) {
                String playerName = args[0];
                if (playerName.equalsIgnoreCase("add") || playerName.equalsIgnoreCase("set")) {
                    return false;
                }
                sender.sendMessage(ChatColor.GOLD + playerName + ChatColor.BLUE + " currently has " + ChatColor.GOLD + getTokens(playerName) + ChatColor.BLUE + " tokens.");
                return true;
            } else if (args.length == 3) {
                String act = args[0];
                String tar = args[1];
                Player ptar = getServer().getPlayerExact(tar);
                if (act.equalsIgnoreCase("add")) {
                    if (!(isConsole || pex.has((Player) sender, "TokenMarket.Admin"))) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                        return true;
                    }
                    long add = Long.parseLong(args[2]);
                    long newTok = this.addTokens(tar, add);
                    if (add < 0) {
                        sender.sendMessage(ChatColor.BLUE + "You took from " + ChatColor.GOLD + tar + " " + (add * -1L) + ChatColor.BLUE + " tokens. They now have " + ChatColor.BLUE + newTok + ChatColor.GOLD + " tokens.");
                        if (ptar != null) {
                            ptar.sendMessage(ChatColor.BLUE + "You've had " + ChatColor.GOLD + (add * -1L) + ChatColor.BLUE + " tokens taken. You now have " + ChatColor.GOLD + newTok + ChatColor.BLUE + " tokens.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "You gave " + ChatColor.GOLD + tar + " " + add + ChatColor.BLUE + " tokens. They now have " + ChatColor.GOLD + newTok + ChatColor.BLUE + " tokens.");
                        if (ptar != null) {
                            ptar.sendMessage(ChatColor.BLUE + "You've been given " + ChatColor.GOLD + add + ChatColor.BLUE + " tokens. You now have " + ChatColor.GOLD + newTok + ChatColor.BLUE + " tokens.");
                        }
                    }
                    return true;
                } else if (act.equalsIgnoreCase("set")) {
                    if (!(isConsole || pex.has((Player) sender, "TokenMarket.Admin"))) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                        return true;
                    }
                    long newTok = Long.parseLong(args[2]);
                    this.setTokens(tar, newTok);
                    sender.sendMessage(ChatColor.BLUE + "You've set " + ChatColor.GOLD + tar + ChatColor.BLUE + "'s tokens to " + ChatColor.GOLD + newTok + ChatColor.BLUE + ".");
                    if (ptar != null) {
                        ptar.sendMessage(ChatColor.BLUE + "Your tokens have been set to " + ChatColor.GOLD + newTok + ChatColor.BLUE + ".");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (cmd.getName().equalsIgnoreCase("ctm")) {
            if (isConsole) {
                sender.sendMessage("You cannot create a TokenMarket from the console.");
                return true;
            }
            if (!pex.has((Player) sender, "TokenMarket.CTM")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                return true;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("start")) {
                    if (active.containsKey(ply.getName())) {
                        sender.sendMessage(ChatColor.RED + "You've already started a TokenMarket creation. Type \"/ctm end\" to stop it.");
                        return true;
                    }
                    SLocation tarloc = new SLocation(ply.getTargetBlock(null, 10).getLocation());
                    sender.sendMessage(ChatColor.BLUE + "You've created a TokenMarket at " + tarloc.toString() + "!");
                    sender.sendMessage(ChatColor.BLUE + "To add data to it, type \"/ctm <startFile.txt> <removeFile.txt> <time> <price>\".");
                    sender.sendMessage(ChatColor.BLUE + "Or, type \"/ctm undo\" to cancel the creation.");
                    active.put(ply.getName(), tarloc);
                    return true;
                } else if (args[0].equalsIgnoreCase("undo")) {
                    if (!active.containsKey(ply.getName())) {
                        sender.sendMessage(ChatColor.RED + "You have no active TokenMarket creations to undo.");
                        return true;
                    }
                    sender.sendMessage(ChatColor.RED + "TokenMarket creation successfully cancelled.");
                    SLocation aloc = active.get(ply.getName());
                    for (TokenBlock cur : blocks) {
                        if (cur.getLocation().equals(aloc.getLocation())) {
                            blocks.remove(cur);
                        }
                    }
                    active.remove(ply.getName());
                    return true;
                } else if (args[0].equalsIgnoreCase("end")) {
                    active.remove(ply.getName());
                    ply.sendMessage("Creation successfully ended.");
                    return true;
                } else {
                    return false;
                }
            } else if (args.length == 0) {
                return false;
            } else if (args.length == 4) {
                Pattern std = Pattern.compile("\\w+\\.\\w+\\s\\w+\\.\\w+\\s\\d+[sSmMhHdD]\\s\\d+");
                String argString = args[0] + " " + args[1] + " " + args[2] + " " + args[3];
                if (!std.matcher(argString).matches()) {
                    sender.sendMessage(ChatColor.RED + "Invalid syntax.");
                    sender.sendMessage(ChatColor.GRAY + "\"/ctm <startFile.txt> <removeFile.txt> <time> <price>\"");
                    return true;
                }
                if (!active.containsKey(ply.getName())) {
                    sender.sendMessage(ChatColor.RED + "You don't have an active TokenMarket to add data to.");
                    return false;
                }
                SLocation aloc = active.get(ply.getName());
                TokenBlock ablock = null;
                for (TokenBlock cur : blocks) {
                    if (cur.getLocation().equals(aloc.getLocation())) {
                        ablock = cur;
                        break;
                    }
                }
                boolean fnd = (ablock != null);
                if (!fnd) {
                    ablock = new TokenBlock(aloc.getLocation());
                    blocks.add(ablock);
                }
                ablock.setData(new Data(args[0], args[1], args[2], args[3]));
                sender.sendMessage(ChatColor.GREEN + "Data successfully added! This TokenBlock is now successfully configured.");
                active.remove(ply.getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid syntax.");
                sender.sendMessage(ChatColor.GRAY + "\"/ctm <startFile.txt> <removeFile.txt> <time> <price>\"");
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block bl = event.getClickedBlock();
        if (bl == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!event.getPlayer().hasPermission("TokenMarket.USE")) {
            return;
        }
        SLocation sl = new SLocation(event.getClickedBlock().getLocation());
        TokenBlock tbl = null;
        for (TokenBlock cur : blocks) {
            if (new SLocation(cur.getLocation()).equals(sl)) {
                tbl = cur;
                break;
            }
        }
        if (tbl == null) {
            return;
        }
        if (bl.getTypeId() != tbl.getMaterial().getId()) {
            blocks.remove(tbl);
            return;
        }
        if (getTokens(event.getPlayer().getName()) < tbl.getData().getPrice()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have enough tokens to use this block.");
            return;
        }
        tbl.run(event.getPlayer());
        return;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (active.containsKey(event.getPlayer().getName())) {
            active.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Entry<ArrayList<String>, String> set : joinCmds.entrySet()) {
            String playerName = set.getValue();
            if (playerName.equalsIgnoreCase(event.getPlayer().getName())) {
                ArrayList<String> cmds = set.getKey();
                for (String cmd : cmds) {
                    getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
                }
                joinCmds.remove(set.getKey());
            }
        }
    }

    private ArrayList<TokenBlock> readData() {
        ObjectInputStream istream = null;
        try {
            istream = new ObjectInputStream(new FileInputStream(dataFile));
            ArrayList<TokenBlock> toReturn = (ArrayList<TokenBlock>) istream.readObject();
            if (toReturn != null) {
                return toReturn;
            } else {
                return new ArrayList<TokenBlock>();
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (istream != null) {
                    istream.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ArrayList<TokenBlock>();
    }

    private void saveData() {
        ObjectOutputStream ostream = null;
        try {
            ostream = new ObjectOutputStream(new FileOutputStream(dataFile));
            if (blocks != null) {
                ostream.writeObject(blocks);
            } else {
                ostream.writeObject(new ArrayList<TokenBlock>());
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ostream != null) {
                    ostream.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ArrayList<Data2> readData2() {
        ObjectInputStream istream = null;
        try {
            istream = new ObjectInputStream(new FileInputStream(endLogFile));
            ArrayList<Data2> toReturn = (ArrayList<Data2>) istream.readObject();
            if (toReturn != null) {
                return toReturn;
            } else {
                return new ArrayList<Data2>();
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (istream != null) {
                    istream.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ArrayList<Data2>();
    }

    private void saveData2() {
        ObjectOutputStream ostream = null;
        try {
            ostream = new ObjectOutputStream(new FileOutputStream(endLogFile));
            ostream.writeObject(endLog);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ostream != null) {
                    ostream.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private Map<ArrayList<String>, String> readJoins() {
        ObjectInputStream istream = null;
        try {
            istream = new ObjectInputStream(new FileInputStream(joinsFile));
            Map<ArrayList<String>, String> toReturn = (Map<ArrayList<String>, String>) istream.readObject();
            if (toReturn != null) {
                return toReturn;
            } else {
                return new HashMap<ArrayList<String>, String>();
            }
        } catch (ClassNotFoundException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (istream != null) {
                    istream.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new HashMap<ArrayList<String>, String>();
    }

    private void saveJoins() {
        ObjectOutputStream ostream = null;
        try {
            ostream = new ObjectOutputStream(new FileOutputStream(joinsFile));
            ostream.writeObject(joinCmds);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ostream != null) {
                    ostream.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(TokenMarket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void endCheckAll() {
        for (Data2 data : endLog) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new EndRun(data));
        }
    }

    void extendTime(Data2 dat, long seconds) {
        long oldend = dat.getEnd();
        long end = dat.setEnd(oldend + TimeUnit.SECONDS.toMillis(seconds));
        getServer().getScheduler().cancelTask(dat.getID());
        dat.setID(getServer().getScheduler().scheduleSyncDelayedTask(this, new EndRun(dat),
                TimeUnit.MILLISECONDS.toSeconds(end - System.currentTimeMillis()) * 20));
    }

    Data2 getData(String ply, ArrayList<String> cmds) {
        for (Data2 dat : endLog) {
            if (ply.equalsIgnoreCase(dat.getPlayerName()) && cmds.equals(dat.getCommands())) {
                return dat;
            }
        }
        return null;
    }

    public long getTokens(String ply) {
        return tokens.getLong(ply.toLowerCase(), startBal);
    }

    public void setTokens(String ply, long value) {
        tokens.setLong(ply.toLowerCase(), value);
    }

    public long addTokens(String ply, long value) { //Adds or Removes (+ or - value)
        long preTok = getTokens(ply);
        long newTok = preTok + value;
        tokens.setLong(ply.toLowerCase(), newTok);
        return newTok;
    }

    private void updateDatabase() {
        Map<String, String> toks = new HashMap<String, String>();
        try {
            toks = tokens.returnMap();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        for(Entry<String, String> ent : toks.entrySet()){
            String name = ent.getKey();
            long ptok = Long.parseLong(ent.getValue());

            if(!tokens.keyExists(name))
                continue;
            if(name.equals(name.toLowerCase()))
                continue;
            tokens.removeKey(name);
            tokens.setLong(name.toLowerCase(), ptok);
        }
    }
}
