package me.Percyqaz.UltimateInventory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import de.chriis.advancedenderchest.manager.EnderchestManager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener
{
    UltimateInventory plugin;
    FileConfiguration config;
    boolean isPaper;
    boolean isAdvancedEnderchestPresent;
    Map<UUID, ItemStack> openShulkerBoxes = new HashMap<>();
    Map<UUID, Map.Entry<String, Integer>> playerAdvancedChests = new HashMap<>(); // Map to store chest IDs

    boolean enableShulkerbox;
    boolean overrideShulkerbox;
    String commandShulkerbox;

    boolean enableEnderChest;
    boolean overrideEnderChest;
    String commandEnderChest;

    boolean enableCraftingTable;
    boolean overrideCraftingTable;
    String commandCraftingTable;

    boolean enableSmithingTable;
    boolean overrideSmithingTable;
    String commandSmithingTable;

    boolean enableStoneCutter;
    boolean overrideStoneCutter;
    String commandStoneCutter;

    boolean enableGrindstone;
    boolean overrideGrindstone;
    String commandGrindstone;

    boolean enableCartographyTable;
    boolean overrideCartographyTable;
    String commandCartographyTable;

    boolean enableLoom;
    boolean overrideLoom;
    String commandLoom;

    boolean enableAnvil;
    boolean overrideAnvil;
    String commandAnvil;

    boolean usePermissions;

    public InventoryListener(UltimateInventory plugin, FileConfiguration config, boolean isPaper, boolean isAdvancedEnderchestPresent) {
        this.config = config;
        this.plugin = plugin;
        this.isPaper = isPaper;
        this.isAdvancedEnderchestPresent = isAdvancedEnderchestPresent;

        enableShulkerbox = config.getBoolean("shulkerbox.enable", true);
        overrideShulkerbox = config.getBoolean("shulkerbox.override", false);
        commandShulkerbox = config.getString("shulkerbox.command", "");

        enableEnderChest = config.getBoolean("enderChest.enable", true);
        overrideEnderChest = config.getBoolean("enderChest.override", false);
        commandEnderChest = config.getString("enderChest.command", "");

        enableCraftingTable = config.getBoolean("craftingTable.enable", true);
        overrideCraftingTable = config.getBoolean("craftingTable.override", false);
        commandCraftingTable = config.getString("craftingTable.command", "");

        if (isPaper) {
            enableSmithingTable = config.getBoolean("smithingTable.enable", true);
            overrideSmithingTable = config.getBoolean("smithingTable.override", false);
            commandSmithingTable = config.getString("smithingTable.command", "");

            enableStoneCutter = config.getBoolean("stoneCutter.enable", true);
            overrideStoneCutter = config.getBoolean("stoneCutter.override", false);
            commandStoneCutter = config.getString("stoneCutter.command", "");

            enableGrindstone = config.getBoolean("grindstone.enable", true);
            overrideGrindstone = config.getBoolean("grindstone.override", false);
            commandGrindstone = config.getString("grindstone.command", "");

            enableCartographyTable = config.getBoolean("cartographyTable.enable", true);
            overrideCartographyTable = config.getBoolean("cartographyTable.override", false);
            commandCartographyTable = config.getString("cartographyTable.command", "");

            enableLoom = config.getBoolean("loom.enable", true);
            overrideLoom = config.getBoolean("loom.override", false);
            commandLoom = config.getString("loom.command", "");

            enableAnvil = config.getBoolean("anvil.enable", false);
            overrideAnvil = config.getBoolean("anvil.override", false);
            commandAnvil = config.getString("anvil.command", "");
        }

        usePermissions = config.getBoolean("usePermissions", false);
    }

    private void executeCommand(Player player, String command) {
        if (command.isEmpty()) {
            return;
        }
        player.performCommand(command);
    }

    private boolean IsShulkerBox(Material material)
    {
        switch (material)
        {
            case SHULKER_BOX:
            case RED_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    private boolean IsAnvil(Material material)
    {
        switch (material)
        {
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
                return true;
            default:
                return false;
        }
    }

    private void ShowEnderchest(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.ENDER_CHEST)
        {
            player.closeInventory();
            Bukkit.getServer().getPlayer(player.getUniqueId()).playSound(player, Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.2f);
        }
        else
        {
            player.openInventory(player.getEnderChest());
            Bukkit.getServer().getPlayer(player.getUniqueId()).playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 1.0f, 1.2f);
        }
    }

    private void ShowCraftingTable(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.WORKBENCH)
        {
            return;
        }

        player.openWorkbench(null, true);
    }

    private void ShowStoneCutter(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.STONECUTTER)
        {
            return;
        }

        player.openStonecutter(null, true);
    }

    private void ShowCartographyTable(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.CARTOGRAPHY)
        {
            return;
        }

        player.openCartographyTable(null, true);
    }

    private void ShowLoom(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.LOOM)
        {
            return;
        }

        player.openLoom(null, true);
    }

    private void ShowSmithingTable(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.SMITHING)
        {
            return;
        }

        player.openSmithingTable(null, true);
    }

    private void ShowGrindstone(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.GRINDSTONE)
        {
            return;
        }

        player.openGrindstone(null, true);
    }

    private void ShowAnvil(HumanEntity player)
    {
        if (player.getOpenInventory().getType() == InventoryType.ANVIL)
        {
            return;
        }

        player.openAnvil(null, true);
    }

    private void OpenShulkerbox(HumanEntity player, ItemStack shulkerItem)
    {
        // Don't open the box if already open (avoids a duplication bug)
        if (openShulkerBoxes.containsKey(player.getUniqueId()) && openShulkerBoxes.get(player.getUniqueId()).equals(shulkerItem))
        {
            return;
        }

        // Added NBT for "locking" to prevent stacking shulker boxes
        ItemMeta meta = shulkerItem.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey nbtKey = new NamespacedKey(plugin, "__shulkerbox_plugin");
        if(!data.has(nbtKey, PersistentDataType.STRING))
        {
            data.set(nbtKey, PersistentDataType.STRING, String.valueOf(System.currentTimeMillis()));
            shulkerItem.setItemMeta(meta);
        }

        Inventory shulker_inventory = ((ShulkerBox)((BlockStateMeta)meta).getBlockState()).getSnapshotInventory();
        Inventory inventory;
        if (!meta.hasDisplayName())
        {
            inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX);
        }
        else
        {
            inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, meta.getDisplayName());
        }
        inventory.setContents(shulker_inventory.getContents());

        player.openInventory(inventory);
        Bukkit.getServer().getPlayer(player.getUniqueId()).playSound(player, Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1.0f, 1.2f);

        openShulkerBoxes.put(player.getUniqueId(), shulkerItem);
    }

    private void CloseShulkerbox(HumanEntity player)
    {
        ItemStack shulkerItem = openShulkerBoxes.get(player.getUniqueId());
        BlockStateMeta meta = (BlockStateMeta)shulkerItem.getItemMeta();
        ShulkerBox shulkerbox = (ShulkerBox)meta.getBlockState();

        // Update the shulker box inventory
        shulkerbox.getInventory().setContents(player.getOpenInventory().getTopInventory().getContents());

        // Apply the updated block state back to the meta, see: https://jd.papermc.io/paper/1.21.3/org/bukkit/inventory/meta/BlockStateMeta.html#setBlockState(org.bukkit.block.BlockState)
        meta.setBlockState(shulkerbox);

        // Delete NBT for "locking" to prevent stacking shulker boxes
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey nbtKey = new NamespacedKey(plugin, "__shulkerbox_plugin");
        if(data.has(nbtKey, PersistentDataType.STRING))
        {
            data.remove(nbtKey);
        }

        // Apply the updated meta to the item
        shulkerItem.setItemMeta(meta);

        // If AdvancedEnderchests is present check if the player was previously using an AdvancedEnderchest
        if (isAdvancedEnderchestPresent && playerAdvancedChests.containsKey(player.getUniqueId())) {
            Map.Entry<String, Integer> chestEntry = playerAdvancedChests.get(player.getUniqueId());
            if (chestEntry != null) {
                String chestId = chestEntry.getKey();
                int slot = chestEntry.getValue();
                playerAdvancedChests.remove(player.getUniqueId()); // Remove the chest ID

                // Get the old chest data
                EnderchestManager.getItemsByChestID(player.getUniqueId(), chestId, (ItemStack[] itemStacks) -> {
                    // Update the chest data
                    itemStacks[slot] = shulkerItem;
                    EnderchestManager.saveEnderchest(player.getUniqueId(), chestId, itemStacks);
                });
            }
        }

        Bukkit.getServer().getPlayer(player.getUniqueId()).playSound(player, Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.2f);

        openShulkerBoxes.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void InventoryClick(InventoryClickEvent e) {
        if (e.getAction() == InventoryAction.NOTHING) {
            return;
        }

        if (!e.isRightClick() || e.isShiftClick()) {
            if (openShulkerBoxes.containsKey(e.getWhoClicked().getUniqueId()) &&
                    (e.getAction() == InventoryAction.HOTBAR_SWAP
                            || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
                            || (e.getCurrentItem() != null && IsShulkerBox(e.getCurrentItem().getType())))) {
                e.setCancelled(true);
            }
            return;
        }

        InventoryType clickedInventory = e.getClickedInventory().getType();
        if (!(clickedInventory == InventoryType.PLAYER || clickedInventory == InventoryType.ENDER_CHEST || clickedInventory == InventoryType.SHULKER_BOX)) {
            // Check if the inventory is a virtual chest from AdvancedEnderchests
            if (clickedInventory == InventoryType.CHEST && isAdvancedEnderchestPresent) {
                Component inventoryTitle = e.getView().title();

                // Check if the title is an AdvancedEnderchest
                if (!inventoryTitle.toString().contains("AEC Multi-EC")) {
                    return;
                }
            } else {
                return;
            }
        }

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getAmount() != 1) {
            return;
        }

        Material itemType = item.getType();
        HumanEntity player = e.getWhoClicked();

        if (clickedInventory != InventoryType.SHULKER_BOX && IsShulkerBox(itemType) && enableShulkerbox && (!usePermissions || player.hasPermission("ultimateinventory.shulkerbox"))) {
            if (overrideShulkerbox) {
                executeCommand((Player) player, commandShulkerbox);
            } else {

                // Check if the player was previously using an AdvancedEnderchest
                Component inventoryTitle = e.getView().title();

                // Check if the title is an AdvancedEnderchest
                if (inventoryTitle.toString().contains("AEC Multi-EC") && isAdvancedEnderchestPresent) {
                    Pattern pattern = Pattern.compile("Chest\\s+(\\d+)");
                    Matcher matcher = pattern.matcher(inventoryTitle.toString());

                    if (matcher.find()) {
                        String chestNumber = matcher.group(1);
                        String chestId = "aec.multi.chest." + chestNumber;
                        Map.Entry<String, Integer> chestEntry = new AbstractMap.SimpleEntry<>(chestId, e.getRawSlot());
                        playerAdvancedChests.put(player.getUniqueId(), chestEntry); // Store the chest data
                    }
                }

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> OpenShulkerbox(player, item));
            }
            e.setCancelled(true);
        }

        if (itemType == Material.ENDER_CHEST && enableEnderChest && (!usePermissions || player.hasPermission("ultimateinventory.enderchest"))) {
            if (overrideEnderChest) {
                executeCommand((Player) player, commandEnderChest);
            } else {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowEnderchest(player));
            }
            e.setCancelled(true);
        }

        if (itemType == Material.CRAFTING_TABLE && enableCraftingTable && (!usePermissions || player.hasPermission("ultimateinventory.craftingtable"))) {
            if (overrideCraftingTable) {
                executeCommand((Player) player, commandCraftingTable);
            } else {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowCraftingTable(player));
            }
            e.setCancelled(true);
        }

        if (isPaper) {
            if (itemType == Material.STONECUTTER && enableStoneCutter && (!usePermissions || player.hasPermission("ultimateinventory.stonecutter"))) {
                if (overrideStoneCutter) {
                    executeCommand((Player) player, commandStoneCutter);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowStoneCutter(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.CARTOGRAPHY_TABLE && enableCartographyTable && (!usePermissions || player.hasPermission("ultimateinventory.cartographytable"))) {
                if (overrideCartographyTable) {
                    executeCommand((Player) player, commandCartographyTable);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowCartographyTable(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.LOOM && enableLoom && (!usePermissions || player.hasPermission("ultimateinventory.loom"))) {
                if (overrideLoom) {
                    executeCommand((Player) player, commandLoom);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowLoom(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.SMITHING_TABLE && enableSmithingTable && (!usePermissions || player.hasPermission("ultimateinventory.smithingtable"))) {
                if (overrideSmithingTable) {
                    executeCommand((Player) player, commandSmithingTable);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowSmithingTable(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.GRINDSTONE && enableGrindstone && (!usePermissions || player.hasPermission("ultimateinventory.grindstone"))) {
                if (overrideGrindstone) {
                    executeCommand((Player) player, commandGrindstone);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowGrindstone(player));
                }
                e.setCancelled(true);
            }

            if (IsAnvil(itemType) && enableAnvil && (!usePermissions || player.hasPermission("ultimateinventory.anvil"))) {
                if (overrideAnvil) {
                    executeCommand((Player) player, commandAnvil);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowAnvil(player));
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void RightClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Material itemType = item.getType();

        if (IsShulkerBox(itemType) && item.getAmount() == 1 && enableShulkerbox && (!usePermissions || player.hasPermission("ultimateinventory.shulkerbox"))) {
            if (overrideShulkerbox) {
                executeCommand(player, commandShulkerbox);
            } else {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> OpenShulkerbox(player, item));
            }
            e.setCancelled(true);
        }

        if (itemType == Material.ENDER_CHEST && enableEnderChest && (!usePermissions || player.hasPermission("ultimateinventory.enderchest"))) {
            if (overrideEnderChest) {
                executeCommand(player, commandEnderChest);
            } else {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowEnderchest(player));
            }
            e.setCancelled(true);
        }

        if (itemType == Material.CRAFTING_TABLE && enableCraftingTable && (!usePermissions || player.hasPermission("ultimateinventory.craftingtable"))) {
            if (overrideCraftingTable) {
                executeCommand(player, commandCraftingTable);
            } else {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowCraftingTable(player));
            }
            e.setCancelled(true);
        }

        if (isPaper) {
            if (itemType == Material.STONECUTTER && enableStoneCutter && (!usePermissions || player.hasPermission("ultimateinventory.stonecutter"))) {
                if (overrideStoneCutter) {
                    executeCommand(player, commandStoneCutter);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowStoneCutter(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.CARTOGRAPHY_TABLE && enableCartographyTable && (!usePermissions || player.hasPermission("ultimateinventory.cartographytable"))) {
                if (overrideCartographyTable) {
                    executeCommand(player, commandCartographyTable);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowCartographyTable(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.LOOM && enableLoom && (!usePermissions || player.hasPermission("ultimateinventory.loom"))) {
                if (overrideLoom) {
                    executeCommand(player, commandLoom);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowLoom(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.SMITHING_TABLE && enableSmithingTable && (!usePermissions || player.hasPermission("ultimateinventory.smithingtable"))) {
                if (overrideSmithingTable) {
                    executeCommand(player, commandSmithingTable);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowSmithingTable(player));
                }
                e.setCancelled(true);
            }

            if (itemType == Material.GRINDSTONE && enableGrindstone && (!usePermissions || player.hasPermission("ultimateinventory.grindstone"))) {
                if (overrideGrindstone) {
                    executeCommand(player, commandGrindstone);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowGrindstone(player));
                }
                e.setCancelled(true);
            }

            if (IsAnvil(itemType) && enableAnvil && (!usePermissions || player.hasPermission("ultimateinventory.anvil"))) {
                if (overrideAnvil) {
                    executeCommand(player, commandAnvil);
                } else {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ShowAnvil(player));
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventoryClose(InventoryCloseEvent e)
    {
        if (openShulkerBoxes.containsKey(e.getPlayer().getUniqueId()))
        {
            CloseShulkerbox(e.getPlayer());
        }
    }

    // Needs to close shulker box before items drop on death to avoid a duplication bug
    @EventHandler(priority = EventPriority.HIGHEST)
    public void Death(PlayerDeathEvent e)
    {
        Player player = e.getEntity();
        if (openShulkerBoxes.containsKey(player.getUniqueId()))
        {
            CloseShulkerbox(player);
        }
    }
}
