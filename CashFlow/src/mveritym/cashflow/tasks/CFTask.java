package mveritym.cashflow.tasks;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.database.Buffer;

public abstract class CFTask
{
	protected CashFlow plugin;
	protected String name;
	protected Double hours;
	protected int id;
	protected static final int tickToHour = 72000;
	
	public CFTask(CashFlow plugin, String name, Double hours)
	{
		this.plugin = plugin;
		this.name = name;
		this.hours = hours;
		this.id = -1;
	}
	
	public void start()
	{
		schedule(Math.round(this.hours* tickToHour) +  1);
	}
	
	public abstract void schedule(long period);
//	{
//		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, run, period, period);
//		if(id == -1)
//		{
//			plugin.getLogger().severe("Could not schedule " + getName());
//		}
//	}
	
	public abstract void reschedule(long delay, long period);
//	{
//		cancel();
//		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, null, delay, period);
//		if(id == -1)
//		{
//			plugin.getLogger().severe("Could not schedule " + this.getName());
//		}
//	}
	
	public void cancel()
	{
		if(id != -1)
		{
			plugin.getServer().getScheduler().cancelTask(id);
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getState()
	{
		if(id != -1)
		{
			if(plugin.getServer().getScheduler().isCurrentlyRunning(id))
				return "running";
			else if(Buffer.getInstance().buffering())
				return "buffered";
			else if(plugin.getServer().getScheduler().isQueued(id))
				return "queued";
		}
		return "cancelled";
	}
}
