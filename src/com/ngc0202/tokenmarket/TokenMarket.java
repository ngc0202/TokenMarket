package com.ngc0202.tokenmarket;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author ngc0202
 */
public class TokenMarket extends JavaPlugin implements Listener {

    public static final int SERIALIZATION_VERSION = 0;
    public static TokenMarket plugin; // For the benefit of TMEnchantRepair
    private File dataFile;
    private File endLogFile;
    private File joinsFile;
    private PropertiesFile tokens;
    private long startBal = 0;
    private Map<SLocation, TokenBlock> blocks;
    private Map<String, PlayerCommandTimer> commandTimers;
    private Map<String, QueuedCommands> joinCmds;
    private Map<String, SLocation> creatingMarket = new HashMap<String, SLocation>();


    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(this, this);
        getDataFolder().mkdirs();
        PropertiesFile prop = new PropertiesFile(new File(getDataFolder(), "TokenMarket.prop").getAbsolutePath());
        startBal = prop.getLong("startingBalance", 0);
        dataFile = new File(getDataFolder(), "data.dat");
        endLogFile = new File(getDataFolder(), "permlog.dat");
        joinsFile = new File(getDataFolder(), "offline.dat");
        try {
            if (dataFile.createNewFile()) {
                getLogger().log(Level.INFO, "data.dat file not found, creating new one.");
                writeCollectionToFile(dataFile, new ArrayList<PluginSerializable>());
            }
            if (endLogFile.createNewFile()) {
                getLogger().log(Level.INFO, "permlog.dat file not found, creating new one.", endLogFile.getName());
                writeCollectionToFile(endLogFile, new ArrayList<PluginSerializable>());
            }
            if (joinsFile.createNewFile()) {
                getLogger().log(Level.INFO, "offline.dat file not found, creating new one.", joinsFile.getName());
                writeCollectionToFile(joinsFile, new ArrayList<PluginSerializable>());
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        List<TokenBlock> blocx = readCollectionFromFile(dataFile, TokenBlock.MyFactory.instance);
        blocks = new HashMap<SLocation, TokenBlock>();
        for (TokenBlock b : blocx) {
            blocks.put(b.getSLocation(), b);
        }
        List<PlayerCommandTimer> commandTimerList = readCollectionFromFile(endLogFile, PlayerCommandTimer.MyFactory.instance);
        commandTimers = new HashMap<String, PlayerCommandTimer>();
        for (PlayerCommandTimer t : commandTimerList) {
            commandTimers.put(t.getPlayerName().toLowerCase(), t);
        }
        List<QueuedCommands> cmds = readCollectionFromFile(joinsFile, QueuedCommands.MyFactory.instance);
        joinCmds = new HashMap<String, QueuedCommands>();
        for (QueuedCommands q : cmds) {
            joinCmds.put(q.getPlayer().toLowerCase(), q);
        }
        File tokenFile = new File(getDataFolder(), "tokens.dat");
        tokens = new PropertiesFile(tokenFile.getAbsolutePath());

        getServer().getPluginManager().registerEvents(new TokenMarketListener(this), this);

        endCheckAll();
//        updateDatabase();
    }

    @Override
    public void onDisable() {
        writeCollectionToFile(dataFile, blocks.values());
        writeCollectionToFile(endLogFile, commandTimers.values());
        writeCollectionToFile(joinsFile, joinCmds.values());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tokens")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command cannot be executed from the console.");
                    return true;
                }
                long toks = this.getTokens(sender.getName());
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
                    if (!sender.hasPermission("TokenMarket.Admin")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                        return true;
                    }
                    long add = Long.parseLong(args[2]);
                    long newTok = this.addTokens(tar, add);
                    if (add < 0) {
                        sender.sendMessage(ChatColor.BLUE + "You took from " + ChatColor.GOLD + tar + ' ' + (add * -1L) + ChatColor.BLUE + " tokens. They now have " + ChatColor.BLUE + newTok + ChatColor.GOLD + " tokens.");
                        if (ptar != null) {
                            ptar.sendMessage(ChatColor.BLUE + "You've had " + ChatColor.GOLD + (add * -1L) + ChatColor.BLUE + " tokens taken. You now have " + ChatColor.GOLD + newTok + ChatColor.BLUE + " tokens.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.BLUE + "You gave " + ChatColor.GOLD + tar + ' ' + add + ChatColor.BLUE + " tokens. They now have " + ChatColor.GOLD + newTok + ChatColor.BLUE + " tokens.");
                        if (ptar != null) {
                            ptar.sendMessage(ChatColor.BLUE + "You've been given " + ChatColor.GOLD + add + ChatColor.BLUE + " tokens. You now have " + ChatColor.GOLD + newTok + ChatColor.BLUE + " tokens.");
                        }
                    }
                    return true;
                } else if (act.equalsIgnoreCase("set")) {
                    if (!sender.hasPermission("TokenMarket.Admin")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                        return true;
                    }
                    long newTok = Long.parseLong(args[2]);
                    this.setTokens(tar, newTok);
                    sender.sendMessage(ChatColor.BLUE + "You've set " + ChatColor.GOLD + tar + ChatColor.BLUE + "'s tokens to " + ChatColor.GOLD + newTok + ChatColor.BLUE + '.');
                    if (ptar != null) {
                        ptar.sendMessage(ChatColor.BLUE + "Your tokens have been set to " + ChatColor.GOLD + newTok + ChatColor.BLUE + '.');
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (cmd.getName().equalsIgnoreCase("ctm")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You cannot create a TokenMarket from the console.");
                return true;
            }
            if (!sender.hasPermission("TokenMarket.CTM")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use that command.");
                return true;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("start")) {
                    if (creatingMarket.containsKey(sender.getName())) {
                        sender.sendMessage(ChatColor.RED + "You've already started a TokenMarket creation. Type \"/ctm end\" to stop it.");
                        return true;
                    }
                    SLocation tarloc = new SLocation(((Player) sender).getTargetBlock(null, 10).getLocation());
                    sender.sendMessage(ChatColor.BLUE + "You've created a TokenMarket at " + tarloc.toString() + '!');
                    sender.sendMessage(ChatColor.BLUE + "To add data to it, type \"/ctm <startFile.txt> <removeFile.txt> <time> <price>\".");
                    sender.sendMessage(ChatColor.BLUE + "Or, type \"/ctm undo\" to cancel the creation.");
                    creatingMarket.put(sender.getName(), tarloc);
                    return true;
                } else if (args[0].equalsIgnoreCase("undo")) {
                    if (!creatingMarket.containsKey(sender.getName())) {
                        sender.sendMessage(ChatColor.RED + "You have no active TokenMarket creations to undo.");
                        return true;
                    }
                    sender.sendMessage(ChatColor.RED + "TokenMarket creation successfully cancelled.");
                    SLocation aloc = creatingMarket.get(sender.getName());
                    blocks.remove(aloc);
                    creatingMarket.remove(sender.getName());
                    return true;
                } else if (args[0].equalsIgnoreCase("end")) {
                    creatingMarket.remove(sender.getName());
                    sender.sendMessage("Creation successfully ended.");
                    return true;
                } else {
                    return false;
                }
            } else if (args.length == 0) {
                return false;
            } else if (args.length == 4) {
                Pattern std = Pattern.compile("\\w+\\.\\w+\\s\\w+\\.\\w+\\s\\d+[sSmMhHdD]\\s\\d+");
                String argString = args[0] + ' ' + args[1] + ' ' + args[2] + ' ' + args[3];
                if (!std.matcher(argString).matches()) {
                    sender.sendMessage(ChatColor.RED + "Invalid syntax.");
                    sender.sendMessage(ChatColor.GRAY + "\"/ctm <startFile.txt> <removeFile.txt> <time> <price>\"");
                    return true;
                }
                SLocation aloc = creatingMarket.get(sender.getName());
                if (aloc == null) {
                    sender.sendMessage(ChatColor.RED + "You don't have an active TokenMarket to add data to.");
                    return false;
                }
                TokenBlock ablock = getBlockAt(aloc);
                if (ablock == null) {
                    ablock = new TokenBlock(this, aloc.getLocation());
                    blocks.put(aloc, ablock);
                }
                ablock.setTokenBlockData(new TokenBlockData(getDataFolder(), args[0], args[1], args[2], args[3]));
                sender.sendMessage(ChatColor.GREEN + "Data successfully added! This TokenBlock is now successfully configured.");
                creatingMarket.remove(sender.getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid syntax.");
                sender.sendMessage(ChatColor.GRAY + "\"/ctm <startFile.txt> <removeFile.txt> <time> <price>\"");
                return false;
            }
        }
        return true;
    }

    private static <T extends PluginSerializable<T>> void writeCollection(DataOutput out, Collection<T> coll, int version) throws IOException {
        out.writeInt(coll.size());
        for (T p : coll) {
            p.getSerializationFactory().serialize(p, out, version);
        }
    }

    private <T> List<T> readCollection(DataInput in, int version, PluginSerializable.Factory<T> factory) throws IOException {
        int size = in.readInt();
        List<T> list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            list.add(factory.deserialize(in, version, this));
        }
        return list;
    }

