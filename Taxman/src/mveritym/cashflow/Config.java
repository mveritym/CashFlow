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


	public Config(CashFlow plugin)
	{
		cf = plugin;
		ConfigurationSection config = cf.getConfig();
		//Hashmap of defaults
		final Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put("world", "world");
		defaults.put("taxes.list", new ArrayList<String>());
		defaults.put("salaries.list", new ArrayList<String>());
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
			cf.log.info("[" + cf.info.getName() + "] No CashFlow config file found. Creating config file.");
		}
		debug = config.getBoolean("debug", false);
		//Save config
		cf.saveConfig();
	}

	public void save()
	{
		cf.saveConfig();
	}

	public void setProperty(String path, Object o) {
		cf.getConfig().set(path, o);
	}

	public Object getProperty(String path)
	{
		return cf.getConfig().get(path);
	}

	public List<String> getStringList(String path) {
		@SuppressWarnings ("unchecked")
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
