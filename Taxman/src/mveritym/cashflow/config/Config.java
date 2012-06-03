package mveritym.cashflow.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mveritym.cashflow.CashFlow;

import org.bukkit.configuration.ConfigurationSection;

public class Config {
	// Class variables
	private CashFlow plugin;
	public boolean debug, useMySQL, importSQL;
	public String prefix, suffix, host, port, database, user, password;
	public static String tablePrefix;
	public double catchUpDelay;

	public Config(CashFlow plugin) {
		this.plugin = plugin;
		final ConfigurationSection config = plugin.getConfig();
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
		defaults.put("version", plugin.getDescription().getVersion());
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
			plugin.getLogger().info(CashFlow.TAG
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
		plugin.saveConfig();
	}

	public void set(String path, Object o) {
		final ConfigurationSection config = plugin.getConfig();
		config.set(path, o);
		plugin.saveConfig();
	}

	/**
	 * Checks bounds on variables to make sure nothing user given is wrong
	 */
	private void checkBounds() {
		if(catchUpDelay > 0.25 || catchUpDelay < 0)
		{
			catchUpDelay = 0.08;
			plugin.getLogger().warning(CashFlow.TAG + " catchUpDelay is wrong. Setting to default.");
		}
	}

	public void save() {
		plugin.saveConfig();
	}

	public void reload() {
		plugin.reloadConfig();
		final ConfigurationSection config = plugin.getConfig();
		debug = config.getBoolean("debug", false);
		// Reinitialize variables
		debug = config.getBoolean("debug", false);
		prefix = config.getString("prefix", "");
		suffix = config.getString("suffix", "");
		catchUpDelay = config.getDouble("catchUpDelay", 0.08);
		checkBounds();
	}

	public void setProperty(String path, Object o) {
		plugin.getConfig().set(path, o);
	}

	public Object getProperty(String path) {
		return plugin.getConfig().get(path);
	}

	public List<String> getStringList(String path) {
		List<String> list = plugin.getConfig().getStringList(path);
		if (list != null)
		{
			return list;
		}
		return new ArrayList<String>();
	}

	public void removeProperty(String path) {
		plugin.getConfig().set(path, null);
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
