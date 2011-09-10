package mveritym.cashflow;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Taxer {
	
	private TaxManager taxManager;
	private String taxName;
	private Double hours;
	private Timer timer;
	private Date lastPaid;
	private Boolean first;

	public Taxer(TaxManager taxManager, String taxName, Double hours, Date lastPaid) {
		this.taxManager = taxManager;
		this.taxName = taxName;
		this.hours = hours;
		this.first = true;
		this.lastPaid = lastPaid;
		
		if(this.lastPaid == null) {
			System.out.println("Setting new date.");
			this.lastPaid = new Date();
			TaxManager.conf.setProperty("taxes." + taxName + ".lastPaid", this.lastPaid);
			TaxManager.conf.save();
		}
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TaxTask(), this.lastPaid, Math.round(this.hours * 3600000));
	}
	
	public void cancel() {
		this.timer.cancel();
	}

    class TaxTask extends TimerTask {
        public void run() {
        	if(first) {
        		first = false;
        	} else {
        		System.out.format("Paying!");
        		taxManager.payTax(taxName);
        	}
        }
    }

}
