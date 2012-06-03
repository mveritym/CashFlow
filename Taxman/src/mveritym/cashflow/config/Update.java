package mveritym.cashflow.config;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.database.Table;

public class Update
{
	private static CashFlow plugin;

	public static void init(CashFlow cf)
	{
		plugin = cf;
	}

	/**
	 * Check if updates are necessary
	 */
	public static void checkUpdate()
	{
		// Check if need to update
		ConfigurationSection config = plugin.getConfig();
		if (Double.parseDouble(plugin.getDescription().getVersion()) > Double
				.parseDouble(config.getString("version")))
		{
			// Update to latest version
			plugin.getLogger().info(
					"Updating to v" + plugin.getDescription().getVersion());
			update();
		}
	}

	/**
	 * This method is called to make the appropriate changes, most likely only
	 * necessary for database schema modification, for a proper update.
	 */
	public static void update()
	{
		final double ver = Double.parseDouble(plugin.getConfig().getString(
				"version"));
		String query = "";
		if (ver < 1.1)
		{
			// Update table to add laston column
			plugin.getLogger().info(
					CashFlow.TAG
							+ " Altering cashflow table to add laston column");
			query = "ALTER TABLE cashflow ADD laston REAL;";
			plugin.getDatabaseHandler().standardQuery(query);
			// Update table to add check column
			plugin.getLogger().info(
					CashFlow.TAG
							+ " Altering cashflow table to add check column");
			query = "ALTER TABLE cashflow ADD check INTEGER;";
			plugin.getDatabaseHandler().standardQuery(query);
			// Drop unneeded lastpaid table
			plugin.getLogger().info(CashFlow.TAG + " Dropping lastpaid table");
			query = "DROP TABLE lastpaid;";
			plugin.getDatabaseHandler().standardQuery(query);
		}
		if (ver < 1.12)
		{
			// Drop newly created tables
			plugin.getLogger().info(CashFlow.TAG + " Dropping empty tables.");
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.CASHFLOW.getName() + ";");
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.BUFFER.getName() + ";");
			// Update tables to have prefix
			plugin.getLogger().info(
					CashFlow.TAG + " Renaming cashflow table to '"
							+ Table.CASHFLOW.getName() + "'.");
			query = "ALTER TABLE cashflow RENAME TO "
					+ Table.CASHFLOW.getName() + ";";
			plugin.getDatabaseHandler().standardQuery(query);
			plugin.getLogger().info(
					CashFlow.TAG + " Renaming buffer table to '"
							+ Table.BUFFER.getName() + "'.");
			query = "ALTER TABLE buffer RENAME TO " + Table.BUFFER.getName()
					+ "";
			plugin.getDatabaseHandler().standardQuery(query);
		}
		if (ver < 1.184)
		{
			// Add new node for cashes and taxes
			final List<String> taxes = plugin.getConfig().getStringList(
					"taxes.list");
			for (String taxName : taxes)
			{
				plugin.getConfig()
						.set("taxes." + taxName + ".autoEnable", true);
				plugin.saveConfig();
			}
			final List<String> salaries = plugin.getConfig().getStringList(
					"salaries.list");
			for (String salaryName : salaries)
			{
				plugin.getConfig().set(
						"salaries." + salaryName + ".autoEnable", true);
				plugin.saveConfig();
			}
		}
		// Update version number in config.yml
		plugin.getConfig().set("version", plugin.getDescription().getVersion());
		plugin.saveConfig();
		plugin.getLogger().info(CashFlow.TAG + " Upgrade complete");
	}
}
