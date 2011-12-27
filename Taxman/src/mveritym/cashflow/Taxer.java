package mveritym.cashflow;

public class Taxer {

	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private String name;
	private Double hours;
	private int id;
	private final int tickToHour = 72000;

	public Taxer(TaxManager taxManager, String taxName, Double hours) {
		this.taxManager = taxManager;
		this.setName(taxName);
		this.hours = hours;

		schedule(new TaxTask(), Math.round(this.hours* tickToHour));
	}

	public Taxer(SalaryManager salaryManager, String taxName, Double hours) {
		this.salaryManager = salaryManager;
		this.setName(taxName);
		this.hours = hours;

		schedule(new SalaryTask(), Math.round(this.hours * tickToHour));
	}

	public void cancel() {
		TaxManager.cashFlow.getServer().getScheduler().cancelTask(id);
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

	class TaxTask implements Runnable {
        public void run() {
        	taxManager.payTax(getName());
        }
    }

    class SalaryTask implements Runnable {
        public void run() {
        	salaryManager.paySalary(getName());
        }
    }

}
