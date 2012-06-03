package mveritym.cashflow.permissions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.Config;
import mveritym.cashflow.database.SQLibrary.Database.Query;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

// TODO Fix GroupManager inheritance?
public class PermissionsManager
{

	private static String pluginName = "null";
	private static Permission perm;
	private static PermissionManager pm;
	private static CashFlow cashflow;
	private static Config conf;
	private static Plugin plugin;
	private static PermissionsPlugin permsPlugin;
	private static final String empty = null;

	public static void init(CashFlow cf)
	{
		cashflow = cf;
		conf = cashflow.getPluginConfig();
		// Setup Vault for permissions
		RegisteredServiceProvider<Permission> permissionProvider = cashflow
				.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null)
		{
			perm = permissionProvider.getProvider();
			pluginName = perm.getName();

			final PluginManager pluginManager = cashflow.getServer()
					.getPluginManager();

			if (pluginName.equals("PermissionsBukkit"))
			{
				plugin = pluginManager.getPlugin("PermissionsBukkit");
				permsPlugin = (PermissionsPlugin) plugin;
			}
			else if (pluginName.equals("PermissionsEx"))
			{
				pm = PermissionsEx.getPermissionManager();
				plugin = pluginManager.getPlugin("PermissionsEx");
			}
			else if (pluginName.equals("bPermissions2"))
			{
				// Do we really need this at all? :\
				plugin = pluginManager.getPlugin("bPermissions");
			}

			else if (pluginName.equals("GroupManager"))
			{
				plugin = pluginManager.getPlugin("GroupManager");
				if (plugin == null)
				{
					cashflow.getLogger().severe(
							"Could not hook into GroupManager... D:");
				}
			}

		}
		else
		{
			cashflow.getLogger().warning("No permissions plugin detected.");
			cashflow.getServer().getPluginManager().disablePlugin(cashflow);
		}
	}

	public static boolean pluginDetected()
	{
		if (pluginName != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean hasPermission(Player player, PermissionNode node)
	{
		return hasPermission(player, node.getNode());
	}

	public static boolean hasPermission(Player player, String node)
	{
		return perm.has(player, node);
	}

	/**
	 * Determines if given group name is valid, known group
	 * 
	 * @param name
	 *            of group
	 * @return true if permissions has the group, else false
	 */
	public static boolean isGroup(String groupName)
	{
		String[] groups = perm.getGroups();
		for (int i = 0; i < groups.length; i++)
		{
			if (groups[i].equals(groupName))
			{
				return true;
			}
		}
		return false;
	}

	public static List<String> getPermissionsEXUsers(List<String> groups)
	{
		final List<String> playerList = new ArrayList<String>();
		for (String groupName : groups)
		{
			// Handle for default group
			if (groupName.equals(pm.getDefaultGroup().getName()))
			{
				// Grab all players
				final List<String> allList = getAllPlayers();
				for (String name : allList)
				{
					if (!(playerList.contains(name)))
					{
						// Player not in list
						final PermissionUser user = pm.getUser(name);
						for (final Entry<String, PermissionGroup[]> e : user
								.getAllGroups().entrySet())
						{
							final PermissionGroup[] g = e.getValue();
							if (g.length == 0)
							{
								// No groups, therefore they are in
								// default
								playerList.add(user.getName());
							}
							else if (g.length >= 1)
							{
								// They have multiple groups, check to
								// see if its their primary group
								if (g[0].getName().equals(groupName))
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
				final PermissionUser[] userList = pm.getUsers(groupName);
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
		return playerList;
	}

	/**
	 * Specific get users for PermissionsBukkit
	 * 
	 * @param List
	 *            of groups to include
	 * @return List of users
	 */
	public static List<String> getPermissionsBukkitUsers(List<String> groups)
	{
		final List<String> playerList = new ArrayList<String>();
		for (String groupName : groups)
		{
			// Handle default group
			if (groupName.equals("default"))
			{
				// Grab all players
				final List<String> allList = getAllPlayers();
				for (String name : allList)
				{
					if (!(playerList.contains(name)))
					{
						// Player not in list
						final List<Group> userGroups = permsPlugin
								.getGroups(name);
						if (userGroups.size() == 0)
						{
							// No groups, therefore they are in
							// default
							playerList.add(name);
						}
						else if (userGroups.size() >= 1)
						{
							// TODO check that this is actually correct
							// I have no idea if, when getting a user's groups
							// that it includes inherited groups or not.
							// If it does include inherited groups, rather than
							// explicit groups
							// Then this will almost always be true, depending
							// on ordering
							if (userGroups.get(0).getName().equals("default"))
							{
								// Their first group is default
								playerList.add(name);
							}
						}
					}
				}
			}
			else
			{
				try
				{
					final Group group = permsPlugin.getGroup(groupName);
					final List<String> groupPlayers = group.getPlayers();
					if (groupPlayers != null)
					{
						for (String player : groupPlayers)
						{
							if (!(playerList.contains(player)))
								playerList.add(player);
						}
					}
				}
				catch (NullPointerException e)
				{
					cashflow.getLogger().severe(
							"PermissionsBukkit gave a null error for group: "
									+ groupName);
					cashflow.getLogger()
							.severe("Cashflow will ignore this error. Tax/Salary will not go to players of that group.");
					cashflow.getLogger().severe(
							"Resultant exception: " + e.getMessage());
				}
			}
		}
		return playerList;
	}

	public static List<String> getbPermissionsUsers(List<String> groups)
	{
		final List<String> playerList = new ArrayList<String>();
		for (String groupName : groups)
		{
			final List<String> groupPlayers = getAllPlayers();
			for (String playerName : groupPlayers)
			{
				if (!(playerList.contains(playerName)))
				{
					if (perm.playerInGroup(empty, playerName, groupName))
					{
						playerList.add(playerName);
					}
				}
			}
		}
		return playerList;
	}

	public static List<String> getGroupManagerUsers(List<String> groups)
	{
		final List<String> playerList = new ArrayList<String>();
		final List<String> groupPlayers = getAllPlayers();
		for (final String groupName : groups)
		{
			for (final String playerName : groupPlayers)
			{
				if (!(playerList.contains(playerName)))
				{
					if (perm.playerInGroup(empty, playerName, groupName))
					{
						playerList.add(playerName);
					}
				}
			}
		}
		return playerList;
	}

	public static List<String> getUsers(List<String> groups, List<String> players,
			List<String> exceptedPlayers)
	{
		List<String> playerList = new ArrayList<String>();

		if (pluginDetected())
		{
			if (pluginName.equals("PermissionsEx"))
			{
				playerList = getPermissionsEXUsers(groups);
			}
			else if (pluginName.equals("PermissionsBukkit"))
			{
				playerList = getPermissionsBukkitUsers(groups);
			}
			else if (pluginName.equals("bPermissions")
					|| pluginName.equals("bPermissions2"))
			{
				playerList = getbPermissionsUsers(groups);
			}

			else if (pluginName.equals("GroupManager"))
			{
				playerList = getGroupManagerUsers(groups);
			}

		}

		for (final String player : players)
		{
			if (!(playerList.contains(player)) && isPlayer(player))
			{
				playerList.add(player);
			}
		}

		for (final String player : exceptedPlayers)
		{
			playerList.remove(player);
		}

		return playerList;
	}

	public static List<String> getAllPlayers()
	{
		final List<String> players = new ArrayList<String>();
		try
		{
			final String query = "SELECT * FROM "
					+ cashflow.getPluginConfig().tablePrefix + "cashflow;";
			final Query rs = cashflow.getDatabaseHandler().select(query);
			if (rs.getResult().next())
			{
				do
				{
					players.add(rs.getResult().getString("playername"));
				} while (rs.getResult().next());
			}
			rs.closeQuery();
		}
		catch (SQLException e)
		{
			cashflow.getLogger().warning(cashflow.prefix + " SQL Exception");
			e.printStackTrace();
		}
		return players;
	}

	public static boolean isPlayer(String playerName)
	{
		String worldName = conf.getString("world");
		boolean has = false;
		if (worldName == null)
		{
			worldName = "world";
		}

		if (cashflow.getServer().getPlayer(playerName) != null)
		{
			has = true;
		}
		else
		{
			try
			{
				final String query = "SELECT * FROM "
						+ cashflow.getPluginConfig().tablePrefix
						+ "cashflow WHERE playername='" + playerName + "';";
				final Query rs = cashflow.getDatabaseHandler().select(query);
				if (rs.getResult().next())
				{
					has = true;
				}
				else
				{
					has = false;
				}
				rs.closeQuery();
			}
			catch (SQLException e)
			{
				cashflow.getLogger()
						.warning(cashflow.prefix + " SQL Exception");
				e.printStackTrace();
			}
		}
		return has;
	}

	public static boolean setWorld(String worldName)
	{

		final List<World> worlds = cashflow.getServer().getWorlds();
		for (World world : worlds)
		{
			if (world.getName().equals(worldName))
			{
				conf.setProperty("world", worldName);
				cashflow.saveConfig();
				return true;
			}
		}

		return false;
	}

	
}
