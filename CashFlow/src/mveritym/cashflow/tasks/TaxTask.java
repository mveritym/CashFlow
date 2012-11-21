package mveritym.cashflow.tasks;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.managers.TaxManager;

public class TaxTask extends CFTask implements Runnable
{
	private TaxManager manager;

	public TaxTask(CashFlow plugin, TaxManager manager, String name,
			Double hours)
	{
		super(plugin, name, hours);
		this.manager = manager;
		start();
	}
	
	@Override
	public void schedule(long period)
	{
		id = plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this, period, period);
		if (id == -1)
		{
			plugin.getLogger().severe("Could not schedule " + getName());
		}

	}

	@Override
	public void reschedule(long delay, long period)
	{
		cancel();
		id = plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this, delay, period);
		if (id == -1)
		{
			plugin.getLogger().severe("Could not schedule " + this.getName());
		}
	}

	@Override
	public void run()
	{
		plugin.getLogger().info("Paying tax '" + name + "'");
		manager.payTax(name);
	}

}
