package mveritym.cashflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.config.Configuration;

public class TaxManager {
	protected static CashFlow cashFlow;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    List<String> taxes;
    List<String> payingGroups;
    ListIterator<String> iterator;
    Timer timer = new Timer();
    Collection<Taxer> taxTasks = new ArrayList<Taxer>();
    
	public TaxManager(CashFlow cashFlow) {
    	TaxManager.cashFlow = cashFlow;
    	
    	conf = null;
    	
    	loadConf();
    	taxes = conf.getStringList("taxes.list", null);    	
        
        File f = new File(TaxManager.cashFlow.getDataFolder(), "users.yml");
        
        if (f.exists()) {
            uconf = new Configuration(f);
            uconf.load();
        }
        else {
            this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "users.yml");
            TaxManager.uconf = new Configuration(confFile);
            uconf.save();
        }
    }
    
    public void loadConf() {
    	File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");

        if (f.exists()) {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	System.out.println("No CashFlow config file found. Creating config file.");
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);  
            List<String> tempList = null;
            conf.setProperty("taxes.list", tempList);
            conf.save();
        }
    }
    
	public void createTax(CommandSender sender, String name, String percentOfBal, String interval, String taxReceiver) {
		String taxName = name;
		double percentIncome = Double.parseDouble(percentOfBal.split("%")[0]);
		double taxInterval = Double.parseDouble(interval);
		List<String> payingGroups = null;
		
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		iterator = taxes.listIterator();
		
		if(percentIncome > 100 || percentIncome <= 0) {
			sender.sendMessage(ChatColor.RED + "Please choose a % of income between 0 and 100");
			return;
		} else if(taxInterval <= 0) {
			sender.sendMessage(ChatColor.RED + "Please choose a tax interval greater than 0.");
			return;
		} else if(!isPlayer(taxReceiver)) {
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return;
		} else {
			while(iterator.hasNext()) {
				if(iterator.next().equals(taxName)) {
					sender.sendMessage(ChatColor.RED + "A tax with that name has already been created.");
					return;
				}
			}
		}
		
		taxes.add(taxName);	
		conf.setProperty("taxes.list", taxes);
		conf.setProperty("taxes." + taxName + ".percentIncome", percentIncome);
		conf.setProperty("taxes." + taxName + ".taxInterval", taxInterval);
		conf.setProperty("taxes." + taxName + ".receiver", taxReceiver);
		conf.setProperty("taxes." + taxName + ".payingGroups", payingGroups);
		conf.setProperty("taxes." + taxName + ".lastPaid", null);
		conf.save();
	
		sender.sendMessage(ChatColor.GREEN + "New tax " + taxName + " created successfully.");
	}
	
	public void deleteTax(CommandSender sender, String name) {
		String taxName = name;
		
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		
		if(taxes.contains(taxName)) {
			taxes.remove(taxName);
			conf.setProperty("taxes.list", taxes);
			conf.removeProperty("taxes." + taxName);
			conf.save();
			
			sender.sendMessage(ChatColor.GREEN + "Tax " + taxName + " deleted successfully.");
		} else {
			sender.sendMessage(ChatColor.RED + "No tax, " + taxName);
		}
		
		return;
	}
	
	public void taxInfo(CommandSender sender, String taxName) {
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		
		if(taxes.contains(taxName)) {
			sender.sendMessage(ChatColor.BLUE + "Percent income: " + conf.getString("taxes." + taxName + ".percentIncome") + "%");
			sender.sendMessage(ChatColor.BLUE + "Interval: " + conf.getString("taxes." + taxName + ".taxInterval") + " hours");
			sender.sendMessage(ChatColor.BLUE + "Receiving player: " + conf.getString("taxes." + taxName + ".receiver"));
			sender.sendMessage(ChatColor.BLUE + "Paying groups: " + conf.getStringList("taxes." + taxName + ".payingGroups", null));
		} else {
			sender.sendMessage(ChatColor.RED + "No tax, " + taxName);
		}
		
		return;
	}
	
	public void listTaxes(CommandSender sender) {
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		iterator = taxes.listIterator();
		
		if(taxes.size() != 0) {
			while(iterator.hasNext()) {
				sender.sendMessage(ChatColor.BLUE + iterator.next());
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No taxes to list.");
		}
	}
	
	@SuppressWarnings("unused")
	public boolean isPlayer(String playerName) {
		if(TaxManager.cashFlow.getServer().getPlayer(playerName) != null) {
			return true;
		} else {
			try {
				FileInputStream test = new FileInputStream("world/players/" + playerName + ".dat");
			} catch (FileNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	public void addTaxpayer(CommandSender sender, String taxName, String groupName) {
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		payingGroups = conf.getStringList("taxes." + taxName + ".payingGroups", null);
		
		if(!(taxes.contains(taxName))) {
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		/*
		else if(!(TaxManager.cashFlow.permsManager.isGroup(groupName))){
			sender.sendMessage(ChatColor.RED + "Group not found.");
		}
		*/ 
		else {
			sender.sendMessage(ChatColor.GREEN + taxName + " applied successfully to " + groupName);
			payingGroups.add(groupName);
			conf.setProperty("taxes." + taxName + ".payingGroups", payingGroups);
			conf.save();
		}
		
		return;
	}
	
	public void removeTaxpayer(CommandSender sender, String taxName, String groupName) {
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		payingGroups = conf.getStringList("taxes." + taxName + ".payingGroups", null);
		
		if(!(taxes.contains(taxName))) {
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		} else if(!(payingGroups.contains(groupName))) {
			sender.sendMessage(ChatColor.RED + "Group not found.");
		} else {
			sender.sendMessage(ChatColor.GREEN + taxName + " removed successfully from " + groupName);
			payingGroups.remove(groupName);
			conf.setProperty("taxes." + taxName + ".payingGroups", payingGroups);
			conf.save();
		}
	}

	public void startTax() {		
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		Double hours;
		Date lastPaid;
		
		for(String taxName : taxes) {
			hours = Double.parseDouble(conf.getString("taxes." + taxName + ".taxInterval"));
			lastPaid = (Date) conf.getProperty("taxes." + taxName + ".lastPaid");
			System.out.println(conf.getProperty("taxes." + taxName + ".lastPaid"));
			Taxer taxer = new Taxer(this, taxName, hours, lastPaid);
			taxTasks.add(taxer);
		}
	}
	
	public void payTax(String taxName) {
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		
		conf.setProperty("taxes." + taxName + ".lastPaid", new Date());
		System.out.println(conf.getProperty("taxes." + taxName + ".lastPaid"));
		conf.save();
		
		
		System.out.println(taxName);
	}
	
	public void disableTax() {
		for(Taxer taxTask : taxTasks) {
			//taxTask.TaxTask.cancel();
		}
	}
	
}
