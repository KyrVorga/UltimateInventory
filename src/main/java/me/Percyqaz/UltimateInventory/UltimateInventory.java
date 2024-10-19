package me.Percyqaz.UltimateInventory;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateInventory extends JavaPlugin {

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {

        boolean isPaper = false;
        try
        {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            isPaper = true;
            this.getLogger().info("You are running PaperMC, some extra features are enabled");
        }
        catch (ClassNotFoundException e)
        {
            //https://www.spigotmc.org/threads/quick-question-about-posting-resources.394544/#post-3543896
            this.getLogger().info("You are not running PaperMC");
        }

        PluginManager pm = getServer().getPluginManager();

        addItemConfig("shulkerbox", true, false, "");
        addItemConfig("enderChest", true, false, "");
        addItemConfig("craftingTable", true, false, "");
        if (isPaper) {
            addItemConfig("smithingTable", true, false, "");
            addItemConfig("stoneCutter", true, false, "");
            addItemConfig("grindstone", true, false, "");
            addItemConfig("cartographyTable", true, false, "");
            addItemConfig("loom", true, false, "");
            addItemConfig("anvil", false, false, "");
        }
        config.addDefault("usePermissions", false);

        config.options().copyDefaults(true);
        saveConfig();

        pm.registerEvents(new InventoryListener(this, config, isPaper), this);
        if (pm.getPlugin("ChestSort") != null) {
            pm.registerEvents(new ChestSortListener(this), this);
            this.getLogger().info("ChestSort detected, enabling compatibility support");
        }
    }

    private void addItemConfig(String itemName, boolean enable, boolean override, String command) {
        config.addDefault(itemName + ".enable", enable);
        config.addDefault(itemName + ".override", override);
        config.addDefault(itemName + ".command", command);
    }
}
