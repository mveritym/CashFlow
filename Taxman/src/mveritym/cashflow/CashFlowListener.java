package mveritym.cashflow;

import java.sql.SQLException;

import mveritym.cashflow.database.SQLibrary.Database.Query;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
//import org.bukkit.event.player.PlayerQuitEvent;

public class CashFlowListener implements Listener {
	// Class variables
	private final CashFlow plugin;
	private final Config config;

	public CashFlowListener(CashFlow plugin) {
		// Instantiate variables
		this.plugin = plugin;
		config = plugin.getPluginConfig();
	}

	//TODO implement at some future point
	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		// Update last on time for player and to check it
		cf.getDatabaseHandler().standardQuery(
				"UPDATE " + config.tablePrefix + "cashflow SET laston='"
						+ System.currentTimeMillis()
						+ "', check='1' WHERE playername='"
						+ event.getPlayer().getName() + "';");
	}*/

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (config.debug) {
			plugin.getLogger().warning(plugin.prefix + " PlayerJoin event");
		}
		String query = "SELECT COUNT(*) FROM " + config.tablePrefix
				+ "cashflow WHERE playername='" + event.getPlayer().getName()
				+ "';";
		Query rs = plugin.getDatabaseHandler().select(query);
		try {
			boolean has = false;
			if (rs.getResult().next()) {
				if (rs.getResult().getInt(1) >= 1) {
					// They're already in the database
					has = true;
				}
			}
			rs.closeQuery();
			if (!has) {
				if (config.debug) {
					plugin.getLogger().warning(plugin.prefix + " PlayerJoin - add new player");
				}
				// Add to master list
				query = "INSERT INTO " + config.tablePrefix
						+ "cashflow (playername) VALUES('"
						+ event.getPlayer().getName() + "');";
				plugin.getDatabaseHandler().standardQuery(query);
			}
		} catch (SQLException e) {
			plugin.getLogger().warning(plugin.prefix + " SQL Exception");
			e.printStackTrace();
		}
	}
}
