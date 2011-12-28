package mveritym.cashflow;

import java.util.ArrayList;
import java.util.List;

public class Taxer {
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private String name;
	private Double hours;
	private int id, bufferId;
	private final int tickToHour = 72000;
	private boolean tax;
	private List<String> buffer;

	public Taxer(TaxManager taxManager, String taxName, Double hours) {
		this.taxManager = taxManager;
		this.setName(taxName);
		this.hours = hours;
		this.tax = true;
		this.bufferId = -1;
		this.buffer = new ArrayList<String>();

		schedule(new TaxTask(), Math.round(this.hours* tickToHour));
	}

	public Taxer(SalaryManager salaryManager, String taxName, Double hours) {
		this.salaryManager = salaryManager;
		this.setName(taxName);
		this.hours = hours;
		this.tax = false;
		this.bufferId = -1;
		this.buffer = new ArrayList<String>();

		schedule(new SalaryTask(), Math.round(this.hours * tickToHour));
	}

	public void finishBuffer()
	{
		if(bufferId != -1)
		{
			cancelBuffer();
		}
		if(!buffer.isEmpty())
		{
			for(int i = 0; i < buffer.size(); i++)
			{
				if(tax)
				{
					taxManager.payTax(buffer.get(i), getName(), true);
				}
				else
				{
					salaryManager.paySalary(buffer.get(i), getName(), true);
				}
			}
		}
	}

	public void cancel() {
		TaxManager.cashFlow.getServer().getScheduler().cancelTask(id);
	}

	public void cancelBuffer()
	{
		TaxManager.cashFlow.getServer().getScheduler().cancelTask(bufferId);
		bufferId = -1;
	}

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void schedule(Runnable run, long period)
	{
		id = TaxManager.cashFlow.getServer().getScheduler().scheduleSyncRepeatingTask(TaxManager.cashFlow, run, period, period);
		if(id == -1)
		{
			TaxManager.cashFlow.log.severe("Could not schedule " + this.getName());
		}
	}

	public void scheduleBuffer(Runnable run)
	{
		bufferId = TaxManager.cashFlow.getServer().getScheduler().scheduleSyncRepeatingTask(TaxManager.cashFlow, run, 0, 40);
		if(bufferId == -1)
		{
			TaxManager.cashFlow.log.severe("Could not schedule buffer task for: " + this.getName());
		}
	}

	class TaxTask implements Runnable {
        public void run() {
        	System.out.println("[" + TaxManager.cashFlow.info.getName()
    				+ "] Paying tax " + getName());
        	scheduleBuffer(new CFTask(taxManager.getUsers(getName())));
        }
    }

    class SalaryTask implements Runnable {
        public void run() {
        	System.out.println("[" + SalaryManager.cashFlow.info.getName()
    				+ "] Paying salary " + getName());
        	scheduleBuffer(new CFTask(salaryManager.getUsers(getName())));
        }
    }

    class CFTask implements Runnable
    {
    	public CFTask()
    	{
    		buffer = new ArrayList<String>();
    	}

    	public CFTask(List<String> in)
    	{
    		buffer = in;
    	}

		@Override
		public void run() {
			if(!buffer.isEmpty())
			{
				try
				{
					for(int i = 0; i < 50; i++)
					{
						String name = buffer.remove(0);
						if(tax)
						{
							taxManager.payTax(name, getName(), true);
						}
						else
						{
							salaryManager.paySalary(name, getName(), true);
						}
					}
				}
				catch (IndexOutOfBoundsException e)
				{
					//buffer is now empty
					cancelBuffer();
				}
				catch(UnsupportedOperationException r)
				{
					//Ignore?
					cancelBuffer();
				}
			}
			else
			{
				cancelBuffer();
			}
		}
    }
}
