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

import com.platymuus.bukkit.permissions.PermissionsPlugin;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.interfaces.PermissionSet;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager {

	String pluginName = "null";
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
			System.out.println("[" + PermissionsManager.cashflow.info.getName() + "] PermissionsBukkit is not supported at this time.");
			/*
			System.out.println("[" + PermissionsManager.cashflow.info.getName() + "] Using PermissionsBukkit plugin.");
			pluginName = "PermissionsBukkit";
			plugin = pluginManager.getPlugin("PermissionsBukkit");
			*/
		} else if(PermissionsManager.cashflow.getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
			System.out.println("[" + PermissionsManager.cashflow.info.getName() + "] Using PermissionsEx plugin.");
			pluginName = "PermissionsEx";
			pm = PermissionsEx.getPermissionManager();
			plugin = pluginManager.getPlugin("PermissionsEx");
		} else if(PermissionsManager.cashflow.getServer().getPluginManager().getPlugin("bPermissions") != null) {
			System.out.println("[" + PermissionsManager.cashflow.info.getName() + "] Using bPermissions plugin.");
			pluginName = "bPermissions";
			wpm = Permissions.getWorldPermissionsManager();
			permissionsSet = wpm.getPermissionSet(world);
			plugin = pluginManager.getPlugin("bPermissions");
			
		} else {
			System.out.println("[" + PermissionsManager.cashflow.info.getName() + "] No permissions plugin detected.");
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
			PermissionGroup[] groups = pm.getGroups();
			for(PermissionGroup group : groups) {
				if(group.getName().equals(groupName)) {
					return true;
				}
			}
			return false;
		} /*else if(pluginName.equals("PermissionsBukkit")) {
			return true;
			if(permsPlugin.getGroup(groupName) != null) {
				return true;
			} else {
				return false;
			}
		} */else if(pluginName.equals("bPermissions")) {
			if(permissionsSet.getGroupNodes(groupName).size() != 0) {
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}

	public List<String> getUsers(List<String> groups, List<String> players, List<String> exceptedPlayers) {
		List<String> playerList = new ArrayList<String>();
		loadConf();
		Boolean onlineOnly = conf.getBoolean("onlineOnly", false);
		
		if(pluginDetected()) {
			if(pluginName.equals("PermissionsEx")) {
				for(String groupName : groups) {
					PermissionUser[] userList = pm.getUsers(groupName);
					if(userList.length > 0) {
						for(PermissionUser pu : userList) {
							if(isPlayer(pu.getName())) { 
								playerList.add(pu.getName());
							}
						}
					}
				}
			} /*else if(pluginName.equals("PermissionsBukkit")) {
				for(String groupName : groups) {
					//Group group = permsPlugin.getGroup(groupName);
					System.out.println(PermissionsPlugin.getAllGroups() == null);
					System.out.println("GROUPS: " + permsPlugin.getAllGroups());
					
					for(Group group : permsPlugin.getAllGroups()) {
						System.out.println(group);
					}
					
					List<String> players = group.getPlayers();
					
					for(String player : players) {
						System.out.println(player);
						playerList.add(player);
					}
				}
			} */else if(pluginName.equals("bPermissions")) {
				for(String groupName : groups) {
					List<String> groupPlayers = getAllPlayers();
					for(String playerName : groupPlayers) {
						List<String> groupNames = permissionsSet.getGroups(playerName);
						if(groupNames.contains(groupName)) {
							playerList.add(playerName);
						}
					}
				}
			}
		}
		
		for(String player : players) {
			if(!(playerList.contains(player)) && isPlayer(player)) {
				playerList.add(player);
			}
		}
		
		for(String player : exceptedPlayers) {
			playerList.remove(player);
		}
		
		if(onlineOnly) {
			List<String> tempPlayerList = new ArrayList<String>();			
			for(String player : playerList) {
				if(PermissionsManager.cashflow.getServer().getPlayer(player) != null) {
					tempPlayerList.add(player);
				}
			}
			playerList = tempPlayerList;
		}
		
		return playerList;
	}
	
	public List<String> getAllPlayers() {
		loadConf();
		String worldName = conf.getString("world");
		List<String> players = new ArrayList<String>();
		
		if(worldName == null) {
			worldName = "world";
		}
		
		File folder = new File(worldName + "/players");
	    File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  String fileName = listOfFiles[i].getName();
	    	  String playerName = fileName.substring(0, fileName.length() - 4);
	    	  players.add(playerName);
	      }
	    }
	    
	    return players;
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
	
	public void setOnlineOnly(Boolean online) {
		loadConf();
		conf.setProperty("onlineOnly", online);
		conf.save();
		return;
	}
	
	public void loadConf() {
    	File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");

        if (f.exists()) {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	System.out.println("[" + PermissionsManager.cashflow.info.getName() + "] No CashFlow config file found. Creating config file.");
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);  
            List<String> tempList = null;
            conf.setProperty("taxes.list", tempList);
            conf.save();
        }
    }	
}
