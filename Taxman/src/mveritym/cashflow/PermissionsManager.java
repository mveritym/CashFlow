package mveritym.cashflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/*import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
 import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;*/
import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.platymuus.bukkit.permissions.Group;
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
	Permission perm;
	PermissionManager pm;
	WorldPermissionsManager wpm;
	/* WorldDataHolder wdh; */
	PermissionSet permissionsSet;
	protected static CashFlow cashflow;
	protected Config conf;
	protected File confFile;
	String world;
	PluginManager pluginManager;
	Plugin plugin;
	PermissionsPlugin permsPlugin;

	public PermissionsManager(CashFlow cashflow) {
		PermissionsManager.cashflow = cashflow;
		conf = cashflow.getPluginConfig();
		this.world = conf.getString("world");
		this.setupPermissions();

		pluginManager = PermissionsManager.cashflow.getServer().getPluginManager();

		if (pluginManager.getPlugin("PermissionsBukkit") != null)
		{
			System.out.println("[" + PermissionsManager.cashflow.info.getName()
					+ "] Using PermissionsBukkit plugin.");
			pluginName = "PermissionsBukkit";
			plugin = pluginManager.getPlugin("PermissionsBukkit");
			permsPlugin = (PermissionsPlugin) plugin;
		}
		else if (PermissionsManager.cashflow.getServer().getPluginManager()
				.getPlugin("PermissionsEx") != null)
		{
			System.out.println("[" + PermissionsManager.cashflow.info.getName()
					+ "] Using PermissionsEx plugin.");
			pluginName = "PermissionsEx";
			pm = PermissionsEx.getPermissionManager();
			plugin = pluginManager.getPlugin("PermissionsEx");
		}
		else if (PermissionsManager.cashflow.getServer().getPluginManager()
				.getPlugin("bPermissions") != null)
		{
			System.out.println("[" + PermissionsManager.cashflow.info.getName()
					+ "] Using bPermissions plugin.");
			pluginName = "bPermissions";
			wpm = Permissions.getWorldPermissionsManager();
			permissionsSet = wpm.getPermissionSet(this.world);
			plugin = pluginManager.getPlugin("bPermissions");

			/*
			 * } else if
			 * (PermissionsManager.cashflow.getServer().getPluginManager
			 * ().getPlugin("GroupManager") != null) { System.out.println("[" +
			 * PermissionsManager.cashflow.info.getName() +
			 * "] Using GroupManager plugin."); pluginName = "GroupManager";
			 * plugin = pluginManager.getPlugin("GroupManager"); wdh =
			 * ((GroupManager) plugin).getWorldsHolder().getWorldData(world);
			 */
		}
		else
		{
			System.out.println("[" + PermissionsManager.cashflow.info.getName()
					+ "] No permissions plugin detected.");
		}
	}

	private void setupPermissions()
	{
		RegisteredServiceProvider<Permission> permissionProvider = PermissionsManager.cashflow.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if(permissionProvider != null)
        {
        	perm = permissionProvider.getProvider();
        }
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public boolean pluginDetected() {
		if (pluginName != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hasPermission(Player player, String node) {
		if (perm.has(player, node))
		{
			return true;
		}

		return false;
	}

	public boolean isGroup(String groupName) {
		String[] groups = perm.getGroups();
		for(int i = 0; i < groups.length; i++)
		{
			if(groups[i].equals(groupName))
			{
				return true;
			}
		}
		return false;
	}

	public List<String> getUsers(List<String> groups, List<String> players,
			List<String> exceptedPlayers) {
		List<String> playerList = new ArrayList<String>();

		if (pluginDetected())
		{
			if (pluginName.equals("PermissionsEx"))
			{
				for (String groupName : groups)
				{
					//Handle for default group
					if(groupName.equals(pm.getDefaultGroup().getName()))
					{
						//Grab all players
						List<String> allList = this.getAllPlayers();
						for(String name : allList)
						{
							if(!(playerList.contains(name)))
							{
								PermissionUser user = pm.getUser(name);
								for(Entry<String, PermissionGroup[]> e : user.getAllGroups().entrySet())
								{
									PermissionGroup[] g = e.getValue();
									if(g.length == 0)
									{
										//No groups, therefore they are in default
										playerList.add(user.getName());
									}
									else if(g.length >= 1)
									{
										//They have multiple groups, check to see if its their primary group
										if(g[0].getName().equals(groupName))
										{
											playerList.add(user.getName());
										}
									}
								}
							}
						}
					}
					else
					{
						PermissionUser[] userList = pm.getUsers(groupName);
						if (userList.length > 0)
						{
							for (PermissionUser pu : userList)
							{
								if (isPlayer(pu.getName())
										&& !(playerList.contains(pu.getName())))
								{
									playerList.add(pu.getName());
								}
							}
						}
					}
				}
			}
			else if (pluginName.equals("PermissionsBukkit"))
			{
				for (String groupName : groups)
				{
					if (isGroup(groupName))
					{
						Group group = permsPlugin.getGroup(groupName);
						List<String> groupPlayers = group.getPlayers();
						if (groupPlayers != null)
						{
							for (String player : groupPlayers)
							{
								if (!(playerList.contains(player)))
									playerList.add(player);
							}
						}
					}
				}
			}
			else if (pluginName.equals("bPermissions"))
			{
				for (String groupName : groups)
				{
					List<String> groupPlayers = getAllPlayers();
					for (String playerName : groupPlayers)
					{
						if (!(playerList.contains(playerName)))
						{
							List<String> groupNames = permissionsSet
									.getGroups(playerName);
							if (groupNames.contains(groupName))
							{
								playerList.add(playerName);
							}
						}
					}
				}
				/*
				 * } else if (pluginName.equals("GroupManager")) { List<String>
				 * groupPlayers = getAllPlayers(); for (String groupName:
				 * groups) { if (wdh == null) { break; } AnjoPermissionsHandler
				 * aph = wdh.getPermissionsHandler(); for (String playerName :
				 * groupPlayers) { if (!(playerList.contains(playerName)) &&
				 * aph.inGroup(playerName, groupName)) {
				 * playerList.add(playerName); } } }
				 */
			}
		}

		for (String player : players)
		{
			if (!(playerList.contains(player)) && isPlayer(player))
			{
				playerList.add(player);
			}
		}

		for (String player : exceptedPlayers)
		{
			playerList.remove(player);
		}

		return playerList;
	}

	public List<String> getAllPlayers() {
		String worldName = conf.getString("world");
		List<String> players = new ArrayList<String>();

		if (worldName == null)
		{
			worldName = "world";
		}
		World w = PermissionsManager.cashflow.getServer().getWorld(worldName);
		if(w != null)
		{
			File folder = new File(worldName + File.separator+ "players" + File.separator);

			for (File playerFile : folder.listFiles())
			{
				if(playerFile != null)
				{
					players.add(playerFile.getName().substring(0, playerFile.getName().length() - 4));
				}
			}
		}
		else
		{
			PermissionsManager.cashflow.log.warning("["
					+ PermissionsManager.cashflow.info.getName()
					+ "] " + worldName + " not found");
		}

		return players;
	}

	public boolean isPlayer(String playerName) {
		String worldName = conf.getString("world");

		if (worldName == null)
		{
			worldName = "world";
		}

		if (PermissionsManager.cashflow.getServer().getPlayer(playerName) != null)
		{
			return true;
		}
		else
		{
			try
			{
				@SuppressWarnings ("unused")
				FileInputStream test = new FileInputStream(worldName
						+ "/players/" + playerName + ".dat");
			}
			catch (FileNotFoundException e)
			{
				return false;
			}
		}
		return true;
	}

	public boolean setWorld(String worldName) {

		List<World> worlds = PermissionsManager.cashflow.getServer()
				.getWorlds();
		for (World world : worlds)
		{
			if (world.getName().equals(worldName))
			{
				conf.setProperty("world", worldName);
				PermissionsManager.cashflow.saveConfig();
				return true;
			}
		}

		return false;
	}
}
