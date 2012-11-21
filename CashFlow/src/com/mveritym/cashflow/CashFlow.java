package com.mveritym.cashflow;

import org.bukkit.plugin.java.JavaPlugin;

import com.mveritym.cashflow.config.RootConfig;
import com.mveritym.cashflow.config.localization.Localize;

public class CashFlow extends JavaPlugin {
   /**
    * Plugin tag.
    */
   public static final String TAG = "[CF]";
   /**
    * Economy found flag.
    */
   private boolean economyFound = false;

   @Override
   public void onEnable() {
      // TODO initialize localization
      Localize local = new Localize(this);
      // TODO initialize permissions
      // TODO initialize economy
      // TODO initialize config
      RootConfig rootConfig = new RootConfig(this);
      // TODO initialize database
      // TODO initialize updater
      // TODO register listeners
      // TODO command executors

      // TODO enable taxes/salaries
      // TODO last paid
   }

   @Override
   public void onDisable() {
      // Save config
      this.reloadConfig();
      this.saveConfig();
      // Disable and stop stuff
      if(economyFound) {

      }
      // TODO Disconnect from database
   }
}
