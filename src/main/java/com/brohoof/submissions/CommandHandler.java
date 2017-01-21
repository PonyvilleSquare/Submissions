package com.brohoof.submissions;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.brohoof.submissions.exceptions.PlotCreationException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import com.sk89q.worldedit.world.World;

public class CommandHandler {

    public CommandHandler() {
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("deprecation")
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
                if (!oRent.isPresent()) {
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
                if (args.length == 0)
                    return false;
                switch (args[0].toLowerCase()) {
                    case "list": {

                    }
                    case "listx": {

                    }
                    case "getownerof": {
                        // plot|location
                    }
                    case "whereisplot": {
                        // plot
                    }
                    case "getplotof": {
                        // player
                    }
                    case "release": {
                        // plot
                    }
                    case "select": {
                        // plot
                    }
                    case "releaseowner": {
                        // player
                    }
                    case "visualize": {
                        // plot
                    }
                    case "manualrent": {
                        // plot player
                    }
                    case "createplot": {
                        // name + selection
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("Ingame only command.");
                            return true;
                        }
                        if (args.length <= 1) {
                            sender.sendMessage("You must put a name for this plot!");
                        }
                        Player player = (Player) sender;
                        Region re = null;
                        try {
                            final WorldEditPlugin we = JavaPlugin.getPlugin(WorldEditPlugin.class);
                            re = we.getWorldEdit().getSessionManager().getIfPresent(new BukkitPlayer(we, we.getServerInterface(), player)).getRegionSelector((World) BukkitUtil.getLocalWorld(player.getWorld())).getRegion();
                        } catch (final IncompleteRegionException e) {
                            sender.sendMessage(ChatColor.RED + "You must have a WorldEdit selection.");
                            return true;
                        }
                        Vector point1 = re.getMinimumPoint();
                        Vector point2 = re.getMaximumPoint();
                        try {
                            Plot.createPlot(args[1].toLowerCase(), new Location(player.getWorld(), point1.getBlockX(), point1.getBlockY(), point1.getBlockZ()), new Location(player.getWorld(), point2.getBlockX(), point2.getBlockY(), point2.getBlockZ()));
                        } catch (PlotCreationException e) {
                            sender.sendMessage("Error creating plot: " + e.getMessage());
                            return true;
                        }
                        for (BlockVector bv : re) {
                            final Location bLocation = new Location(player.getWorld(), bv.getX(), bv.getY(), bv.getZ());
                            Block b = bLocation.getBlock();
                            if (b.getType() == Material.WOOL && b.getData() == ((byte) 11)) {
                                b.setData((byte) 4);
                            }
                        }
                        sender.sendMessage("Plot created!");
                        return true;
                    }
                    case "reload": {

                    }
                    case "addtry": {
                        // player
                    }
                    case "gettry": {
                        // player
                    }
                    case "expires": {
                        // player
                    }
                    case "timeleft": {
                        // player
                    }
                }
            }
        }
        return false;
    }

}
