package com.ngc0202.tokenmarket;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;

public class TokenMarketListener implements Listener {
    private final TokenMarket plugin;

    public TokenMarketListener(final TokenMarket plugin) {
        this.plugin = plugin;
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
        TokenBlock tbl = plugin.getBlockAt(new SLocation(event.getClickedBlock().getLocation()));
        if (tbl == null) {
            return;
        }
        if (bl.getTypeId() != tbl.getMaterial().getId()) {
            plugin.removeTokenBlock(tbl);
            return;
        }
        if (plugin.getTokens(event.getPlayer().getName()) < tbl.getTokenBlockData().getPrice()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have enough tokens to use this block.");
            return;
        }
        tbl.run(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.finishedCreatingMarket(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
//        for (Entry<ArrayList<String>, String> set : joinCmds.entrySet()) {
        Collection<String> cmds = plugin.getAndRemoveCommandsForPlayer(event.getPlayer().getName());
        if (cmds != null) {
            Server server = plugin.getServer();
            ConsoleCommandSender consoleSender = server.getConsoleSender();
            for (String cmd : cmds) {
                server.dispatchCommand(consoleSender, cmd);
            }
        }
    }
}
