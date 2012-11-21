package com.mveritym.cashflow.config;

import org.bukkit.configuration.ConfigurationSection;

import com.mveritym.cashflow.CashFlow;

/**
 * Handler for the main config.yml configuration file.
 * 
 * @author Mitsugaru
 * 
 */
public class RootConfig extends ModularConfig {

   public RootConfig(CashFlow cf) {
      super(cf);
      final ConfigurationSection config = cf.getConfig();
      loadDefaults(config);
      // TODO database info
      // Reload other settings
      reload();
   }

   @Override
   public void set(final String path, final Object o) {
      final ConfigurationSection config = plugin.getConfig();
      config.set(path, o);
      plugin.saveConfig();
   }

   /**
    * Reloads info from yaml file(s)
    */
   @Override
   public void reload() {
      // Initial reload
      plugin.reloadConfig();
      // Grab config
      loadSettings(plugin.getConfig());
      // Check bounds
      boundsCheck();
   }

   @Override
   public void loadSettings(final ConfigurationSection config) {
      // TODO Auto-generated method stub

   }

   @Override
   public void boundsCheck() {
      // TODO Auto-generated method stub

   }

   @Override
   public void save() {
      plugin.saveConfig();
   }

   @Override
   public void loadDefaults(ConfigurationSection config) {
      for(final RootConfigNode node : RootConfigNode.values()) {
         if(!config.contains(node.getPath())) {
            config.set(node.getPath(), node.getDefaultValue());
         }
      }
      save();
   }

}
