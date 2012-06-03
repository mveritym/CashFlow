package mveritym.cashflow;

import mveritym.cashflow.commands.CashFlowCommand;
import mveritym.cashflow.commands.SalaryCommand;
import mveritym.cashflow.commands.TaxCommand;
import mveritym.cashflow.config.Config;
import mveritym.cashflow.config.Update;
import mveritym.cashflow.database.Buffer;
import mveritym.cashflow.database.DBHandler;
import mveritym.cashflow.managers.SalaryManager;
import mveritym.cashflow.managers.TaxManager;
import mveritym.cashflow.permissions.PermissionsManager;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CashFlow extends JavaPlugin
{
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private Economy eco;
	public Config config;
	public static String TAG;
	private DBHandler database;
	private boolean economyFound;

	@Override
	public void onEnable()
	{
		TAG = "[" + getDescription().getName() + "]";
		// Grab config
		config = new Config(this);
		// Check if master player table exists
		database = new DBHandler(this, config);
		// Updater
		Update.init(this);
		Update.checkUpdate();

		// Register Listener
		getServer().getPluginManager().registerEvents(
				new CashFlowListener(this), this);
		// Grab Permissions
		PermissionsManager.init(this);
		// Grab Economy
		this.setupEconomy();

		// Create tax/salary managers
		taxManager = new TaxManager(this);
		salaryManager = new SalaryManager(this);

		// Instantiate Buffer
		Buffer buffer = Buffer.getInstance();
		buffer.init(this, taxManager, salaryManager);
		buffer.start();

		// Set up command executors
		getCommand("cashflow").setExecutor(
				new CashFlowCommand(this, taxManager, salaryManager));
		getCommand("tax").setExecutor(new TaxCommand(this, taxManager));
		getCommand("salary")
				.setExecutor(new SalaryCommand(this, salaryManager));

		// Enable taxes/salaries
		taxManager.enable();
		salaryManager.enable();
		// Check the last paid and see how many times to iterate the tax/salary
		/*
		 * final int tickToHour = 72000; int id = this .getServer()
		 * .getScheduler() .scheduleSyncDelayedTask( this, new CatchUp(log,
		 * config, taxManager, salaryManager, prefix), (long)
		 * (config.catchUpDelay * tickToHour)); if (id == -1) {
		 * this.log.severe("Could not schedule the CatchUp thread..."); }
		 */
	}

	@Override
	public void onDisable()
	{
		// Save config
		this.reloadConfig();
		this.saveConfig();
		// Disable taxes/salaries and finish the buffers if any exist
		// thus no economy changes are lost
		if (economyFound)
		{
			taxManager.disable();
			salaryManager.disable();
			getLogger().info(TAG + " Saving buffer...");
			Buffer.getInstance().cancelBuffer();
		}
		// Disconnect from sql database? Dunno if necessary
		if (database.checkConnection())
		{
			// Close connection
			database.close();
			getLogger().info(TAG + " Closed database connection.");
		}
	}

	private void setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = this.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
		{
			eco = economyProvider.getProvider();
			economyFound = true;
		}
		else
		{
			// No economy system found, disable
			getLogger().warning(TAG + " No economy found!");
			this.getServer().getPluginManager().disablePlugin(this);
			economyFound = false;
		}
	}

	public Config getPluginConfig()
	{
		return config;
	}

	public DBHandler getDatabaseHandler()
	{
		return database;
	}

	public Economy getEconomy()
	{
		return eco;
	}
}
