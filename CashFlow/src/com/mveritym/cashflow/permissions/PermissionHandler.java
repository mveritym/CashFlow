package com.mveritym.cashflow.permissions;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Class to handle permission node checks.
 * 
 * @author Mitsugaru
 * 
 */
public class PermissionHandler {
   private static Permission perm;
   private static boolean hasVault;

   public static void init(final JavaPlugin basePlugin) {
      if(basePlugin.getServer().getPluginManager().getPlugin("Vault") != null) {
         hasVault = true;
         final RegisteredServiceProvider<Permission> permissionProvider = basePlugin
               .getServer().getServicesManager()
               .getRegistration(Permission.class);
         if(permissionProvider != null) {
            perm = permissionProvider.getProvider();
         }
      } else {
         hasVault = false;
      }
   }

   public static boolean has(final CommandSender sender, PermissionNode node) {
      return has(sender, node.getNode());
   }

   /**
    * 
    * @param CommandSender
    *           that sent command
    * @param PermissionNode
    *           node to check, as String
    * @return true if sender has the node, else false
    */
   public static boolean has(final CommandSender sender, final String node) {
      // Use vault if we have it
      if(hasVault && perm != null) {
         return perm.has(sender, node);
      }
      // Attempt to use SuperPerms or op
      if(sender.isOp() || sender.hasPermission(node)) {
         return true;
      }
      // TODO config option for op allow everything.
      // Else, they don't have permission
      return false;
   }
}
