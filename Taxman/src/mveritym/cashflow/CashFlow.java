package mveritym.cashflow;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.cashflowregister.payment.Method;
import com.nijikokun.cashflowregister.payment.Methods;

public class CashFlow extends JavaPlugin{

	public Logger log = Logger.getLogger("Minecraft");
	public PluginDescriptionFile info;
	public PluginManager pluginManager;
	private TaxManager taxManager = new TaxManager(this);
	public PermissionsManager permsManager;
	public Methods Methods = null;
	public Method Method = null;
	public Plugin plugin;
	
	public void onEnable() {		
		info = getDescription();
		
		pluginManager = getServer().getPluginManager();		
		pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, new server(this), Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, new server(this), Priority.Monitor, this);
        
        permsManager = new PermissionsManager(this, "world");
        
        if(permsManager.pluginDetected()) {
        	System.out.println(info.getName() + " " + info.getVersion() + " has been enabled.");
        } else {
        	this.getPluginLoader().disablePlugin(this);
        }
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
			if(senderPlayer.isOp() || permsManager.hasPermission(senderPlayer, "cashflow." + cmd.getName())) {
				playerCanDo = true;
			}
		} else if (sender instanceof ConsoleCommandSender) {			
			isConsole = true;
		}
		
		CashFlowCommands execCmd = CashFlowCommands.valueOf(cmd.getName());
		if(playerCanDo || isConsole) {
			switch(execCmd) {
				case addtaxpayer:
					if(args.length == 2) {
						taxManager.addTaxpayer(sender, args[0], args[1]);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
						return false;
					}
				case createtax:
					String name;
					String percentOfBal;
					String interval;
					String receiverName;
					
					if(args.length == 4) {
						name = args[0];
						percentOfBal = args[1];
						interval = args[2];
						receiverName = args[3];
						
						taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
						return true;
					} else if(args.length == 3) {
						try {
							@SuppressWarnings("unused")
							double testDouble = Double.parseDouble(args[2]);
						} catch (Exception e) {
							sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
							return false;
						}
						
						name = args[0];
						percentOfBal = args[1];
						interval = args[2];
						receiverName = "null";
						
						taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
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
				case removetaxpayer:
					if(args.length == 2) {
						taxManager.removeTaxpayer(sender, args[0], args[1]);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
						return false;
					} 
				case taxinfo:
					if(args.length == 1) {
						taxManager.taxInfo(sender, args[0]);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
						return false;
					}
				default:
					break;
			}
		} 
		sender.sendMessage(ChatColor.RED + "You are not allowed to use that command.");
	    return false;
	}
	
}
