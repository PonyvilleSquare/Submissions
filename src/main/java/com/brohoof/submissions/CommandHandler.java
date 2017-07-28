package com.brohoof.submissions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.brohoof.submissions.exceptions.PlotCreationException;
import com.google.common.base.Joiner;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

public class CommandHandler {

    private final PlotManager plotManager;
    private final RentManager rentManager;
    private final Permission permission;
    private Settings settings;

    public CommandHandler(final PlotManager plotManager, final RentManager rentManager, final Permission permission, final Settings settings) {
        this.plotManager = plotManager;
        this.rentManager = rentManager;
        this.permission = permission;
        this.settings = settings;
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sorry, this plugin can be used ingame only.");
            return true;
        }
        final Player player = (Player) sender;
        switch (command.getName().toLowerCase()) {
            case "saveandremove": {
                if (args.length < 3)
                    return false;
                final String all_error = ChatColor.DARK_RED + "[FATAL]" + ChatColor.RESET + ChatColor.RED + " Save and remove of submission house aborted. ";
                if(Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
                    player.sendMessage(all_error + "FastAsyncWorldEdit is enabled. Saving and Removing of houses cannot be done while this plugin is enabled. Uninstall FastAsyncWorldEdit and try again.");
                    return true;
                }
                final Optional<Rent> oRent = rentManager.getRent(args[0]);
                if (!oRent.isPresent()) {
                    player.sendMessage(all_error + "I couldn't find a rent for that player!");
                    return true;
                }
                final Rent rent = oRent.get();
                setWorldEditSelection(player, rent.getPlot());
                // Sanity checks
                if (!player.getWorld().equals(plotManager.getWorld())) {
                    player.sendMessage(all_error + "You are not in the submissions world. Please use this command only in the submissions world.");
                }
                double height = player.getLocation().getY();
                if (!(height == Math.floor(height) && height == Math.ceil(height))) {
                    player.sendMessage(all_error + "You are not standing on the ground. Please stand firmly on the ground and try again.");
                }
                String folder = args[1];
                String reason = args[2];
                String extraNotes = Joiner.on(' ').join(ArrayUtils.subarray(args, 3, args.length));
                boolean approved;
                switch (folder.toLowerCase()) {
                    case "approved": {
                        approved = true;
                        break;
                    }
                    case "rejected": {
                        approved = false;
                        break;
                    }
                    default: {
                        player.sendMessage(all_error + "You did not specify to approve or reject!");
                        return true;
                    }
                }
                if (approved)
                    player.sendMessage("Saving and removing approved house...");
                else
                    player.sendMessage("Saving and removing rejected house...");
                String fileNameToSave = "";
                File schemRootDir = null;
                if (approved) {
                    schemRootDir = new File(settings.getSchemFile(), "approved");
                    fileNameToSave = "approved";
                } else {
                    schemRootDir = new File(settings.getSchemFile(), "rejected");
                    fileNameToSave = "rejected";
                }
                String cmc = rent.getOwnerName().toLowerCase();
                File schematicToSave;
                String schemName;
                try {
                    for (int i = 0;; i++) {
                        if (i == 0) {
                            schematicToSave = new File(schemRootDir, cmc + ".schematic");
                            schemName = cmc + ".schematic";
                        } else {
                            schematicToSave = new File(schemRootDir, cmc + "-" + i + ".schematic");
                            schemName = cmc + "-" + i + ".schematic";
                        }
                        if (!schematicToSave.exists())
                            break;
                    }
                } catch (SecurityException ex) {
                    player.sendMessage(all_error + "An unhandled security exception occured when trying to read a schematic file, please check and save manually.");
                    ex.printStackTrace();
                    return true;
                }
                fileNameToSave = fileNameToSave + "/" + schemName;
                Bukkit.dispatchCommand(player, "/copy");
                Bukkit.dispatchCommand(player, "/schematic save mce " + fileNameToSave);
                Bukkit.dispatchCommand(player, "mcprofiler addnote " + cmc + " Removed "+ reason + " house at " + this.getStringFromLocation(player.getLocation()) + ", and saved as " + fileNameToSave + ". " + extraNotes);
                Bukkit.dispatchCommand(player, "/restore " + plotManager.getWorld().getName() + "/fresh");
                Bukkit.dispatchCommand(player, "/sel");
                Bukkit.dispatchCommand(player, "remove items 100");
                player.sendMessage("Save and remove complete.");
                rentManager.removeRent(rent);
                return true;
            }
            case "subm": {
                if (args.length == 0)
                    return false;
                switch (args[0].toLowerCase()) {
                    case "save": {
                        plotManager.saveAllPlots();
                        rentManager.saveAllRents();
                        return true;
                    }
                    case "list": {
                        player.sendMessage(String.format(ChatColor.YELLOW + "Occupied %s out of %s plots (%s empty)", rentManager.getTotalRents(), plotManager.getTotalPlots(), plotManager.getTotalPlots() - rentManager.getTotalRents()));
                        final ArrayList<Rent> rents = rentManager.getRents();
                        Collections.sort(rents);
                        Collections.reverse(rents);
                        for (final Rent rent : rents)
                            displayRentInfo(player, rent);
                        return true;
                    }
                    case "listx": {
                        final int occupied = rentManager.getTotalRents();
                        final int numPlots = plotManager.getTotalPlots();
                        player.sendMessage(String.format(ChatColor.YELLOW + "Occupied %s out of %s open plots (%s empty)", occupied, numPlots, numPlots - occupied));
                        final ArrayList<Plot> plots = plotManager.getPlots();
                        Collections.sort(plots);
                        for (final Plot plot : plots) {
                            final Optional<Rent> rent = rentManager.getRent(plot);
                            if (rent.isPresent())
                                displayRentInfo(player, rent.get());
                            else
                                player.sendMessage(String.format(ChatColor.GRAY + "%s: (empty)", plot.getName()));
                        }
                        return true;
                    }
                    case "getownerof": {
                        if (args.length <= 1) {
                            // no plot name given, so base it off their location
                            Optional<Plot> oPlot = plotManager.getPlot(player.getLocation());
                            if (oPlot.isPresent()) {
                                Plot plot = oPlot.get();
                                Optional<Rent> oRent = rentManager.getRent(plot);
                                if (oRent.isPresent()) {
                                    Rent rent = oRent.get();
                                    player.sendMessage(String.format(ChatColor.YELLOW + "This plot %s is owned by %s", plot.getName(), rent.getOwnerName()));
                                    return true;
                                }
                                player.sendMessage(String.format(ChatColor.YELLOW + "This plot %s is empty", plot.getName()));
                                return true;
                            }
                            player.sendMessage(String.format(ChatColor.GOLD + "This is not a plot"));
                            return true;
                        }
                        // Name given, use that instead.
                        Optional<Plot> oPlot = plotManager.getPlot(args[1]);
                        if (oPlot.isPresent()) {
                            Plot plot = oPlot.get();
                            Optional<Rent> oRent = rentManager.getRent(plot);
                            if (oRent.isPresent()) {
                                Rent rent = oRent.get();
                                player.sendMessage(String.format(ChatColor.YELLOW + "That plot %s is owned by %s", plot.getName(), rent.getOwnerName()));
                                return true;
                            }
                            player.sendMessage(String.format(ChatColor.YELLOW + "That plot %s is empty", plot.getName()));
                            return true;
                        }
                        player.sendMessage(String.format(ChatColor.GOLD + "That is not a plot"));
                        return true;
                    }
                    case "whereisplot": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a plot number!");
                            return true;
                        }
                        final Optional<Plot> plot = plotManager.getPlot(args[1]);
                        if (!plot.isPresent())
                            player.sendMessage(String.format(ChatColor.GOLD + "That is not a plot"));
                        else {
                            Plot pl = plot.get();
                            player.sendMessage(String.format(ChatColor.YELLOW + "Plot %s is located at %s", pl.getName(), getStringFromLocation(pl.getFirstPoint()) + " -> " + getStringFromLocation(pl.getSecondPoint())));
                        }
                        return true;
                    }
                    case "getplotof": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a player.");
                        }
                        final Optional<Rent> rent = rentManager.getRent(args[1]);
                        if (!rent.isPresent())
                            player.sendMessage("This player does not have a plot.");
                        else {
                            Rent re = rent.get();
                            player.sendMessage(String.format(ChatColor.YELLOW + "This plot %s is owned by %s", re.getPlot().getName(), re.getOwnerName()));
                            displayRentInfo(player, re);
                        }
                        return true;
                    }
                    case "release": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a plot number!");
                            return true;
                        }
                        Optional<Plot> oPlot = plotManager.getPlot(args[1]);
                        if (!oPlot.isPresent()) {
                            player.sendMessage("Invalid plot specified!");
                            return true;
                        }
                        Optional<Rent> oRent = rentManager.getRent(oPlot.get());
                        if (oRent.isPresent()) {
                            rentManager.removeRent(oRent.get());
                            return true;
                        }
                        player.sendMessage("That plot does not have a rent!");
                        return true;
                    }
                    case "select": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a plot number!");
                            return true;
                        }
                        final Optional<Plot> op = plotManager.getPlot(args[1]);
                        if (op.isPresent()) {
                            setWorldEditSelection(player, op.get());
                            return true;
                        }
                        player.sendMessage("Invalid plot specified!");
                        return true;
                    }
                    case "releaseowner": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a player!");
                            return true;
                        }
                        final String rentOwner = args[1];
                        final Optional<Rent> or = rentManager.getRent(rentOwner);
                        if (!or.isPresent()) {
                            player.sendMessage("Player does not have a plot!");
                            return true;
                        }
                        final Rent rent = or.get();
                        rentManager.removeRent(rent);
                        return true;
                    }
                    case "selectowner": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a player!");
                            return true;
                        }
                        final String rentOwner = args[1];
                        final Optional<Rent> or = rentManager.getRent(rentOwner);
                        if (!or.isPresent()) {
                            player.sendMessage("Player does not have a plot!");
                            return true;
                        }
                        final Rent rent = or.get();
                        this.setWorldEditSelection(player, rent.getPlot());
                        return true;
                    }
                    case "visualize": {
                        if (args.length <= 1) {
                            player.sendMessage("You must specify a plot number!");
                            return true;
                        }
                        final Optional<Plot> op = plotManager.getPlot(args[1]);
                        if (op.isPresent()) {
                            setWorldEditSelection(player, op.get());
                            return true;
                        }
                        player.sendMessage("Invalid plot specified!");
                        return true;
                    }
                    case "manualrent": {
                        final Player target = getPlayer(args[1]);
                        if (target == null) {
                            player.sendMessage("Player not found.");
                            return true;
                        }
                        final Optional<Plot> oplot = plotManager.getPlot(args[2]);
                        if (!oplot.isPresent())
                            player.sendMessage("Invalid plot specified.");
                        final Plot plot = oplot.get();
                        final Optional<Rent> orent = rentManager.getRent(plot);
                        if (orent.isPresent()) {
                            final Rent rent = orent.get();
                            player.sendMessage(String.format(ChatColor.GOLD + "Cannot reserve: Plot %s is already owned by %s", plot.getName(), rent.getOwnerName()));
                            return true;
                        }
                        rentManager.createRent(plot, target);
                        return true;
                    }
                    case "createplot": {
                        if (args.length <= 1) {
                            player.sendMessage("You must put a name for this plot!");
                            return true;
                        }
                        final Optional<Region> ore = getWorldEditSelection(player);
                        if (!ore.isPresent()) {
                            player.sendMessage(ChatColor.RED + "You must have a WorldEdit selection.");
                            return true;
                        }
                        final Region re = ore.get();
                        final Vector point1 = re.getMinimumPoint();
                        final Vector point2 = re.getMaximumPoint();
                        try {
                            plotManager.createPlot(args[1].toLowerCase(), new Location(player.getWorld(), point1.getBlockX(), point1.getBlockY(), point1.getBlockZ()), new Location(player.getWorld(), point2.getBlockX(), point2.getBlockY(), point2.getBlockZ()));
                        } catch (final PlotCreationException e) {
                            player.sendMessage("Error creating plot: " + e.getMessage());
                            return true;
                        }
                        for (final BlockVector bv : re) {
                            final Location bLocation = new Location(player.getWorld(), bv.getX(), bv.getY(), bv.getZ());
                            final Block b = bLocation.getBlock();
                            if (b.getType() == Material.WOOL && b.getData() == (byte) 11)
                                b.setData((byte) 4);
                        }
                        player.sendMessage("Plot created!");
                        return true;
                    }
                    case "addtry": {
                        if (args.length < 2)
                            return false;
                        final Player target = getPlayer(args[1]);
                        if (target != null) {
                            final int tries = permission.addTry(target);
                            player.sendMessage(ChatColor.YELLOW + target.getName() + " has now taken " + tries + " attempt(s) at submissions.");
                            return true;
                        }
                        final int tries = permission.addTry(args[1]);
                        player.sendMessage(ChatColor.YELLOW + args[1] + " has now taken " + tries + " attempt(s) at submissions.");
                        return true;
                    }
                    case "gettry": {
                        if (args.length < 2)
                            return false;
                        final Player target = getPlayer(args[1]);
                        if (target != null) {
                            final int tries = permission.getTry(target);
                            player.sendMessage(ChatColor.YELLOW + target.getName() + " has taken " + tries + " attempt(s) at submissions.");
                            return true;
                        }
                        final int tries = permission.getTry(args[1]);
                        player.sendMessage(ChatColor.YELLOW + args[1] + " has taken " + tries + " attempt(s) at submissions.");
                        return true;
                    }
                    case "expires": {
                        if (args.length < 2)
                            return false;
                        final Player target = getPlayer(args[1]);
                        if (target != null) {
                            player.sendMessage(ChatColor.YELLOW + target.getName() + "'s submission time expires at " + permission.getTimeStampRemaning(player));
                            return true;
                        }
                        player.sendMessage(ChatColor.YELLOW + args[1] + "'s submission time expires at " + permission.getTimeStampRemaning(args[1]));
                        return true;
                    }
                    case "timeleft": {
                        if (args.length < 2)
                            return false;
                        final Player target = getPlayer(args[1]);
                        if (target != null) {
                            player.sendMessage(ChatColor.YELLOW + target.getName() + "'s submission time expires at " + permission.getFriendlyTimeRemaning(player));
                            return true;
                        }
                        player.sendMessage(ChatColor.YELLOW + args[1] + "'s submission time expires at " + permission.getFriendlyTimeRemaning(args[1]));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getStringFromLocation(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }

    @SuppressWarnings("deprecation")
    private Player getPlayer(final String name) {
        Player possibleplayer;
        for (final Player loopPlayer : Bukkit.getOnlinePlayers())
            if (loopPlayer.getName().equalsIgnoreCase(name))
                return loopPlayer;
        possibleplayer = Bukkit.getPlayerExact(name);
        if (possibleplayer != null)
            return possibleplayer;
        possibleplayer = Bukkit.getPlayer(name);
        if (possibleplayer != null)
            return possibleplayer;
        return null;
    }

    private Optional<Region> getWorldEditSelection(final Player player) {
        final WorldEditPlugin we = SubmissionsPlugin.getWorldEdit();
        try {
            return Optional.<Region>ofNullable(we.getWorldEdit().getSessionManager().getIfPresent(new BukkitPlayer(we, we.getServerInterface(), player)).getRegionSelector((World) BukkitUtil.getLocalWorld(player.getWorld())).getRegion());
        } catch (final Exception e) {
            return Optional.<Region>empty();
        }
    }

    private void setWorldEditSelection(final Player ply, final Plot plot) {
        final org.bukkit.World plotWorld = plotManager.getWorld();
        SubmissionsPlugin.getWorldEdit().setSelection(ply, new CuboidSelection(plotWorld, plot.getFirstPoint(), plot.getSecondPoint()));
    }

    private void displayRentInfo(final Player toSend, final Rent rent) {
        final String plot = rent.getPlot().getName();
        final String owner = Bukkit.getOfflinePlayer(rent.getOwner()).getName();
        final String created = permission.getTimeStamp(rent.getCreated() / 1000);
        final String modified = permission.getTimeStamp(rent.getModified() / 1000);
        final String changes = "" + rent.getChanges();
        final String playerDisplay = ChatColor.translateAlternateColorCodes('&', permission.getPrefix(owner) + owner + permission.getSuffix(owner));
        toSend.sendMessage(String.format(ChatColor.RESET + "%s: %s" + ChatColor.RESET + "(created %s, modified %s, %s changes)", plot, playerDisplay, created, modified, changes));
    }

}
