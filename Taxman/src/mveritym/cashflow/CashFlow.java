package mveritym.cashflow;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class CashFlow extends JavaPlugin{

	public Logger log = Logger.getLogger("Minecraft");
	public PluginDescriptionFile info;
	public PluginManager pluginManager;
	public TaxManager taxManager;
	public SalaryManager salaryManager;
	public CommandManager commandManager;
	public PermissionsManager permsManager;
	public Economy eco;
	public Plugin plugin;
	public Config config;

	public void onEnable() {
		info = getDescription();

        pluginManager = getServer().getPluginManager();

        //Grab config
        config = new Config(this);

        taxManager = new TaxManager(this);
        salaryManager = new SalaryManager(this);
        permsManager = new PermissionsManager(this);
        //Grab Economy
        this.setupEconomy();

        commandManager = new CommandManager(this, taxManager, salaryManager);

        System.out.println("[" + info.getName() + "] v" + info.getVersion() + " has been enabled.");

        taxManager.disable();
		salaryManager.disable();
		taxManager.enable();
		salaryManager.enable();
	}

	public void onDisable() {
		log.info("[" + info.getName() + "] " + info.getVersion() + " has been disabled.");
	}

	private void setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economyProvider != null)
		{
			eco = economyProvider.getProvider();
		}
	}

	public Config getPluginConfig()
	{
		return config;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		boolean playerCanDo = false;
		boolean isConsole = false;
		Player senderPlayer = null;

		if(sender instanceof Player) {
			senderPlayer = (Player) sender;
			String node = "cashflow." + cmd.getName();
			if(senderPlayer.isOp() || permsManager.hasPermission(senderPlayer, node)) {
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
						if(commandManager == null)
						{
							log.severe("Command manger is null");
						}
						else
						{
							return commandManager.salaryCommand(sender, args);
						}
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
	    return true;
	}

}