    private <T extends PluginSerializable<T>> void writeCollectionToFile(File f, Collection<T> coll) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(f));
            out.writeInt(SERIALIZATION_VERSION);
            writeCollection(out, coll, SERIALIZATION_VERSION);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            close(out);
        }
    }

    private <T> List<T> readCollectionFromFile(File f, PluginSerializable.Factory<T> factory) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(f));
            int version = in.readInt();
            return readCollection(in, version, factory);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } finally {
            close(in);
        }
        return new ArrayList<T>();
    }

    public static void close(Closeable ostream) {
        try {
            if (ostream != null) {
                ostream.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void endCheckAll() {
        for (PlayerCommandTimer data : commandTimers.values()) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new EndRun(this, data), data.getRemainingTicks());
        }
    }

    void extendTime(PlayerCommandTimer dat, long seconds) {
        long oldend = dat.getEnd();
        long end = dat.setEnd(oldend + TimeUnit.SECONDS.toMillis(seconds));
        getServer().getScheduler().cancelTask(dat.getID());
        dat.setID(getServer().getScheduler().scheduleSyncDelayedTask(this, new EndRun(this, dat), TimeUnit.MILLISECONDS.toSeconds(end - System.currentTimeMillis()) * 20));
    }

    public PlayerCommandTimer getCommandTimer(String ply, Collection<String> cmds) {
        return commandTimers.get(ply.toLowerCase());
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

    public void addCommandTimer(PlayerCommandTimer timer) {
        commandTimers.put(timer.getPlayerName().toLowerCase(), timer);
    }

    public void removeCommandTimer(PlayerCommandTimer timer) {
        commandTimers.remove(timer.getPlayerName().toLowerCase());
    }

    public void addJoinCommands(QueuedCommands cmds) {
        joinCmds.put(cmds.getPlayer().toLowerCase(), cmds);
    }

    public Collection<String> getAndRemoveCommandsForPlayer(String player) {
        QueuedCommands removed = joinCmds.remove(player.toLowerCase());
        return removed == null ? null : removed.getCommands();
    }

    public void removeTokenBlock(TokenBlock b) {
        blocks.remove(b.getSLocation());
    }

    public void finishedCreatingMarket(String player) {
        creatingMarket.remove(player);
    }

    public TokenBlock getBlockAt(SLocation location) {
        return blocks.get(location);
    }
}
