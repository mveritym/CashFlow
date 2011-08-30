package mveritym.cashflow;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

//import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;

import com.nijikokun.cashflowregister.payment.Method;
import com.nijikokun.cashflowregister.payment.Methods;

public class CashFlow extends JavaPlugin{

	public Configuration config;
	public Logger log = Logger.getLogger("Minecraft");
	public PluginDescriptionFile info = null;
	public PluginManager pluginManager = null;
	//private WorldsHolder worldsHolder;
	private TaxManager taxManager = new TaxManager(this);
	public Methods Methods = null;
	public Method Method = null;
	
	public void onEnable() {		
		config = getConfiguration();
		config.save();
		info = getDescription();
		pluginManager = getServer().getPluginManager();
		
		pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, new server(this), Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, new server(this), Priority.Monitor, this);
        
        log.info(info.getName() + " Plugin " + info.getVersion() + " has been enabled.");
	}
	
	public void onDisable() {
		log.info(info.getName() + " Plugin " + info.getVersion() + " has been disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		boolean playerCanDo = false;
		boolean isConsole = false;
		Player senderPlayer = null;
		
		if(sender instanceof Player) {
			senderPlayer = (Player) sender;
			System.out.println(senderPlayer.isPermissionSet("cashflow." + cmd.getName()));
			if(senderPlayer.isOp() || senderPlayer.hasPermission("cashflow." + cmd.getName())) {
				playerCanDo = true;
				log.info("playerCanDo: " + playerCanDo);
			}
		} else if (sender instanceof ConsoleCommandSender) {			
			isConsole = true;
			log.info("isConsole: " + isConsole);
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
				case deletetax:
					if(args.length == 1) {
						taxManager.deleteTax(sender, args[0]);
						return true;
					} else if(args.length > 1) {
						sender.sendMessage(ChatColor.RED + "Too many arguments.");
						return false;
					} else {
						sender.sendMessage(ChatColor.RED + "Not enough arguments.");
						return false;
					}
				case listtaxes:
					if(args.length != 0) {
						sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
						return false;
					} else {
						taxManager.listTaxes(sender);
						return true;
					}
				default:
					break;
			}
		} 
		sender.sendMessage(ChatColor.RED + "You are not allowed to use that command.");
	    return false;
	}
}
