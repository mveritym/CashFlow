package mveritym.taxman;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Taxman extends JavaPlugin{

	public Configuration config;
	public Logger log = Logger.getLogger("Minecraft");
	public PluginDescriptionFile info = null;
	public PluginManager pluginManager = null;
	
	public void onEnable() {		
		config = getConfiguration();
		config.save();
		info = getDescription();
		pluginManager = getServer().getPluginManager();
		
		log.info(info.getName() + " " + info.getVersion() + " has been enabled.");
	}
	
	public void onDisable() {
		log.info(info.getName() + " " + info.getVersion() + " has been disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("tax")) {
			return true;
		}
		return false;
	}
}
