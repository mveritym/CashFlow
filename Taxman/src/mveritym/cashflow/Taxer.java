package mveritym.cashflow;

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

		scheduleTax(new TaxTask(), Math.round(this.hours* tickToHour));
	}

	public Taxer(SalaryManager salaryManager, String taxName, Double hours) {
		this.salaryManager = salaryManager;
		this.setName(taxName);
		this.hours = hours;
		this.id = -1;

		scheduleSalary(new SalaryTask(), Math.round(this.hours * tickToHour));
	}

	public String getState()
	{
		if(id != -1)
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
		return "cancelled";
	}

	public void cancelTax() {
		if(id != -1)
		{
			taxManager.cashFlow.getServer().getScheduler().cancelTask(id);
		}
	}

	public void cancelSalary() {
		if(id != -1)
		{
			salaryManager.cashFlow.getServer().getScheduler().cancelTask(id);
		}
	}

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void scheduleTax(Runnable run, long period)
	{
		id = taxManager.cashFlow.getServer().getScheduler().scheduleSyncRepeatingTask(taxManager.cashFlow, run, period, period);
		if(id == -1)
		{
			taxManager.cashFlow.log.severe("Could not schedule " + this.getName());
		}
	}

	public void scheduleSalary(Runnable run, long period)
	{
		id = salaryManager.cashFlow.getServer().getScheduler().scheduleSyncRepeatingTask(salaryManager.cashFlow, run, period, period);
		if(id == -1)
		{
			salaryManager.cashFlow.log.severe("Could not schedule " + this.getName());
		}
	}

	class TaxTask implements Runnable {
        public void run() {
        	taxManager.cashFlow.log.info(taxManager.cashFlow.prefix
    				+ " Paying tax " + getName());
        	taxManager.payTax(name);
        }
    }

    class SalaryTask implements Runnable {
        public void run() {
        	salaryManager.cashFlow.log.info(salaryManager.cashFlow.prefix
    				+ " Paying salary " + getName());
        	salaryManager.paySalary(name);
        }
    }
}
