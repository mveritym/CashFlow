package mveritym.cashflow.taxer;

import mveritym.cashflow.database.Buffer;

public class Taxer {
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private String name;
	private Double hours;
	private int id;
	private static final int tickToHour = 72000;

	public Taxer(TaxManager taxManager, String taxName, Double hours) {
		this.taxManager = taxManager;
		this.setName(taxName);
		this.hours = hours;
		this.id = -1;

		//Added constant of +1 to ensure the salaries are paid first before taxes
		//if they have the same time schedule
		schedule(new Task(), Math.round(this.hours* tickToHour) +  1);
	}

	public Taxer(SalaryManager salaryManager, String taxName, Double hours) {
		this.salaryManager = salaryManager;
		this.setName(taxName);
		this.hours = hours;
		this.id = -1;

		schedule(new Task(), Math.round(this.hours * tickToHour));
	}

	public void start()
	{
		if(id == -1)
		{
			if(taxManager != null)
			{
				schedule(new Task(), Math.round(this.hours* tickToHour) +  1);
			}
			else if(salaryManager != null)
			{
				schedule(new Task(), Math.round(this.hours * tickToHour));
			}
		}
	}

	public String getState()
	{
		if(id != -1)
		{
			if(taxManager != null)
			{
				if(taxManager.cashFlow.getServer().getScheduler().isCurrentlyRunning(id))
				{
					return "running";
				}
				else if(Buffer.getInstance().buffering())
				{
					return "buffered";
				}
				else if(taxManager.cashFlow.getServer().getScheduler().isQueued(id))
				{
					return "queued";
				}
			}
			else if(salaryManager != null)
			{
				if(salaryManager.plugin.getServer().getScheduler().isCurrentlyRunning(id))
				{
					return "running";
				}
				else if(Buffer.getInstance().buffering())
				{
					return "buffered";
				}
				else if(salaryManager.plugin.getServer().getScheduler().isQueued(id))
				{
					return "queued";
				}
			}
		}
		return "cancelled";
	}

	public void cancel() {
		if(id != -1)
		{
			if(taxManager != null)
			{
				taxManager.cashFlow.getServer().getScheduler().cancelTask(id);
			}
			else if(salaryManager != null)
			{
				salaryManager.plugin.getServer().getScheduler().cancelTask(id);
			}
			id = -1;
		}
	}

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void schedule(Runnable run, long period)
	{
		if(taxManager != null)
		{
			id = taxManager.cashFlow.getServer().getScheduler().scheduleSyncRepeatingTask(taxManager.cashFlow, run, period, period);
			if(id == -1)
			{
				taxManager.cashFlow.getLogger().severe("Could not schedule " + this.getName());
			}
		}
		else if(salaryManager != null)
		{
			id = salaryManager.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(salaryManager.plugin, run, period, period);
			if(id == -1)
			{
				salaryManager.plugin.getLogger().severe("Could not schedule " + this.getName());
			}
		}
	}

	public void reschedule(long delay, long period)
	{
		//Stop previous task
		cancel();
		if(taxManager != null)
		{
			id = taxManager.cashFlow.getServer().getScheduler().scheduleSyncRepeatingTask(taxManager.cashFlow, new Task(), delay, period);
			if(id == -1)
			{
				taxManager.cashFlow.getLogger().severe("Could not schedule " + this.getName());
			}
		}
		else if(salaryManager != null)
		{
			id = salaryManager.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(salaryManager.plugin, new Task(), delay, period);
			if(id == -1)
			{
				salaryManager.plugin.getLogger().severe("Could not schedule " + this.getName());
			}
		}
	}

	class Task implements Runnable {
        public void run() {
        	if(taxManager != null)
        	{
        	taxManager.cashFlow.getLogger().info(taxManager.cashFlow.prefix
    				+ " Paying tax " + getName());
        	taxManager.payTax(name);
        	}
        	else if(salaryManager != null)
        	{
        		salaryManager.plugin.getLogger().info(salaryManager.plugin.prefix
    				+ " Paying salary " + getName());
        	salaryManager.paySalary(name);
        	}
        }
    }
}
