package com.brohoof.submissions;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class EventListener implements Listener {

    private final Permission perm;
    private final PlotManager plotManager;
    private final SubmissionsPlugin plugin;
    private final RentManager rentManager;

    public EventListener(final SubmissionsPlugin submissionsPlugin, final Permission permission, final PlotManager plot, final RentManager rent) {
        plugin = submissionsPlugin;
        perm = permission;
        plotManager = plot;
        rentManager = rent;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreakEvent(final BlockBreakEvent event) {
        final boolean shouldCancel = proxyEditEvent(event.getPlayer(), event.getBlock().getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlaceEvent(final BlockPlaceEvent event) {
        final boolean shouldCancel = proxyEditEvent(event.getPlayer(), event.getBlock().getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlaceEvent(final HangingPlaceEvent event) {
        final boolean shouldCancel = proxyEditEvent(event.getPlayer(), event.getBlock().getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingRemoveEvent(final HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player))
            return;
        final boolean shouldCancel = proxyEditEvent((Player) event.getRemover(), event.getEntity().getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmptyEvent(final PlayerBucketEmptyEvent event) {
        final boolean shouldCancel = proxyEditEvent(event.getPlayer(), event.getBlockClicked().getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFillEvent(final PlayerBucketFillEvent event) {
        final boolean shouldCancel = proxyEditEvent(event.getPlayer(), event.getBlockClicked().getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Optional<Plot> plot = plotManager.getPlot(event.getClickedBlock().getLocation());
        if (!plot.isPresent())
            // do nothing...
            return;
        final Optional<Rent> rentofPlayer = rentManager.getRent(player.getUniqueId());
        if (!rentofPlayer.isPresent()) {
            // trapdoor check
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TRAP_DOOR) {
                plugin.getLogger().info(player + " attempted to flip a trapdoor in submissions");
                event.setCancelled(true);
                return;
            } // Plot is not player's rent
        } else if (!rentofPlayer.get().getPlot().getName().equals(plot.get().getName())) {
            // trapdoor check
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TRAP_DOOR) {
                plugin.getLogger().info(player + " attempted to flip a trapdoor in submissions");
                event.setCancelled(true);
                return;
            }
        }
        // bonemeal check
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            if (player.getItemInHand().getTypeId() == 351)
                if (player.getItemInHand().getData().getData() == 15) {
                    if (event.getClickedBlock().getType() == Material.GRASS) {
                        player.sendMessage("Sorry, you aren't allowed to use bonemeal.");
                        event.setCancelled(true);
                    }
                    if (event.getClickedBlock().getType() == Material.SAPLING) {
                        player.sendMessage("Sorry, you aren't allowed to use bonemeal.");
                        event.setCancelled(true);
                    }
                }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoinEvent(final PlayerJoinEvent event) {
        final Optional<Rent> playerRent = rentManager.getRent(event.getPlayer().getUniqueId());
        if (!playerRent.isPresent())
            return;
        event.getPlayer().sendMessage(ChatColor.WHITE + "<" + ChatColor.LIGHT_PURPLE + "Sweetie Belle" + ChatColor.WHITE + "> Welcome back crusader! You have " + perm.getFriendlyTimeRemaning(event.getPlayer()) + " until your rank expires!");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerLoginEvent(final PlayerLoginEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final Optional<Rent> playerRent = rentManager.getRent(uuid);
        if (!playerRent.isPresent())
            return;
        if (!playerRent.get().getOwnerName().equalsIgnoreCase(event.getPlayer().getName()))
            playerRent.get().setOwnerName(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrowEvent(final StructureGrowEvent event) {
        if (!event.isFromBonemeal() || event.getPlayer() == null)
            return;
        final boolean shouldCancel = proxyEditEvent(event.getPlayer(), event.getBlocks().get(0).getLocation());
        if (shouldCancel)
            event.setCancelled(true);
    }

    private boolean proxyEditEvent(final Player player, final Location location) {
        if (!plotManager.isConcerned(player, location.getWorld())) {
            // TODO: remove debug
            player.sendMessage("Not concerned about your event.");
            return false;
        }
        final Optional<Plot> plot = plotManager.getPlot(location);
        final Optional<Rent> rent = rentManager.getRent(player.getUniqueId());
        if (!plot.isPresent()) {
            if (!rent.isPresent())
                player.sendMessage(ChatColor.GOLD + "You can only build inside plots. " + ChatColor.WHITE + "Find an empty plot");
            else
                player.sendMessage(ChatColor.GOLD + "You are outside your plot. " + ChatColor.WHITE + "Type /warp " + rent.get().getPlot().getName() + " to warp to your plot");
            return true;
        }
        if (rent.isPresent()) { // Player has a rent
            final Rent rentOfPlayer = rent.get();
            if (rentOfPlayer.getPlot().getName().equals(plot.get().getName())) { // Plot is player's rent
                rentOfPlayer.incrementChanges();
                return false; // Authorized
            }
            // Plot is not player's rent
            player.sendMessage(ChatColor.GOLD + "You can only build on your own plot. " + ChatColor.WHITE + "Type /warp " + rent.get().getPlot().getName() + " to warp to your plot");
            return true;
        }
        final Plot boundedPlot = plot.get();
        // Player has no rent
        final Optional<Rent> rentOfOther = rentManager.getRent(boundedPlot);
        if (rentOfOther.isPresent()) {
            // Plot is already rented
            player.sendMessage(ChatColor.GOLD + "This plot is already taken. " + ChatColor.WHITE + "Find another empty plot.");
            return true;
        }
        final Rent newrent = rentManager.createRent(boundedPlot, player);
        player.sendMessage(ChatColor.YELLOW + "You now own this plot! Good luck! " + ChatColor.WHITE + "You can warp to your plot using /warp " + boundedPlot.getName());
        newrent.incrementChanges();
        return false; // Authorized
    }
}
