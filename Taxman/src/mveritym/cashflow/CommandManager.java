package mveritym.cashflow;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandManager {

	private CashFlow cashFlow;
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	
	public CommandManager(CashFlow cashFlow) {
		this.cashFlow = cashFlow;
		this.taxManager = new TaxManager(this.cashFlow);
		this.salaryManager = new SalaryManager(this.cashFlow);
	}
	
	public boolean taxCommand(CommandSender sender, String[] tempArgs) {
		CashFlowCommands cmd = CashFlowCommands.valueOf(tempArgs[0]);
		
		String args[] = new String[tempArgs.length - 1];
		for(int i = 1; i < tempArgs.length; i++) {
			args[i-1] = tempArgs[i];
		}

		switch(cmd) {
		case create:
			String name;
			String percentOfBal;
			String interval;
			String receiverName;

			if(args.length == 4) {
				name = args[0];
				percentOfBal = args[1];
				interval = args[2];
				receiverName = args[3];
				
				taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
				return true;
			} else if(args.length == 3) {
				try {
					@SuppressWarnings("unused")
					double testDouble = Double.parseDouble(args[2]);
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}

				name = args[0];
				percentOfBal = args[1];
				interval = args[2];
				receiverName = "null";

				taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case delete:
			if(args.length == 1) {
				taxManager.deleteTax(sender, args[0]);
				return true;
			} else if(args.length > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments.");
				return false;
			} else {
				sender.sendMessage(ChatColor.RED + "Not enough arguments.");
				return false;
			}
		case apply:
			if(args.length == 2) {
				taxManager.addTaxpayer(sender, args[0], args[1]);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case remove:
			if(args.length == 2) {
				taxManager.removeTaxpayer(sender, args[0], args[1]);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case list:
			if(args.length != 0) {
				sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
				return false;
			} else {
				taxManager.listTaxes(sender);
				return true;
			}	
		case info:
			if(args.length == 1) {
				taxManager.taxInfo(sender, args[0]);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		default:
			return false;
		}
	
}
	
	public boolean salaryCommand(CommandSender sender, String[] tempArgs) {
		CashFlowCommands cmd = CashFlowCommands.valueOf(tempArgs[0]);
		
		String args[] = new String[tempArgs.length - 1];
		for(int i = 1; i < tempArgs.length; i++) {
			args[i-1] = tempArgs[i];
		}
	
		switch(cmd) {
			case create:
				String name;
				String salary;
				String interval;
				String employer;

				if(args.length == 4) {
					name = args[0];
					salary = args[1];
					interval = args[2];
					employer = args[3];
					
					salaryManager.createSalary(sender, name, salary, interval, employer);
					return true;
				} else if(args.length == 3) {
					try {
						@SuppressWarnings("unused")
						double testDouble = Double.parseDouble(args[2]);
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
						return false;
					}

					name = args[0];
					salary = args[1];
					interval = args[2];
					employer = "null";

				    salaryManager.createSalary(sender, name, salary, interval, employer);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case delete:
				if(args.length == 1) {
					salaryManager.deleteSalary(sender, args[0]);
					return true;
				} else if(args.length > 1) {
					sender.sendMessage(ChatColor.RED + "Too many arguments.");
					return false;
				} else {
					sender.sendMessage(ChatColor.RED + "Not enough arguments.");
					return false;
				}
			case list:
				if(args.length != 0) {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
					return false;
				} else {
					salaryManager.listSalaries(sender);
					return true;
				}	
			case info:
				if(args.length == 1) {
					salaryManager.salaryInfo(sender, args[0]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			default:
				return false;
		}
	}
	
	public void enable() {
		taxManager.enable();
	}
	
	public void disable() {
		taxManager.disable();
	}
	
	public void restart() {
		taxManager.disable();
		taxManager.enable();
	}
}