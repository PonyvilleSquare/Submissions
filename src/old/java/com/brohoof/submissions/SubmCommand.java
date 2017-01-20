package com.brohoof.submissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class SubmCommand implements CommandExecutor {
    private final SubmissionsPlugin plugin;

    public SubmCommand(final SubmissionsPlugin plugin) {
        this.plugin = plugin;
    }

    private void createPlot(final Player requester, final String name) {
        final World w = requester.getWorld();
        if (!plugin.getPlotManager().matchesWorld(w)) {
            requester.sendMessage(plugin.getMessage("create_wrongworld"));
            return;
        }
        final Plot plot = plugin.getPlotManager().getPlotByPlot(name);
        if (plot != null) {
            requester.sendMessage(plugin.getMessage("create_exists", name));
            return;
        }
        final Selection sel = plugin.getWorldEdit().getSelection(requester);
        if (sel == null || !(sel instanceof CuboidSelection)) {
            requester.sendMessage(plugin.getMessage("create_notcuoid"));
            return;
        }
        final CuboidSelection cs = (CuboidSelection) sel;
        if (!plugin.getPlotManager().createPlot(name, cs.getMinimumPoint(), cs.getMaximumPoint()))
            plugin.getLogger().severe("Tried to create plot plot " + name + ", but a precondition was not met!");
        else
            requester.sendMessage(plugin.getMessage("create_none", name));
    }

    private void expropriate(final CommandSender requester, final String name) {
        final Plot plot = plugin.getPlotManager().getPlotByPlot(name);
        if (plot == null) {
            requester.sendMessage(plugin.getMessage("expropriate_none", name));
            return;
        }
        final Rent rentOfPlot = plugin.getPlotManager().getRentByPlot(name);
        if (rentOfPlot != null && rentOfPlot.isRented()) {
            requester.sendMessage(plugin.getMessage("expropriate_owned", name, Bukkit.getPlayer(rentOfPlot.getOwner()).getName()));
            return;
        }
        if (!plugin.getPlotManager().expropriate(name))
            plugin.getLogger().severe("Tried to expropriate plot " + name + ", but a precondition was not met!");
        else
            requester.sendMessage(plugin.getMessage("expropriate_exists", name));
        return;
    }

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

    private void list(final CommandSender sender) {
        final PlotManager plotManager = plugin.getPlotManager();
        sender.sendMessage(plugin.getMessage("count", plotManager.getCountOccupiedRents(), plotManager.getTotalPlots(), plotManager.getTotalPlots() - plotManager.getCountOccupiedRents()));
        final List<Rent> rentsByAge = new ArrayList<Rent>(plotManager.getAllRents());
        for (final Iterator<Rent> iter = rentsByAge.iterator(); iter.hasNext();) {
            final Rent rent = iter.next();
            if (!rent.isRented() || plotManager.isDummyPlayer(rent.getOwner()))
                iter.remove();
        }
        final Comparator<Rent> comparator = CreatedComparator.getInstance();
        Collections.sort(rentsByAge, comparator);
        Collections.reverse(rentsByAge);
        for (final Rent rent : rentsByAge)
            rentInfo(rent, sender);
    }

    private void listx(final CommandSender sender) {
        final PlotManager plotManager = plugin.getPlotManager();
        final int dummies = plotManager.getCountDummyRents();
        if (dummies == 0)
            sender.sendMessage(plugin.getMessage("count", plotManager.getCountOccupiedRents(), plotManager.getTotalPlots(), plotManager.getTotalPlots() - plotManager.getCountOccupiedRents()));
        else
            sender.sendMessage(plugin.getMessage("count_withdummies", plotManager.getCountOccupiedRents(), plotManager.getTotalPlots(), plotManager.getTotalPlots() - plotManager.getCountOccupiedRents(), dummies));
        final List<String> plotNames = new ArrayList<String>(plotManager.getAllPlotNames());
        Collections.sort(plotNames);
        for (final String name : plotNames)
            rentInfo(plotManager.getRentByPlot(name), sender);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length < 1)
            return false;
        if (args[0].equals("getownerof")) {
            if (args.length >= 2)
                whose(sender, args[1]);
            else {
                if (!(sender instanceof Player))
                    return false;
                whose(sender);
            }
        } else if (args[0].equals("whereisplot")) {
            if (args.length < 2)
                return false;
            where(sender, args[1]);
        } else if (args[0].equals("getplotof")) {
            if (args.length < 2)
                return false;
            which(sender, args[1]);
        } else if (args[0].equals("release")) {
            if (args.length < 2)
                return false;
            release(sender, args[1]);
        } else if (args[0].equals("manualrent")) {
            if (args.length < 3)
                return false;
            reserve(sender, args[1], args[2]);
        } else if (args[0].equals("list"))
            list(sender);
        else if (args[0].equals("listx"))
            listx(sender);
        else if (args[0].equals("select")) {
            if (!(sender instanceof Player))
                return false;
            if (args.length < 2)
                return false;
            if (args.length >= 3)
                select((Player) sender, args[1], Integer.parseInt(args[2]));
            else
                select((Player) sender, args[1]);
        } else if (args[0].equals("releaseowner")) {
            if (args.length < 2)
                return false;
            final Player player = getPlayer(args[1]);
            if (player != null) {
                final Rent rent = plugin.getPlotManager().getRentOfPlayer(player.getUniqueId());
                if (rent == null) {
                    sender.sendMessage(plugin.getMessage("rent_nomatch", args[1]));
                    return true;
                }
                release(sender, rent.getName());
                return true;
            }
            @SuppressWarnings("deprecation")
            final Rent rent = plugin.getPlotManager().getRentOfPlayer(args[1]);
            if (rent == null) {
                sender.sendMessage(plugin.getMessage("rent_nomatch", args[1]));
                return true;
            }
            release(sender, rent.getName());
            return true;
        } else if (args[0].equals("selectowner")) {
            if (!(sender instanceof Player))
                return false;
            if (args.length < 2)
                return false;
            final Player player = getPlayer(args[1]);
            Rent rent = null;
            if (player != null)
                rent = plugin.getPlotManager().getRentOfPlayer(player.getUniqueId());
            else
                rent = plugin.getPlotManager().getRentOfPlayer(args[1]);
            if (rent == null) {
                sender.sendMessage(plugin.getMessage("rent_nomatch", args[1]));
                return true;
            }
            if (args.length >= 3)
                select((Player) sender, rent.getName(), Integer.parseInt(args[2]));
            else
                select((Player) sender, rent.getName());
        } else if (args[0].equals("visualize")) {
            if (!(sender instanceof Player))
                return false;
            if (args.length < 2)
                return false;
            if (args.length >= 3)
                visualize((Player) sender, args[1], Integer.parseInt(args[2]));
            else
                visualize((Player) sender, args[1]);
        } else if (args[0].equals("createplot")) {
            if (!(sender instanceof Player))
                return false;
            if (args.length < 2)
                return false;
            createPlot((Player) sender, args[1]);
        } else if (args[0].equals("expropriate")) {
            if (args.length < 2)
                return false;
            expropriate(sender, args[1]);
        } else if (args[0].equals("reload"))
            reload(sender);
        else if (args[0].equals("addtry")) {
            if (args.length < 2)
                return false;
            final Player player = getPlayer(args[1]);
            if (player != null) {
                final int tries = plugin.getPermission().addTry(player, args[1]);
                sender.sendMessage("§e" + player.getName() + " has now taken " + tries + " attempt(s) at submissions.");
                return true;
            }
            final int tries = plugin.getPermission().addTry(player, args[1]);
            sender.sendMessage("§e" + args[1] + " has now taken " + tries + " attempt(s) at submissions.");
            return true;
        } else if (args[0].equals("gettry")) {
            if (args.length < 2)
                return false;
            final Player player = getPlayer(args[1]);
            if (player != null) {
                final int tries = plugin.getPermission().getTry(player, args[1]);
                sender.sendMessage("§e" + player.getName() + " has taken " + tries + " attempt(s) at submissions.");
                return true;
            }
            final int tries = plugin.getPermission().getTry(player, args[1]);
            sender.sendMessage("§e" + args[1] + " has taken " + tries + " attempt(s) at submissions.");
            return true;
        } else if (args[0].equalsIgnoreCase("expires")) {
            if (args.length < 2)
                return false;
            final Player player = getPlayer(args[1]);
            if (player != null) {
                sender.sendMessage("§e" + player.getName() + "'s submission time expires at " + plugin.getPermission().getTimeStampRemaning(player));
                return true;
            }
            sender.sendMessage("§e" + args[1] + "'s submission time expires at " + plugin.getPermission().getTimeStampRemaning(args[1]));
            return true;
        } else if (args[0].equalsIgnoreCase("timeleft")) {
            if (args.length < 2)
                return false;
            final Player player = getPlayer(args[1]);
            if (player != null) {
                sender.sendMessage("§e" + player.getName() + "'s submission time expires at " + plugin.getPermission().getFriendlyTimeRemaning(player));
                return true;
            }
            sender.sendMessage("§e" + args[1] + "'s submission time expires at " + plugin.getPermission().getFriendlyTimeRemaning(args[1]));
            return true;
        } else
            return false;
        return true;
    }

    private void release(final CommandSender requester, final String name) {
        final Rent rentOfPlot = plugin.getPlotManager().getRentByPlot(name);
        if (rentOfPlot == null) {
            requester.sendMessage(plugin.getMessage("release_none"));
            // this.plugin.getLogger().info(this.plugin.getMessage("release_none"));
            return;
        }
        if (!rentOfPlot.isRented()) {
            requester.sendMessage(plugin.getMessage("release_exists", rentOfPlot.getName()));
            // this.plugin.getLogger().info(this.plugin.getMessage("release_exists",
            // rentOfPlot.getName()));
            return;
        }
        if (!plugin.getPlotManager().release(name, requester))
            plugin.getLogger().severe("Tried to release plot " + name + ", but a precondition was not met!");
        return;
    }

    private void reload(final CommandSender sender) {
        sender.sendMessage("Reloading...");
        plugin.reload(false);
    }

    private void rentInfo(final Rent rent, final CommandSender requester) {
        final String plot = rent.getName();
        if (rent.isRented()) {
            final String player = rent.getOwnerName();
            final String created = Simplificator.humanTimeDiff(System.currentTimeMillis(), rent.getCreated());
            final String modified = Simplificator.humanTimeDiff(System.currentTimeMillis(), rent.getModified());
            final String changes = "" + rent.getChanges();
            final String playerDisplay = plugin.getPermission().getPrefix(player) + player + plugin.getPermission().getSuffix(player);
            if (!plugin.getPlotManager().isDummyPlayer(player))
                requester.sendMessage(plugin.getMessage("display_owned", plot, playerDisplay, created, modified, changes));
            else
                requester.sendMessage(plugin.getMessage("display_dummy", plot, playerDisplay, created, modified, changes));
        } else
            requester.sendMessage(plugin.getMessage("display_exists", plot));
    }

    private void reserve(final CommandSender requester, final String plot, final String playername) {
        final Player player = getPlayer(playername);
        final Rent rentOfPlot = plugin.getPlotManager().getRentByPlot(plot);
        if (rentOfPlot == null) {
            requester.sendMessage(plugin.getMessage("reserve_none"));
            return;
        }
        if (rentOfPlot.isRented()) {
            requester.sendMessage(plugin.getMessage("reserve_owned", rentOfPlot.getName(), Bukkit.getPlayer(rentOfPlot.getOwner()).getName()));
            return;
        }
        if (!plugin.getPlotManager().rent(plot, player, requester))
            plugin.getLogger().severe("Tried to reserve plot " + plot + ", but a precondition was not met!");
        return;
    }

    private void select(final Player requester, final String name) {
        select(requester, name, 0);
    }

    private void select(final Player requester, final String name, final int minus) {
        final World w = requester.getWorld();
        if (!plugin.getPlotManager().matchesWorld(w)) {
            requester.sendMessage(plugin.getMessage("select_wrongworld"));
            return;
        }
        final Plot plot = plugin.getPlotManager().getPlotByPlot(name);
        if (plot == null) {
            requester.sendMessage(plugin.getMessage("select_none"));
            return;
        }
        final WorldlessCuboid wc = plot.getExtended();
        final Location min = wc.createMin(w);
        final Location max = wc.createMax(w);
        max.setY(max.getY() - minus);
        final Selection sel = new CuboidSelection(w, min, max);
        plugin.getWorldEdit().setSelection(requester, sel);
        requester.sendMessage(plugin.getMessage("select_exists", name));
    }

    private void visualize(final Player requester, final String name) {
        visualize(requester, name, 0);
    }

    private void visualize(final Player requester, final String name, final int i) {
        final World w = requester.getWorld();
        if (!plugin.getPlotManager().matchesWorld(w)) {
            requester.sendMessage(plugin.getMessage("select_wrongworld"));
            return;
        }
        final Plot plot = plugin.getPlotManager().getPlotByPlot(name);
        if (plot == null) {
            requester.sendMessage(plugin.getMessage("select_none"));
            return;
        }
        final WorldlessCuboid wc = plot.getBounded();
        final Location min = wc.createMin(w);
        final Location max = wc.createMax(w);
        min.setY(requester.getLocation().getBlockY() - i);
        max.setY(requester.getLocation().getBlockY() - i);
        final Selection sel = new CuboidSelection(w, min, max);
        plugin.getWorldEdit().setSelection(requester, sel);
        requester.sendMessage(plugin.getMessage("select_visualize", name));
    }

    private void where(final CommandSender sender, final String name) {
        final Plot plot = plugin.getPlotManager().getPlotByPlot(name);
        if (plot == null)
            sender.sendMessage(plugin.getMessage("whereisthatplot_none"));
        else
            sender.sendMessage(plugin.getMessage("whereisthatplot_exists", plot.getName(), plot.getAnchor() + " -> " + plot.getSize()));
    }

    private void which(final CommandSender sender, final String player) {
        final Rent rent = plugin.getPlotManager().getRentOfPlayer(player);
        if (rent == null)
            sender.sendMessage(plugin.getMessage("whichplotistheirs_none", player.toLowerCase()));
        else {
            sender.sendMessage(plugin.getMessage("whichplotistheirs_owned", player.toLowerCase(), rent.getName()));
            rentInfo(rent, sender);
        }
    }

    private void whose(final CommandSender ply) {
        whose(ply, null);
    }

    private void whose(final CommandSender ply, final String name) {
        final Plot plot = name == null && ply instanceof Player ? plugin.getPlotManager().getExtendedPlot(((Player) ply).getLocation()) : plugin.getPlotManager().getPlotByPlot(name);
        if (plot == null)
            ply.sendMessage(plugin.getMessage("whoseplotisthis_none"));
        else if (!plugin.getPlotManager().getRentByPlot(plot.getName()).isRented())
            ply.sendMessage(plugin.getMessage("whoseplotisthis_exists", plot.getName()));
        else {
            final Rent renter = plugin.getPlotManager().getRentByPlot(plot.getName());
            ply.sendMessage(plugin.getMessage("whoseplotisthis_owned", plot.getName(), renter.getOwnerName()));
            rentInfo(renter, ply);
        }
    }
}
