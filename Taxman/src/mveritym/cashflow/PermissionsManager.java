package mveritym.cashflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

//import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

//import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.interfaces.PermissionSet;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager {

	String pluginName;
	PermissionManager pm;
	WorldPermissionsManager wpm;
	PermissionSet permissionsSet;
	protected static CashFlow cashflow;
	protected static Configuration conf;
	protected File confFile;
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
			System.out.println("bPermissions is not supported at this time.");
			/*
			System.out.println("Using bPermissions plugin.");
			pluginName = "bPermissions";
			wpm = Permissions.getWorldPermissionsManager();
			permissionsSet = wpm.getPermissionSet(world);
			plugin = pluginManager.getPlugin("bPermissions");
			*/
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
        } 
		/*
		else if(pluginName.equals("PermissionsBukkit")) {
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
        */
		return false;
	}
	
	public boolean isGroup(String groupName) {
		if(pluginName.equals("PermissionsEx")) {
			if(pm.getGroup(groupName) != null) {
				return true;
			} else {
				return false;
			}
		} 
		/*
		else if(pluginName.equals("PermissionsBukkit")) {
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
		*/
		return false;
	}

	public List<String> getUsers(List<String> groups, List<String> exceptedPlayers) {
		List<String> playerList = new ArrayList<String>();
		
		if(pluginName.equals("PermissionsEx")) {
			for(String groupName : groups) {
				PermissionUser[] userList = pm.getUsers(groupName);
				if(userList.length > 0) {
					for(PermissionUser pu : userList) {
						playerList.add(pu.getName());
					}
				}
			}
		}
		/*
		else if(pluginName.equals("PermissionsBukkit")) {
			for(String groupName : groups) {
				for(Group group : permsPlugin.getAllGroups()) {
					if(group.getName().equals(groupName)) {
						for(String player : group.getPlayers()) {
							playerList.add((Player) player);
						}
					}
				}
			}
		} else if(pluginName.equals("bPermissions")) {
			for(String groupName : groups) {
				
			}
		}
		*/
		
		for(String player : exceptedPlayers) {
			playerList.remove(player);
		}
		
		return playerList;
	}
	
	@SuppressWarnings("unused")
	public boolean isPlayer(String playerName) {
		loadConf();
		String worldName = conf.getString("world");
		
		if(worldName == null) {
			worldName = "world";
		}
		
		if(PermissionsManager.cashflow.getServer().getPlayer(playerName) != null) {
			return true;
		} else {
			try {
				FileInputStream test = new FileInputStream(worldName + "/players/" + playerName + ".dat");
			} catch (FileNotFoundException e) {
				return false;
			}
		}
		return true;
	}
	
	public boolean setWorld(String worldName) {
		loadConf();
		
		List<World> worlds = PermissionsManager.cashflow.getServer().getWorlds();
		for(World world : worlds) {
			if(world.getName().equals(worldName)) {
				conf.setProperty("world", worldName);
				conf.save();
				return true;
			}
		}
		
		return false;
	}
	
	public void loadConf() {
    	File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");

        if (f.exists()) {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	System.out.println("No CashFlow config file found. Creating config file.");
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);  
            List<String> tempList = null;
            conf.setProperty("taxes.list", tempList);
            conf.save();
        }
    }
	
}
