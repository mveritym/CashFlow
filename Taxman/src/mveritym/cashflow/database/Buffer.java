package mveritym.cashflow.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.database.SQLibrary.Database.Query;
import mveritym.cashflow.taxer.SalaryManager;
import mveritym.cashflow.taxer.TaxManager;

public class Buffer implements Runnable {
	private static Buffer _instance;
	private CashFlow plugin;
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private final List<Tax> queue =  new ArrayList<Tax>();
	private int id = -1;

	private Buffer()
	{
		//Empty constructor
	}

	public static synchronized Buffer getInstance()
	{
		if(_instance == null)
		{
			_instance = new Buffer();
		}
		return _instance;
	}

	public synchronized void start()
	{
		if(id == -1)
		{
			id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 100, 100);
		}
		if(id == -1)
		{
			plugin.getLogger().severe("Could not schedule buffer task!");
		}
		else
		{
			//Add old entries into buffer
			try
			{
				final Query rs = plugin.getDatabaseHandler().select("SELECT * FROM " + plugin.getPluginConfig().tablePrefix
					+ "buffer;");
				if(rs.getResult().next())
				{
					do
					{
						final int isTax = rs.getResult().getInt("tax");
						if(isTax == 1)
						{
							addToBuffer(rs.getResult().getString("name"), rs.getResult().getString("contract"), true);
						}
						else
						{
							addToBuffer(rs.getResult().getString("name"), rs.getResult().getString("contract"), false);
						}
					}while(rs.getResult().next());
					plugin.getLogger().info(plugin.prefix + " Added old entries into buffer");
				}
				rs.closeQuery();
				//Clear buffer table of entries
				plugin.getDatabaseHandler().standardQuery("DELETE FROM " + plugin.getPluginConfig().tablePrefix
					+ "buffer");
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning(plugin.prefix + " SQL Exception");
				e.printStackTrace();
			}
		}
	}

	public synchronized void setup(CashFlow cf, TaxManager rm, SalaryManager sm)
	{
		plugin = cf;
		taxManager = rm;
		salaryManager = sm;
	}

	public synchronized boolean buffering()
	{
		if(!queue.isEmpty())
		{
			return true;
		}
		return false;
	}

	public synchronized int size()
	{
		return queue.size();
	}

	public synchronized void addToBuffer(String name, String contract, boolean tax)
	{
		queue.add(new Tax(name, contract, tax));
	}

	//TODO maybe have the payTaxToUser give a boolean on whether or not it
	//actually edited the account. And possibly, if it fails, then keep
	//it in the buffer until it resolves?
	@Override
	public synchronized void run() {
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
					try
					{
						if(t.tax)
						{
							//Pay tax
							taxManager.payTaxToUser(t.user, t.contract);
						}
						else
						{
							salaryManager.paySalaryToUser(t.user, t.contract);
						}
					}
					catch(NullPointerException e)
					{
						//ignore
					}
					queue.remove(array[i]);
				}
			}
		}
	}

	/**
	 * Save the buffer to table
	 */
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
						//Save tax
						plugin.getDatabaseHandler().standardQuery("INSERT INTO " + plugin.getPluginConfig().tablePrefix
					+ "buffer (name,contract,tax) VALUES('" + tax.user +"','" + tax.contract + "','1');");
					}
					else
					{
						plugin.getDatabaseHandler().standardQuery("INSERT INTO " + plugin.getPluginConfig().tablePrefix
					+ "buffer (name,contract,tax) VALUES('" + tax.user +"','" + tax.contract + "','0');");
					}
				}
			}
			id = -1;
		}
	}

	static class Tax
	{
		String user, contract;
		boolean tax;

		public Tax(String n, String r, boolean t)
		{
			user = n;
			contract = r;
			tax = t;
		}
	}
}
