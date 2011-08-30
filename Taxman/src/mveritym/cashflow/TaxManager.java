package mveritym.cashflow;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class TaxManager {
	protected static CashFlow cashFlow;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    List<String> taxes;
    ListIterator<String> iterator;
    
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
        
        /*
        f = new File(TaxManager.cashFlow.getDataFolder(), "taxData.bin");
        
        if (f.exists()) {
        	TaxManager.cashFlow.log.info("Loading CashFlow tax data.");
        	try {
				taxes = (HashMap<String, String[]>)SLAPI.load("taxData.bin");
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
        	TaxManager.cashFlow.log.info("No CashFlow tax data found. Creating data file.");
        	try {
				SLAPI.save(taxes, "plugins/CashFlow/taxData.bin");
			} catch (Exception e) {
				e.printStackTrace();
			} 
        }
        */
    }
    
    public void loadConf() {
    	File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");

        if (f.exists()) {
        	TaxManager.cashFlow.log.info("CashFlow config file loaded.");
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	TaxManager.cashFlow.log.info("No CashFlow config file found. Creating config file.");
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);  
            List<String> tempList = null;
            conf.setProperty("taxes.list", tempList);
            conf.save();
        }
    }
    
	public void createTax(CommandSender sender, String name, String percentOfBal, String interval, String taxReceiver) {
		TaxManager.cashFlow.log.info("Creating new tax " + name + ".");
		String taxName = name;
		double percentIncome = Double.parseDouble(percentOfBal);
		double taxInterval = Double.parseDouble(interval);
		Player receiver = cashFlow.getServer().getPlayer(taxReceiver);
		
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		iterator = taxes.listIterator();
		
		if(!checkArguments()) {
			return;
		}
		
		while(iterator.hasNext()) {
			if(iterator.next().equals(taxName)) {
				sender.sendMessage(ChatColor.RED + "A tax with that name has already been created.");
				TaxManager.cashFlow.log.info("Failed to create new tax - tax already exists.");
				return;
			}
		}
		
		taxes.add(taxName);	
		conf.setProperty("taxes.list", taxes);
		conf.setProperty("taxes." + taxName + ".percentIncome", percentIncome);
		conf.setProperty("taxes." + taxName + ".taxInterval", taxInterval);
		conf.setProperty("taxes." + taxName + ".receiver", receiver);
		conf.save();		
		
		TaxManager.cashFlow.log.info("New tax " + taxName + " created successfully.");
		sender.sendMessage(ChatColor.GREEN + "New tax " + taxName + " created successfully.");
	}
	
	public void deleteTax(CommandSender sender, String name) {
		String taxName = name;
		
		loadConf();
		taxes = conf.getStringList("taxes.list", null);
		iterator = taxes.listIterator();
		
		while(iterator.hasNext()) {
			if(iterator.next().equals(taxName)) {
				taxes.remove(taxName);
				conf.setProperty("taxes.list", taxes);
				conf.removeProperty("taxes." + taxName);
				conf.save();
				
				TaxManager.cashFlow.log.info("Tax " + taxName + " deleted successfully.");
				sender.sendMessage(ChatColor.GREEN + "Tax " + taxName + " deleted successfully.");
				return;
			}
		}
		
		TaxManager.cashFlow.log.info("No tax, " + taxName);
		sender.sendMessage(ChatColor.RED + "No tax, " + taxName);
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
	
	public boolean checkArguments() {
		return true;
	}
}
