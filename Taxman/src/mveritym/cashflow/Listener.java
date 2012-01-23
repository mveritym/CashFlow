package mveritym.cashflow;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listener extends PlayerListener {
	// Class variables
	private final CashFlow cf;
	private final Config config;

	public Listener(CashFlow plugin) {
		// Instantiate variables
		cf = plugin;
		config = cf.getPluginConfig();
	}
	@Override
	public void onPlayerQuit(final PlayerQuitEvent event)
    {
		//Update last on time for player and to check it
		cf.getDatabaseHandler().standardQuery("UPDATE " + config.tablePrefix
					+ "cashflow SET laston='" + System.currentTimeMillis() + "', check='1' WHERE playername='" + event.getPlayer().getName() + "';");
    }

	@Override
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (config.debug)
		{
			cf.log.warning(cf.prefix + " PlayerJoin event");
		}
		String query = "SELECT COUNT(*) FROM " + config.tablePrefix
					+ "cashflow WHERE playername='"
				+ event.getPlayer().getName() + "';";
		ResultSet rs = cf.getDatabaseHandler().select(query);
		try
		{
			boolean has = false;
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
				if (config.debug)
				{
					cf.log.warning(cf.prefix + " PlayerJoin - add new player");
				}
				// Add to master list
				query = "INSERT INTO " + config.tablePrefix
					+ "cashflow (playername) VALUES('"
						+ event.getPlayer().getName() + "');";
				cf.getDatabaseHandler().standardQuery(query);
			}
		}
		catch (SQLException e)
		{
			cf.log.warning(cf.prefix + " SQL Exception");
			e.printStackTrace();
		}
	}
}
