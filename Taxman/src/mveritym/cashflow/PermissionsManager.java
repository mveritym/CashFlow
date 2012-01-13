package mveritym.cashflow;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/*import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;*/
import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
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

//TODO Fix GroupManager
public class PermissionsManager {

	private String pluginName = "null";
	private Permission perm;
	private PermissionManager pm;
	private WorldPermissionsManager wpm;
	/*private WorldDataHolder wdh;*/
	private PermissionSet permissionsSet;
	protected CashFlow cashflow;
	protected Config conf;
	protected File confFile;
	private String world;
	private Plugin plugin;
	private PermissionsPlugin permsPlugin;

	public PermissionsManager(CashFlow cashflow) {
		this.cashflow = cashflow;
		conf = cashflow.getPluginConfig();
		this.world = conf.getString("world");
		// Setup Vault for permissions
		RegisteredServiceProvider<Permission> permissionProvider = this.cashflow
				.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null)
		{
			perm = permissionProvider.getProvider();
			pluginName = perm.getName();

			final PluginManager pluginManager = this.cashflow.getServer()
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
			else if (pluginName.equals("bPermissions"))
			{
				wpm = Permissions.getWorldPermissionsManager();
				permissionsSet = wpm.getPermissionSet(this.world);
				plugin = pluginManager.getPlugin("bPermissions");

			}
			/*else if (pluginName.equals("GroupManager"))
			{
				plugin = pluginManager.getPlugin("GroupManager");
				wdh = ((GroupManager) plugin).getWorldsHolder().getWorldData(
						world);
			}*/
		}
		else
		{
			System.out.println(this.cashflow.prefix
					+ " No permissions plugin detected.");
			this.cashflow.getServer().getPluginManager().disablePlugin(cashflow);
		}
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
		// Pex specific supercedes vault
		if (pluginName.equals("PermissionsEx"))
		{
			final PermissionManager permissions = PermissionsEx
					.getPermissionManager();
			// Handle pex check
			if (permissions.has(player, node))
			{
				return true;
			}
		}
		if (perm.has(player, node))
		{
			return true;
		}
		return false;
	}

	/**
	 * Determines if given group name is valid, known group
	 *
	 * @param name
	 *            of group
	 * @return true if permissions has the group, else false
	 */
	public boolean isGroup(String groupName) {
		//bPermissions compatibility
		if(perm.getName().equals("bPermissions"))
		{
			for(PermissionSet p : wpm.getPermissionSets())
			{
				if(!p.getGroupNodes(groupName).isEmpty())
				{
					return true;
				}
			}
		}
		else
		{
			String[] groups = perm.getGroups();
			for (int i = 0; i < groups.length; i++)
			{
				if (groups[i].equals(groupName))
				{
					return true;
				}
			}
		}
		return false;
	}

	public List<String> getPermissionsEXUsers(List<String> groups) {
		final List<String> playerList = new ArrayList<String>();
		for (String groupName : groups)
		{
			// Handle for default group
			if (groupName.equals(pm.getDefaultGroup().getName()))
			{
				// Grab all players
				final List<String> allList = this.getAllPlayers();
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
	public List<String> getPermissionsBukkitUsers(List<String> groups) {
		final List<String> playerList = new ArrayList<String>();
		for (String groupName : groups)
		{
			// Handle default group
			if (groupName.equals("default"))
			{
				// Grab all players
				final List<String> allList = this.getAllPlayers();
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
		}
		return playerList;
	}

	public List<String> getbPermissionsUsers(List<String> groups) {
		final List<String> playerList = new ArrayList<String>();
		for (String groupName : groups)
		{
			final List<String> groupPlayers = getAllPlayers();
			for (String playerName : groupPlayers)
			{
				if (!(playerList.contains(playerName)))
				{
					final List<String> groupNames = permissionsSet
							.getGroups(playerName);
					if (groupNames.contains(groupName))
					{
						playerList.add(playerName);
					}
				}
			}
		}
		return playerList;
	}

	/*public List<String> getGroupManagerUsers(List<String> groups) {
		final List<String> playerList = new ArrayList<String>();
		final List<String> groupPlayers = getAllPlayers();
		for (final String groupName : groups)
		{
			if (wdh == null)
			{
				break;
			}
			final AnjoPermissionsHandler aph = wdh.getPermissionsHandler();
			for (final String playerName : groupPlayers)
			{
				if (!(playerList.contains(playerName))
						&& aph.inGroup(playerName, groupName))
				{
					playerList.add(playerName);
				}
			}
		}
		return playerList;
	}*/

	public List<String> getUsers(List<String> groups, List<String> players,
			List<String> exceptedPlayers) {
		List<String> playerList = new ArrayList<String>();

		if (pluginDetected())
		{
			if (pluginName.equals("PermissionsEx"))
			{
				playerList = this.getPermissionsEXUsers(groups);
			}
			else if (pluginName.equals("PermissionsBukkit"))
			{
				playerList = this.getPermissionsBukkitUsers(groups);
			}
			else if (pluginName.equals("bPermissions"))
			{
				playerList = this.getbPermissionsUsers(groups);
			}
			/*else if (pluginName.equals("GroupManager"))
			{
				playerList = this.getGroupManagerUsers(groups);
			}*/
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

	public List<String> getAllPlayers() {
		final List<String> players = new ArrayList<String>();
		try
		{
			final String query = "SELECT * FROM 'cashflow'";
			final ResultSet rs = this.cashflow.getLiteDB().select(query);
			if (rs.next())
			{
				do
				{
					players.add(rs.getString("playername"));
				}
				while (rs.next());
			}
		}
		catch (SQLException e)
		{
			this.cashflow.log.warning(this.cashflow.prefix + " SQL Exception");
			e.printStackTrace();
		}
		return players;
	}

	public boolean isPlayer(String playerName) {
		String worldName = conf.getString("world");
		boolean has = false;
		if (worldName == null)
		{
			worldName = "world";
		}

		if (this.cashflow.getServer().getPlayer(playerName) != null)
		{
			has = true;
		}
		else
		{
			try
			{
				final String query = "SELECT * FROM 'cashflow' WHERE playername='"
						+ playerName + "';";
				final ResultSet rs = this.cashflow.getLiteDB().select(query);
				if (rs.next())
				{
					has = true;
				}
				else
				{
					has = false;
				}
				rs.close();
			}
			catch (SQLException e)
			{
				this.cashflow.log.warning(this.cashflow.prefix
						+ " SQL Exception");
				e.printStackTrace();
			}
		}
		return has;
	}

	public boolean setWorld(String worldName) {

		final List<World> worlds = this.cashflow.getServer().getWorlds();
		for (World world : worlds)
		{
			if (world.getName().equals(worldName))
			{
				conf.setProperty("world", worldName);
				this.cashflow.saveConfig();
				return true;
			}
		}

		return false;
	}

	public void importPlayers(CommandSender sender, String worldName) {
		this.cashflow.getServer().getScheduler().scheduleAsyncDelayedTask(cashflow, new ImportPlayersTask(sender, worldName));
	}

	class ImportPlayersTask implements Runnable
	{
		private String worldName;
		private CommandSender sender;

		public ImportPlayersTask(CommandSender sender, String world)
		{
			worldName = world;
			this.sender = sender;
		}

		@Override
		public void run() {
			final File folder = new File(worldName + File.separator + "players"
					+ File.separator);
			for (final File playerFile : folder.listFiles())
			{
				if (playerFile != null)
				{
					final String name = playerFile.getName().substring(0,
							playerFile.getName().length() - 4);
					try
					{
						boolean has = false;
						// Check if player already exists
						String query = "SELECT COUNT(*) FROM 'cashflow' WHERE playername='"
								+ name + "';";
						final ResultSet rs = cashflow.getLiteDB()
								.select(query);
						if (rs.next())
						{
							if (rs.getInt(1) >= 1)
							{
								// They're already in the database
								has = true;
							}
						}
						rs.close();
						if (!has)
						{
							// Add to master list
							query = "INSERT INTO 'cashflow' VALUES('" + name
									+ "');";
							cashflow.getLiteDB().standardQuery(query);
						}
					}
					catch (SQLException e)
					{
						cashflow.log.warning(cashflow.prefix
								+ " SQL Exception");
						e.printStackTrace();
					}
				}
			}
			sender.sendMessage(cashflow.prefix
					+ " Done importing players from " + worldName + " into database");
			cashflow.log.info(cashflow.prefix
					+ " Done importing players from " + worldName + " into database");
		}
	}
}
