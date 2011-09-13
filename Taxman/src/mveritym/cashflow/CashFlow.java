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
	public CommandManager commandManager;
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
        commandManager = new CommandManager(this);
        
        if(permsManager.pluginDetected()) {
        	System.out.println("[" + info.getName() + "] " + info.getVersion() + " has been enabled.");
        } else {
        	this.getPluginLoader().disablePlugin(this);
        }
	}
	
	public void onDisable() {
		log.info("[" + info.getName() + "] " + info.getVersion() + " has been disabled.");
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
				case tax:
					if(args.length > 0) {
						return commandManager.taxCommand(sender, args);
					} else {
						return false;
					}					
				case salary:
					if(args.length > 0) {
						return commandManager.salaryCommand(sender, args);
					} else {
						return false;
					}	
				case cashflow:
					if(args.length > 0) {
						return commandManager.cashflowCommand(sender, args);
					} else {
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
