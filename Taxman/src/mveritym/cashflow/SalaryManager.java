package mveritym.cashflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.Configuration;

public class SalaryManager {
	
	protected static CashFlow cashFlow;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    List<String> salaries;
    List<String> paidGroups;
    ListIterator<String> iterator;
    Timer timer = new Timer();
    //Collection<Taxer> taxTasks = new ArrayList<Taxer>();

	public SalaryManager(CashFlow cashFlow) {
		conf = null;
		loadConf();
		SalaryManager.cashFlow = cashFlow;
		salaries = conf.getStringList("salaries.list", null);
	}
	
	public void loadConf() {
		File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");

        if (f.exists()) {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	System.out.println("[CashFlow] No config file found. Creating data file.");
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);  
            List<String> tempList = null;
            conf.setProperty("salaries.list", tempList);
            conf.save();
        }
    }
	
	public void createSalary(CommandSender sender, String name, String paycheck, String interval, String employer) {
		String salaryName = name;
		double salary = Double.parseDouble(paycheck);
		double salaryInterval = Double.parseDouble(interval);
		List<String> paidGroups = null;
		
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		iterator = salaries.listIterator();
		
		if(salary <= 0) {
			sender.sendMessage(ChatColor.RED + "Please choose a salary greater than 0.");
			return;
		} else if(salaryInterval <= 0) {
			sender.sendMessage(ChatColor.RED + "Please choose a salary interval greater than 0.");
			return;
		} else if(!SalaryManager.cashFlow.isPlayer(employer) && !(employer.equals("null"))) {
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return;
		} else {
			while(iterator.hasNext()) {
				if(iterator.next().equals(salaryName)) {
					sender.sendMessage(ChatColor.RED + "A salary with that name has already been created.");
					return;
				}
			}
		}
		
		salaries.add(salaryName);	
		conf.setProperty("salaries.list", salaries);
		conf.setProperty("salaries." + salaryName + ".salary", salary);
		conf.setProperty("salaries." + salaryName + ".salaryInterval", salaryInterval);
		conf.setProperty("salaries." + salaryName + ".employer", employer);
		conf.setProperty("salaries." + salaryName + ".paidGroups", paidGroups);
		conf.setProperty("salaries." + salaryName + ".lastPaid", null);
		conf.setProperty("salaries." + salaryName + ".exceptedPlayers", null);
		conf.save();
	
		sender.sendMessage(ChatColor.GREEN + "New salary " + salaryName + " created successfully.");
	}
}
