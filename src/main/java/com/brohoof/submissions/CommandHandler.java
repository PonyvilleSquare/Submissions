package com.brohoof.submissions;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import com.sk89q.worldedit.world.World;

public class CommandHandler {

    public CommandHandler() {
        // TODO Auto-generated constructor stub
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "saveandremove": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Ingame only command.");
                    return true;
                }
                if (args.length == 0)
                    return false;
                Player player = (Player) sender;
                String all_error = ChatColor.RED + "" + ChatColor.BOLD + "[FATAL]" + ChatColor.RESET + ChatColor.RED + "Save and remove of submission house aborted. ";
                Optional<Rent> oRent = Rent.getRent(args[0]);
                if(!oRent.isPresent()) {
                    player.sendMessage(all_error + "I couldn't find a rent for that player!");
                    return true;
                }
                Rent rent = oRent.get();
                WorldlessCuboid plot = rent.getPlot().getPlot();
                final WorldEditPlugin we = JavaPlugin.getPlugin(WorldEditPlugin.class);
                LocalSession ls = JavaPlugin.getPlugin(WorldEditPlugin.class).getWorldEdit().getSessionManager().getIfPresent(new BukkitPlayer(we, we.getServerInterface(), player));
                ls.setMask((Mask) null); 
                RegionSelector rs = ls.getRegionSelector((World) BukkitUtil.getLocalWorld(player.getWorld()));
                rs.selectPrimary(new Vector(plot.xMin, plot.yMin, plot.zMin), PermissiveSelectorLimits.getInstance());
                rs.selectSecondary(new Vector(plot.xMax, plot.yMax, plot.zMax), PermissiveSelectorLimits.getInstance());
            }
            case "subm": {

            }
        }
        return false;
    }

}
