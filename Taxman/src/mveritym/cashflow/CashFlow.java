package mveritym.cashflow;

import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

import net.milkbowl.vault.economy.Economy;

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
	public PermissionsManager permsManager;
	public Economy eco;
	public Plugin plugin;
	public Config config;
	public String prefix;
	private SQLite database;
	private boolean economyFound;

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
			database.createTable("CREATE TABLE `cashflow` (`playername` varchar(32) NOT NULL, `laston` REAL, UNIQUE(`playername`));");
		}
		if (!database.checkTable("buffer"))
		{
			log.info(prefix + " Created buffer table");
			// Table to save buffer items
			database.createTable("CREATE TABLE `buffer` (`name` varchar(32) NOT NULL, `contract` TEXT NOT NULL, `tax` INTEGER NOT NULL);");
		}
	}

	@Override
	public void onEnable() {
		//Check for updates to database:
		config.checkUpdate();
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

		// Set up command executors
		CashFlowCommand cashFlowCom = new CashFlowCommand(this, permsManager, taxManager, salaryManager);
		TaxCommand taxCom = new TaxCommand(this, permsManager, taxManager);
		SalaryCommand salaryCom = new SalaryCommand(this, permsManager, salaryManager);
		getCommand("cashflow").setExecutor(cashFlowCom);
		getCommand("tax").setExecutor(taxCom);
		getCommand("salary").setExecutor(salaryCom);

		log.info(prefix + " v" + info.getVersion() + " has been enabled.");

		// Enable taxes/salaries
		taxManager.enable();
		salaryManager.enable();
		//Check the last paid and see how many times to iterate the tax/salary
		final int tickToHour = 72000;
		int id = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new CatchUp(log, config, taxManager, salaryManager, prefix), (long) (config.catchUpDelay * tickToHour));
		if(id == -1)
		{
			this.log.severe("Could not schedule the CatchUp thread...");
		}
	}

	@Override
	public void onDisable() {
		// Save config
		this.saveConfig();
		// Disable taxes/salaries and finish the buffers if any exist
		// thus no economy changes are lost
		if(economyFound)
		{
			taxManager.disable();
			salaryManager.disable();
			log.info(prefix + " Saving buffer...");
			Buffer.getInstance().cancelBuffer();
		}
		// Disconnect from sql database? Dunno if necessary
		if (database.checkConnection())
		{
			// Close connection
			database.close();
			log.info(prefix + " Closed database connection.");
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
			economyFound = true;
		}
		else
		{
			// No economy system found, disable
			log.warning(prefix + " No economy found!");
			this.getServer().getPluginManager().disablePlugin(this);
			economyFound = false;
		}
	}

	public Config getPluginConfig() {
		return config;
	}

	public SQLite getLiteDB() {
		return database;
	}

	static class CatchUp implements Runnable
	{
		private Logger log;
		private Config config;
		private TaxManager taxManager;
		private SalaryManager salaryManager;
		private String prefix;

		public CatchUp(Logger l, Config conf, TaxManager tax, SalaryManager sal, String p)
		{
			log = l;
			config = conf;
			taxManager = tax;
			salaryManager = sal;
			prefix = p;
		}
		@Override
		public void run() {
			//Grab current time
			final long currentTime = System.currentTimeMillis();
			//Unit conversion
			final double millisecondToSecond = 0.001;
			final long hoursToSeconds = 3600;
			final int tickToHour = 72000;
			//Grab all enabled taxes and check their time
			for(Taxer tax : taxManager.taxTasks)
			{
				final String name = tax.getName();
				final long past = config.getLong("taxes." + name + ".lastpaid");
				double hoursDiff = ((currentTime - past)*millisecondToSecond)/hoursToSeconds;
				final double interval = config.getDouble(("taxes." + name + ".taxInterval"), 1);
				if(hoursDiff > interval)
				{
					//The difference in hours is greater than the interval
					//Do the number of iterations of the specified tax
					final double iterations = hoursDiff / interval;
					for(int i = 0; i < iterations; i++)
					{
						taxManager.payTax(name);
					}
				}
				//Set delay time
				final long period = Math.round(interval * tickToHour) + 1;
				final long delay = (long) (hoursDiff % interval);
				tax.reschedule(delay, period);
			}
			//Grab all enabled salaries and check their time
			for(Taxer salary : salaryManager.salaryTasks)
			{
				final String name = salary.getName();
				final long past = config.getLong("salaries." + name + ".lastpaid");
				double hoursDiff = ((currentTime - past)*millisecondToSecond)/hoursToSeconds;
				final double interval = config.getDouble(("salaries." + name + ".taxInterval"), 1);
				if(hoursDiff > interval)
				{
					//The difference in hours is greater than the interval
					//Do the number of iterations of the specified tax
					final double iterations = hoursDiff / interval;
					for(int i = 0; i < iterations; i++)
					{
						salaryManager.paySalary(name);
					}
				}
				// Set delay time
				final long period = Math.round(interval * tickToHour) + 1;
				final long delay = (long) (hoursDiff % interval);
				salary.reschedule(delay, period);
			}
			log.info(prefix + " Buffered iterations + Rescheduled threads");
		}
	}
}
