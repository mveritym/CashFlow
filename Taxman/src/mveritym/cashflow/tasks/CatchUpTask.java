package mveritym.cashflow.tasks;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.config.Config;
import mveritym.cashflow.managers.SalaryManager;
import mveritym.cashflow.managers.TaxManager;

public class CatchUpTask
{
	class CatchUp implements Runnable {
		private CashFlow plugin;
		private Config config;
		private TaxManager taxManager;
		private SalaryManager salaryManager;
		private String prefix;

		public CatchUp(CashFlow plugin, Config conf, TaxManager tax,
				SalaryManager sal, String p) {
			this.plugin = plugin;
			config = conf;
			taxManager = tax;
			salaryManager = sal;
			prefix = p;
		}

		@Override
		public void run() {
			// Grab current time
			plugin.getLogger().info(prefix + " Running CatchUp");
			final long currentTime = System.currentTimeMillis();
			// Unit conversion
			final double millisecondToSecond = 0.001;
			final long hoursToSeconds = 3600;
			final int tickToHour = 72000;
			// Grab all enabled taxes and check their time
			for (CFTask tax : taxManager.taxTasks)
			{
				final String name = tax.getName();
				final long past = config.getLong("taxes." + name + ".lastPaid");
				if (past > 0)
				{
					double hoursDiff = (((currentTime - past) * millisecondToSecond) / hoursToSeconds);
					final double interval = config.getDouble(
							("taxes." + name + ".taxInterval"), 1);
					final long period = Math.round(interval * tickToHour) + 1;
					long delay = (long) ((interval * tickToHour) - (config.catchUpDelay * tickToHour));
					if (hoursDiff > interval)
					{
						// The difference in hours is greater than the interval
						// Do the number of iterations of the specified tax
						final double iterations = hoursDiff / interval;
						for (int i = 0; i < iterations; i++)
						{
							taxManager.payTax(name);
						}
						delay = (long) (((hoursDiff % interval) * tickToHour) - (config.catchUpDelay * tickToHour));
					}
					if (delay < 0)
					{
						delay = 0;
					}
					tax.reschedule(delay, period);
				}
			}
			// Grab all enabled salaries and check their time
			for (CFTask salary : salaryManager.salaryTasks)
			{
				final String name = salary.getName();
				final long past = config.getLong("salaries." + name + ".lastPaid");
				if (past > 0)
				{
					double hoursDiff = (((currentTime - past) * millisecondToSecond) / hoursToSeconds);
					final double interval = config.getDouble(
							("salaries." + name + ".salaryInterval"), 1);
					final long period = Math.round(interval * tickToHour) + 1;
					long delay = (long) ((interval * tickToHour) - (config.catchUpDelay * tickToHour));
					if (hoursDiff > interval)
					{
						// The difference in hours is greater than the interval
						// Do the number of iterations of the specified tax
						final double iterations = hoursDiff / interval;
						for (int i = 0; i < iterations; i++)
						{
							salaryManager.paySalary(name);
						}
						delay = (long) (((hoursDiff % interval) * tickToHour) - (config.catchUpDelay * tickToHour));
					}
					if (delay < 0)
					{
						delay = 0;
					}
					salary.reschedule(delay, period);
				}
			}
			plugin.getLogger().info(prefix + " Buffered iterations + Rescheduled threads");
		}
	}
}
