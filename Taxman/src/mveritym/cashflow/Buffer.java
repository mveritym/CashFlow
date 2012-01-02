package mveritym.cashflow;

import java.util.ArrayList;
import java.util.List;

public class Buffer implements Runnable {
	private static Buffer _instance;
	private CashFlow plugin;
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private List<Tax> queue =  new ArrayList<Tax>();
	private int id = -1;

	private Buffer()
	{

	}

	public static synchronized Buffer getInstance()
	{
		if(_instance == null)
		{
			_instance = new Buffer();
		}
		return _instance;
	}

	public void start()
	{
		if(id == -1)
		{
			id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 100, 100);
		}
		if(id == -1)
		{
			plugin.log.severe("Could not schedule buffer task.");
		}
	}

	public void setup(CashFlow cf, TaxManager rm, SalaryManager sm)
	{
		plugin = cf;
		taxManager = rm;
		salaryManager = sm;
	}

	public boolean buffering()
	{
		if(!queue.isEmpty())
		{
			return true;
		}
		return false;
	}

	public synchronized void addToBuffer(String name, String contract, boolean tax)
	{
		queue.add(new Tax(name, contract, tax));
	}

	@Override
	public void run() {
		if(!queue.isEmpty())
		{
			//Generate array
			final Tax[] array = queue.toArray(new Tax[0]);
			for(int i = 0; i < 30; i++)
			{
				//Prevent out of bounds
				if(i < array.length)
				{
					//Remove from queue
					final Tax t = array[i];
					if(t.tax)
					{
						//Pay tax
						taxManager.payTaxToUser(t.user, t.contract);
					}
					else
					{
						salaryManager.paySalaryToUser(t.user, t.contract);
					}
					queue.remove(array[i]);
				}
			}
		}
	}

	public synchronized void cancelBuffer()
	{
		if(id != -1)
		{
			//Stop thread
			plugin.getServer().getScheduler().cancelTask(id);
			if(!queue.isEmpty())
			{
				//Iterate through queue and pay appropriate tax/salary
				for(Tax tax : queue)
				{
					if(tax.tax)
					{
						//Pay tax
						taxManager.payTaxToUser(tax.user, tax.contract);
					}
					else
					{
						salaryManager.paySalaryToUser(tax.user, tax.contract);
					}
				}
			}
			id = -1;
		}
	}

	class Tax
	{
		String user, contract;
		double amount;
		boolean tax;

		public Tax(String n, String r, boolean t)
		{
			user = n;
			contract = r;
			tax = t;
		}
	}
}
