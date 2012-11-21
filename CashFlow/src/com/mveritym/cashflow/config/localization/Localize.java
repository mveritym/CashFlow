package com.mveritym.cashflow.config.localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.mveritym.cashflow.config.ModularConfig;

public class Localize extends ModularConfig {
   // Class variables
   private File file;
   private YamlConfiguration config;

   public Localize(JavaPlugin jplugin) {
      super(jplugin);
      file = new File(plugin.getDataFolder().getAbsolutePath()
            + "/localization.yml");
      config = YamlConfiguration.loadConfiguration(file);
      loadDefaults(config);
      loadSettings(config);
   }

   /**
    * Save the localization config.
    */
   @Override
   public void save() {
      try {
         // Save the file
         config.save(file);
      } catch(IOException e1) {
         plugin.getLogger().warning(
               "File I/O Exception on saving localization config");
         e1.printStackTrace();
      }
   }

   /**
    * Reload the localization config.
    */
   @Override
   public void reload() {
      try {
         config.load(file);
      } catch(FileNotFoundException e) {
         e.printStackTrace();
      } catch(IOException e) {
         e.printStackTrace();
      } catch(InvalidConfigurationException e) {
         e.printStackTrace();
      }
      loadSettings(config);
   }

   @Override
   public void set(String path, Object o) {
      config.set(path, o);
      save();
   }

   @Override
   public void loadSettings(final ConfigurationSection config) {
      for(LocalizeNode node : LocalizeNode.values()) {
         OPTIONS.put(node,
               config.getString(node.getPath(), node.getDefaultValue()));
      }
   }

   /**
    * Parse a localization string and replace the flags from the given map.
    * 
    * @param node
    *           - LocalizeNode for the string to use.
    * @param replace
    *           - Replacement strings for appropriate flags.
    * @return Formatted, color coded localization string.
    */
   public String parseString(LocalizeNode node, EnumMap<Flag, String> replace) {
      /**
       * Thanks to @Njol for the following
       * http://forums.bukkit.org/threads/multiple
       * -classes-config-colours.79719/#post-1154761
       */
      String out = ChatColor.translateAlternateColorCodes('&', getString(node));
      if(replace != null) {
         for(final Entry<Flag, String> entry : replace.entrySet()) {
            out = out.replaceAll(entry.getKey().getFlag(), entry.getValue());
         }
      }
      return out;
   }

   @Override
   public void boundsCheck() {
      // Ignore
   }

   @Override
   public void loadDefaults(ConfigurationSection config) {
      for(final LocalizeNode node : LocalizeNode.values()) {
         if(!config.contains(node.getPath())) {
            config.set(node.getPath(), node.getDefaultValue());
         }
      }
      save();
   }
}
