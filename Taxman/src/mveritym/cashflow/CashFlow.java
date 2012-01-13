package mveritym.cashflow;

import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CashFlow extends JavaPlugin {

	public final Logger log = Logger.getLogger("Minecraft");
	public PluginDescriptionFile info;
	public PluginManager pluginManager;
	public TaxManager taxManager;
	public SalaryManager salaryManager;
	public CommandManager commandManager;
	public PermissionsManager permsManager;
	public Economy eco;
	public Plugin plugin;
	public Config config;
	public String prefix;
	private SQLite database;

	@Override
	public void onLoad() {
		// Grab info
		info = getDescription();
		prefix = "[" + info.getName() + "]";
		// Grab config
		config = new Config(this);
		// Check if master player table exists
		database = new SQLite(log, prefix, "database", this.getDataFolder()
				.getAbsolutePath());
		if (!database.checkTable("cashflow"))
		{
			log.info(prefix + " Created master list table");
			// Master table
			database.createTable("CREATE TABLE `cashflow` (`playername` varchar(32) NOT NULL);");
		}
	}

	@Override
	public void onEnable() {
		pluginManager = getServer().getPluginManager();

		// Register Listener
		Listener listener = new Listener(this);
		pluginManager.registerEvent(Event.Type.PLAYER_JOIN, listener,
				Event.Priority.Monitor, this);
		//Grab Permissions
		permsManager = new PermissionsManager(this);
		// Grab Economy
		this.setupEconomy();

		//Create tax/salary managers
		taxManager = new TaxManager(this);
		salaryManager = new SalaryManager(this);

		//Instantiate Buffer
		Buffer buffer = Buffer.getInstance();
		buffer.setup(this, taxManager, salaryManager);
		buffer.start();

		// Set up command manager
		// TODO separate command manager into two separate classes
		// Then use getCommand(command).setExector(class) for the three
		// For salaries/taxes, since they're the same commands
		// Just check the command label to determine which one to use
		commandManager = new CommandManager(this, taxManager, salaryManager);

		log.info(prefix + " v" + info.getVersion() + " has been enabled.");

		// Enable taxes/salaries
		taxManager.enable();
		salaryManager.enable();
	}

	@Override
	public void onDisable() {
		// Save config
		this.saveConfig();
		// Disable taxes/salaries and finish the buffers if any exist
		// thus no economy changes are lost
		log.info(prefix + " " + " Finishing tax/salary buffers...");
		taxManager.disable();
		salaryManager.disable();
		Buffer.getInstance().cancelBuffer();
		// Disconnect from sql database? Dunno if necessary
		if (database.checkConnection())
		{
			// Close connection
			database.close();
		}
		log.info(prefix + " v" + info.getVersion() + " has been disabled.");
	}

	private void setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = this.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
		{
			eco = economyProvider.getProvider();
		}
		else
		{
			// No economy system found, disable
			log.warning(prefix + " No economy found!");
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	public Config getPluginConfig() {
		return config;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		boolean playerCanDo = false;
		boolean isConsole = false;
		Player senderPlayer = null;

		if (sender instanceof Player)
		{
			senderPlayer = (Player) sender;
			String node = "cashflow." + cmd.getName();
			if (senderPlayer.isOp()
					|| permsManager.hasPermission(senderPlayer, node))
			{
				playerCanDo = true;
			}
		}
		else if (sender instanceof ConsoleCommandSender)
		{
			isConsole = true;
		}

		CashFlowCommands execCmd = CashFlowCommands.valueOf(cmd.getName());

		if (playerCanDo || isConsole)
		{
			switch (execCmd)
			{
				case tax:
					if (args.length > 0)
					{
						return commandManager.taxCommand(sender, args);
					}
					else
					{
						return false;
					}
				case salary:
					if (args.length > 0)
					{
						if (commandManager == null)
						{
							log.severe("Command manger is null");
						}
						else
						{
							return commandManager.salaryCommand(sender, args);
						}
					}
					else
					{
						return false;
					}
				case cashflow:
					if (args.length > 0)
					{
						return commandManager.cashflowCommand(sender, args);
					}
					else
					{
						return false;
					}
				default:
					break;
			}
		}
		sender.sendMessage(ChatColor.RED
				+ "You are not allowed to use that command.");
		return true;
	}

	public SQLite getLiteDB() {
		return database;
	}

}
