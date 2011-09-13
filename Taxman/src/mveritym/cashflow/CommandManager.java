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
		case addexception:
			if(args.length == 2) {
				taxManager.addException(sender, args[0], args[1]);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case removeexception:
			if(args.length == 2) {
				taxManager.removeException(sender, args[0], args[1]);
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
			case apply:
				if(args.length == 2) {
					salaryManager.applySalary(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case remove:
				if(args.length == 2) {
					salaryManager.removeSalary(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case addexception:
				if(args.length == 2) {
					salaryManager.addException(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case removeexception:
				if(args.length == 2) {
					salaryManager.removeException(sender, args[0], args[1]);
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
	
	public boolean cashflowCommand(CommandSender sender, String[] tempArgs) {
		CashFlowCommands cmd = CashFlowCommands.valueOf(tempArgs[0]);
		
		String args[] = new String[tempArgs.length - 1];
		for(int i = 1; i < tempArgs.length; i++) {
			args[i-1] = tempArgs[i];
		}
		
		switch(cmd) {
			case enable:
				if(args.length == 0) {
					taxManager.enable();
					salaryManager.enable();
					sender.sendMessage(ChatColor.GREEN + "Taxes and salaries enabled.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
					return false;
				}
			case disable:
				if(args.length == 0) {
					taxManager.disable();
					salaryManager.disable();
					sender.sendMessage(ChatColor.GREEN + "Taxes and salaries disabled.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
					return false;
				}
			case restart:
				if(args.length == 0) {
					taxManager.disable();
					salaryManager.disable();
					taxManager.enable();
					salaryManager.enable();
					sender.sendMessage(ChatColor.GREEN + "Taxes and salaries restarted.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
				}
			case setworld:
				if(args.length == 1) {
					if(this.cashFlow.permsManager.setWorld(args[0])) {
						sender.sendMessage(ChatColor.GREEN + "World set.");
					} else {
						sender.sendMessage(ChatColor.RED + "World not found.");
					}
					return true;
				} else {
					return false;
				}
			default:
				return false;
		}
	}
	
}