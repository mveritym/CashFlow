package mveritym.cashflow;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.platymuus.bukkit.permissions.PermissionsPlugin;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.interfaces.PermissionSet;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager {

	String pluginName;
	PermissionManager pm;
	WorldPermissionsManager wpm;
	PermissionSet permissionsSet;
	protected static CashFlow cashflow;
	String world;
	PluginManager pluginManager;
	Plugin plugin;
	PermissionsPlugin permsPlugin;
	
	public PermissionsManager(CashFlow cashflow, String world) {
		PermissionsManager.cashflow = cashflow;
		this.world = world;
		pluginManager = PermissionsManager.cashflow.getServer().getPluginManager();
		
		if(pluginManager.getPlugin("PermissionsBukkit") != null) {
			System.out.println("PermissionsBukkit is not supported at this time.");
			/*
			System.out.println("Using PermissionsBukkit plugin.");
			pluginName = "PermissionsBukkit";
			permsPlugin = new PermissionsPlugin();
			plugin = pluginManager.getPlugin("PermissionsBukkit");
			*/
		} else if(PermissionsManager.cashflow.getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
			System.out.println("Using PermissionsEx plugin.");
			pluginName = "PermissionsEx";
			pm = PermissionsEx.getPermissionManager();
			plugin = pluginManager.getPlugin("PermissionsEx");
		} else if(PermissionsManager.cashflow.getServer().getPluginManager().getPlugin("bPermissions") != null) {
			System.out.println("Using bPermissions plugin.");
			pluginName = "bPermissions";
			wpm = Permissions.getWorldPermissionsManager();
			permissionsSet = wpm.getPermissionSet(world);
			plugin = pluginManager.getPlugin("bPermissions");
		} else {
			System.out.println("No permissions plugin detected.");
		}
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public boolean pluginDetected() {
		if(pluginName != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasPermission(Player player, String node) {
		if(pluginName.equals("PermissionsEx")) {
            if(pm.has(player, node)){
            	return true;
            } else {
            	return false;
            }
        } else if(pluginName.equals("PermissionsBukkit")) {
           if(player.hasPermission(node)) {
        	   return true;
           } else {
        	   return false;
           }
        } else if(pluginName.equals("bPermissions")) {
        	if(player.hasPermission(node)) {
        		return true;
        	} else {
        		return false;
        	}
        }
		return false;
	}
	
	public boolean isGroup(String groupName) {
		if(pluginName.equals("PermissionsEx")) {
			if(pm.getGroup(groupName) != null) {
				return true;
			} else {
				return false;
			}
		} else if(pluginName.equals("PermissionsBukkit")) {
			if(permsPlugin.getGroup(groupName) != null) {
				return true;
			} else {
				return false;
			}
		} else if(pluginName.equals("bPermissions")) {
			if(permissionsSet.getGroupNodes(groupName) != null) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
}
