package mveritym.cashflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

public class Config {
	//Class variables
	private CashFlow cf;
	public boolean debug;
	public String prefix, suffix;


	public Config(CashFlow plugin)
	{
		cf = plugin;
		final ConfigurationSection config = cf.getConfig();
		//Hashmap of defaults
		final Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put("world", "world");
		defaults.put("taxes.list", new ArrayList<String>());
		defaults.put("salaries.list", new ArrayList<String>());
		defaults.put("prefix", "$");
		defaults.put("suffix", "");
		defaults.put("version", cf.getDescription().getVersion());
		boolean gen = false;
		for(final Entry<String, Object> e : defaults.entrySet())
		{
			if(!config.contains(e.getKey()))
			{
				config.set(e.getKey(), e.getValue());
				gen = true;
			}
		}
		if(gen)
		{
			cf.log.info(cf.prefix + " No CashFlow config file found. Creating config file.");
		}
		//Initialize variables
		debug = config.getBoolean("debug", false);
		prefix = config.getString("prefix", "");
		suffix = config.getString("suffix", "");
		//Save config
		cf.saveConfig();
	}

	/**
	 * Check if updates are necessary
	 */
	public void checkUpdate()
	{
		// Check if need to update
		ConfigurationSection config = cf.getConfig();
		if (Double.parseDouble(cf.getDescription().getVersion()) > Double
				.parseDouble(config.getString("version")))
		{
			// Update to latest version
			cf.log.info(
					cf.prefix + " Updating to v"
							+ cf.getDescription().getVersion());
			this.update();
		}
	}

	/**
	 * This method is called to make the appropriate changes, most likely only
	 * necessary for database schema modification, for a proper update.
	 */
	private void update() {
		//Grab current version
		final double ver = Double.parseDouble(cf.getConfig().getString("version"));
		String query = "";
		if(ver < 1.1)
		{
			//Update table to add laston column
			cf.log.info(cf.prefix + " Altering cashflow table to add laston column");
			query = "ALTER TABLE cashflow ADD laston REAL;";
			cf.getLiteDB().standardQuery(query);
		}
	}

	public void save()
	{
		cf.saveConfig();
	}

	public void reload()
	{
		cf.reloadConfig();
		final ConfigurationSection config = cf.getConfig();
		debug = config.getBoolean("debug", false);
		//Reinitialize variables
		debug = config.getBoolean("debug", false);
		prefix = config.getString("prefix", "");
		suffix = config.getString("suffix", "");
	}

	public void setProperty(String path, Object o) {
		cf.getConfig().set(path, o);
	}

	public Object getProperty(String path)
	{
		return cf.getConfig().get(path);
	}

	public List<String> getStringList(String path) {
		List<String> list = cf.getConfig().getStringList(path);
		if(list != null)
		{
			return list;
		}
		return new ArrayList<String>();
	}

	public void removeProperty(String path) {
		cf.getConfig().set(path, null);
	}

	public boolean getBoolean(String path, boolean b) {
		return (Boolean)(this.getProperty(path));
	}

	public double getDouble(String path, double d) {
		return (Double) (this.getProperty(path));
	}

	public String getString(String path) {
		return (String) this.getProperty(path);
	}
}
