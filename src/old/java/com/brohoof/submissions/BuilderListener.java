package com.brohoof.submissions;

import java.util.UUID;

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

public class BuilderListener implements Listener {
    private final SubmissionsPlugin plugin;

    public BuilderListener(final SubmissionsPlugin plugin) {
        this.plugin = plugin;
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
        if (!plugin.getPlotManager().isConcerned(player, event.getClickedBlock().getWorld()))
            return;
        final Plot plot = plugin.getPlotManager().getExtendedPlot(event.getClickedBlock().getLocation());
        if (plot == null)
            // do nothing...
            return;
        final Rent rentOfPlayer = plugin.getPlotManager().getRentOfPlayer(player.getUniqueId());
        if (rentOfPlayer == null) {
            // trapdoor check
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TRAP_DOOR) {
                plugin.getLogger().info(player + " attempted to flip a trapdoor in submissions");
                event.setCancelled(true);
                return;
            }
        } else if (!rentOfPlayer.getName().equals(plot.getName())) // Plot
                                                                   // is not
                                                                   // player's
                                                                   // rent
        {
            // trapdoor check
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TRAP_DOOR) {
                plugin.getLogger().info(player + " attempted to flip a trapdoor in submissions");
                event.setCancelled(true);
                return;
            }
        } else
            rentOfPlayer.incrementChanges();// Authorized
        if (!plugin.getConfig().getBoolean("options.block_bonemeal_on_grass"))
            return;
        // bonemeal check
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            if (player.getItemInHand().getTypeId() == 351)
                if (player.getItemInHand().getData().getData() == 15) {
                    if (event.getClickedBlock().getType() == Material.GRASS) {
                        player.sendMessage(plugin.getMessage("block_bonemeal_on_grass"));
                        event.setCancelled(true);
                    }
                    if (event.getClickedBlock().getType() == Material.SAPLING) {
                        player.sendMessage(plugin.getMessage("block_bonemeal_on_sapling"));
                        event.setCancelled(true);
                    }
                }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerLoginEvent(final PlayerLoginEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final Rent playerRent = plugin.getPlotManager().getRentOfPlayer(uuid);
        if (playerRent == null)
            return;
        if (!playerRent.getName().equalsIgnoreCase(event.getPlayer().getName()))
            playerRent.setOwnerName(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoinEvent(final PlayerJoinEvent event) {
        final Rent playerRent = plugin.getPlotManager().getRentOfPlayer(event.getPlayer().getUniqueId());
        if (playerRent == null)
            return;
        event.getPlayer().sendMessage("§f<§dSweetie Belle§f> Welcome back crusader! You have " + plugin.getPermission().getFriendlyTimeRemaning(event.getPlayer()) + " until your rank expires!");
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
        if (!plugin.getPlotManager().isConcerned(player, location.getWorld()))
            return false;
        final boolean isForbidden = plugin.getPlotManager().matchesForbiddenPlayer(player);
        final Plot plot = plugin.getPlotManager().getExtendedPlot(location);
        final Rent rentOfPlayer = plugin.getPlotManager().getRentOfPlayer(player.getUniqueId());
        if (plot == null) {
            if (isForbidden)
                return false;
            // event.setCancelled(true);
            if (rentOfPlayer == null)
                player.sendMessage(plugin.getMessage("outsideplot"));
            else
                player.sendMessage(plugin.getMessage("outsideyourplot", rentOfPlayer.getName()));
            return true;
        } else if (isForbidden) {
            player.sendMessage(plugin.getMessage("notinplots"));
            return true;
        }
        if (rentOfPlayer != null) // Player has a rent
        {
            if (rentOfPlayer.getName().equals(plot.getName())) // Plot is
                                                               // player's rent
            {
                rentOfPlayer.incrementChanges();
                return false; // Authorized
            }
            // Plot is not player's rent
            // event.setCancelled(true);
            player.sendMessage(plugin.getMessage("notyourplot", rentOfPlayer.getName()));
            return true;
        }
        final Plot boundedPlot = plugin.getPlotManager().getBoundedPlot(location);
        if (boundedPlot == null) {
            // event.setCancelled(true);
            player.sendMessage(plugin.getMessage("outsideplot"));
            return true;
        }
        // Player has no rent
        final Rent rentOfPlot = plugin.getPlotManager().getRentByPlot(boundedPlot.getName());
        if (rentOfPlot.isRented()) {
            // Plot is already rented
            // event.setCancelled(true);
            player.sendMessage(plugin.getMessage("alreadytaken"));
            return true;
        }
        if (!plugin.getPlotManager().rent(rentOfPlot.getName(), player, player)) {
            plugin.getLogger().severe("Tried to rent plot " + rentOfPlot.getName() + " by player " + player.getName() + ", but a precondition was not met!");
            return true;
        }
        rentOfPlot.incrementChanges();
        return false;
    }
}
