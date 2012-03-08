package mveritym.cashflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

public class Config {
	// Class variables
	private CashFlow cf;
	public boolean debug, useMySQL, importSQL;
	public String prefix, suffix, host, port, database, user, password, tablePrefix;
	public double catchUpDelay;

	public Config(CashFlow plugin) {
		cf = plugin;
		final ConfigurationSection config = cf.getConfig();
		// Hashmap of defaults
		final Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put("world", "world");
		defaults.put("taxes.list", new ArrayList<String>());
		defaults.put("salaries.list", new ArrayList<String>());
		defaults.put("prefix", "$");
		defaults.put("suffix", "");
		defaults.put("catchUpDelay", 0.08);
		defaults.put("mysql.use", false);
		defaults.put("mysql.host", "localhost");
		defaults.put("mysql.port", 3306);
		defaults.put("mysql.database", "minecraft");
		defaults.put("mysql.user", "username");
		defaults.put("mysql.password", "pass");
		defaults.put("mysql.tablePrefix", "cf_");
		defaults.put("mysql.import", false);
		defaults.put("version", cf.getDescription().getVersion());
		boolean gen = false;
		for (final Entry<String, Object> e : defaults.entrySet())
		{
			if (!config.contains(e.getKey()))
			{
				config.set(e.getKey(), e.getValue());
				gen = true;
			}
		}
		if (gen)
		{
			cf.log.info(cf.prefix
					+ " No CashFlow config file found. Creating config file.");
		}
		// Initialize variables
		debug = config.getBoolean("debug", false);
		prefix = config.getString("prefix", "");
		suffix = config.getString("suffix", "");
		catchUpDelay = config.getDouble("catchUpDelay", 0.08);
		useMySQL = config.getBoolean("mysql.use", false);
		host = config.getString("mysql.host", "localhost");
		port = config.getString("mysql.port", "3306");
		database = config.getString("mysql.database", "minecraft");
		user = config.getString("mysql.user", "user");
		password = config.getString("mysql.password", "password");
		tablePrefix = config.getString("mysql.prefix", "cf_");
		importSQL = config.getBoolean("mysql.import", false);
		checkBounds();
		// Save config
		cf.saveConfig();
	}

	public void set(String path, Object o) {
		final ConfigurationSection config = cf.getConfig();
		config.set(path, o);
		cf.saveConfig();
	}

	/**
	 * Checks bounds on variables to make sure nothing user given is wrong
	 */
	private void checkBounds() {
		if(catchUpDelay > 0.25 || catchUpDelay < 0)
		{
			catchUpDelay = 0.08;
			cf.log.warning(cf.prefix + " catchUpDelay is wrong. Setting to default.");
		}
	}

	/**
	 * Check if updates are necessary
	 */
	public void checkUpdate() {
		// Check if need to update
		ConfigurationSection config = cf.getConfig();
		if (Double.parseDouble(cf.getDescription().getVersion()) > Double
				.parseDouble(config.getString("version")))
		{
			// Update to latest version
			cf.log.info(cf.prefix + " Updating to v"
					+ cf.getDescription().getVersion());
			this.update();
		}
	}

	/**
	 * This method is called to make the appropriate changes, most likely only
	 * necessary for database schema modification, for a proper update.
	 */
	private void update() {
		// Grab current version
		final double ver = Double.parseDouble(cf.getConfig().getString(
				"version"));
		String query = "";
		if (ver < 1.1)
		{
			// Update table to add laston column
			cf.log.info(cf.prefix
					+ " Altering cashflow table to add laston column");
			query = "ALTER TABLE cashflow ADD laston REAL;";
			cf.getDatabaseHandler().standardQuery(query);
			// Update table to add check column
			cf.log.info(cf.prefix
					+ " Altering cashflow table to add check column");
			query = "ALTER TABLE cashflow ADD check INTEGER;";
			cf.getDatabaseHandler().standardQuery(query);
			//Drop unneeded lastpaid table
			cf.log.info(cf.prefix
					+ " Dropping lastpaid table");
			query = "DROP TABLE lastpaid;";
			cf.getDatabaseHandler().standardQuery(query);
		}
		if(ver < 1.12)
		{
			//Drop newly created tables
			cf.log.info(
					cf.prefix
							+ " Dropping empty tables.");
			cf.getDatabaseHandler().standardQuery("DROP TABLE " + tablePrefix + "cashflow;");
			cf.getDatabaseHandler().standardQuery("DROP TABLE " + tablePrefix + "buffer;");
			// Update tables to have prefix
			cf.log.info(
					cf.prefix
							+ " Renaming cashflow table to '" + tablePrefix +"cashflow'.");
			query = "ALTER TABLE cashflow RENAME TO " + tablePrefix + "cashflow;";
			cf.getDatabaseHandler().standardQuery(query);
			cf.log.info(
					cf.prefix
							+ " Renaming buffer table to '" + tablePrefix +"buffer'.");
			query = "ALTER TABLE buffer RENAME TO " + tablePrefix + "buffer;";
			cf.getDatabaseHandler().standardQuery(query);
		}
		if(ver < 1.184)
		{
			//Add new node for cashes and taxes
			final List<String> taxes = cf.getConfig().getStringList("taxes.list");
			for (String taxName : taxes)
			{
				cf.getConfig().set("taxes." + taxName + ".autoEnable", true);
				cf.saveConfig();
			}
			final List<String> salaries = cf.getConfig().getStringList("salaries.list");
			for(String salaryName : salaries)
			{
				cf.getConfig().set("salaries." + salaryName
						+ ".autoEnable", true);
				cf.saveConfig();
			}
		}
		// Update version number in config.yml
		cf.getConfig().set("version", cf.getDescription().getVersion());
		cf.saveConfig();
		cf.log.info(cf.prefix + " Upgrade complete");
	}

	public void save() {
		cf.saveConfig();
	}

	public void reload() {
		cf.reloadConfig();
		final ConfigurationSection config = cf.getConfig();
		debug = config.getBoolean("debug", false);
		// Reinitialize variables
		debug = config.getBoolean("debug", false);
		prefix = config.getString("prefix", "");
		suffix = config.getString("suffix", "");
		catchUpDelay = config.getDouble("catchUpDelay", 0.08);
		checkBounds();
	}

	public void setProperty(String path, Object o) {
		cf.getConfig().set(path, o);
	}

	public Object getProperty(String path) {
		return cf.getConfig().get(path);
	}

	public List<String> getStringList(String path) {
		List<String> list = cf.getConfig().getStringList(path);
		if (list != null)
		{
			return list;
		}
		return new ArrayList<String>();
	}

	public void removeProperty(String path) {
		cf.getConfig().set(path, null);
	}

	public boolean getBoolean(String path, boolean b) {
		return (Boolean) (this.getProperty(path));
	}

	public double getDouble(String path, double d) {
		return (Double) (this.getProperty(path));
	}

	public String getString(String path) {
		return (String) this.getProperty(path);
	}

	public long getLong(String path)
	{
		final String temp = this.getString(path);
		long l = 0;
		if(temp != null)
		{
			try
			{
				l = Long.parseLong(temp);
			}
			catch(NumberFormatException e)
			{
				l = 0;
			}
		}
		return l;
	}
}
