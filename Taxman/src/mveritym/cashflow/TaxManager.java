package mveritym.cashflow;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class TaxManager {
	protected static CashFlow cashFlow;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    HashMap<String, String[]> taxes = new LinkedHashMap<String, String[]>();
    
    public TaxManager(CashFlow cashFlow) {
    	TaxManager.cashFlow = cashFlow;
    	
    	File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
    	conf = null;

        if (f.exists())
        {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);            
            conf.save();
        }
        
        f = new File(TaxManager.cashFlow.getDataFolder(), "users.yml");
        
        if (f.exists())
        {
            uconf = new Configuration(f);
            uconf.load();
        }
        else {
            this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "users.yml");
            TaxManager.uconf = new Configuration(confFile);
            uconf.save();
        }
    }
    
	public void createTax(CommandSender sender, String name, String percentOfBal, String interval, String taxReceiver) {
		String taxName = name;
		double percentIncome = Double.parseDouble(percentOfBal);
		double taxInterval = Double.parseDouble(interval);
		Player receiver = cashFlow.getServer().getPlayer(taxReceiver);
	
		if(taxes.containsKey(taxName)) {
			sender.sendMessage(ChatColor.RED + "A tax with that name has already been created.");
			return;
		} else {
			String[] taxValues = new String[3];
			taxValues[0] = percentOfBal;
			taxValues[1] = taxReceiver;
			taxValues[2] = interval;
			taxes.put(name, taxValues);
			
			conf.setProperty(taxName + ".percentIncome", percentIncome);
			conf.setProperty(taxName + ".taxInterval", taxInterval);
			conf.setProperty(taxName + ".receiver", receiver);
		}
	}
}
