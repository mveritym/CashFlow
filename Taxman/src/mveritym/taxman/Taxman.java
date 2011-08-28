package mveritym.taxman;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.Configuration;
//import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Taxman extends JavaPlugin{
	
	public Logger log = Logger.getLogger("Minecraft");
	public Configuration config;
	
	public void onEnable() {
		log.info("Taxman plugin has been enabled.");
		config = getConfiguration();
		config.save();
	}
	
	public void onDisable() {
		log.info("Taxman plugin has been disabled.");		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("tax")) {
			return true;
		}
		return false;
	}
}
