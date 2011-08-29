package mveritym.cashflow;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;

public class CashFlow extends JavaPlugin{

	public Configuration config;
	public Logger log = Logger.getLogger("Minecraft");
	public PluginDescriptionFile info = null;
	public PluginManager pluginManager = null;
	private WorldsHolder worldsHolder;
	private TaxManager taxManager = new TaxManager(this);
	
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
		boolean playerCanDo = false;
		boolean isConsole = false;
		Player senderPlayer = null;
		
		if(sender instanceof Player) {
			senderPlayer = (Player) sender;
			if(senderPlayer.isOp() || worldsHolder.getWorldPermissions(senderPlayer).has(senderPlayer, "groupmanager." + cmd.getName())) {
				playerCanDo = true;
			}
		} else if (sender instanceof ConsoleCommandSender) {
			isConsole = true;
		}
		
		CashFlowCommands execCmd = CashFlowCommands.valueOf(cmd.getName());
		if(playerCanDo || isConsole) {
			switch(execCmd) {
				case createtax:
					if(args.length == 4) {
						String name = args[0];
						String percentOfBal = args[1];
						String interval = args[2];
						String receiverName = args[3];
						taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
						return true;
					} else if (args.length > 3){
						sender.sendMessage(ChatColor.RED + "Too many arguments.");
						return false;
					} else {
						sender.sendMessage(ChatColor.RED + "Not enough arguments.");
						return false;
					}	
				case test:
					sender.sendMessage(ChatColor.RED + "Testing!");
					return true;
				default:
					break;
			}
		} 
		sender.sendMessage(ChatColor.RED + "You are not allowed to use that command.");
	    return false;
	}
}
