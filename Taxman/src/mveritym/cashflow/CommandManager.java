package mveritym.cashflow;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class CommandManager {

	private final CashFlow cashFlow;
	private final TaxManager taxManager;
	private final SalaryManager salaryManager;

	public CommandManager(CashFlow cashFlow, TaxManager taxManager, SalaryManager salaryManager) {
		this.cashFlow = cashFlow;
		this.taxManager = taxManager;
		this.salaryManager = salaryManager;
	}

	//TODO add a more friendly help menu
	public boolean taxCommand(CommandSender sender, String[] tempArgs) {
		CashFlowCommands cmd;

		try {
			cmd = CashFlowCommands.valueOf(tempArgs[0]);
		} catch(Exception e) {
			return false;
		}

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

				this.taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
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

				this.taxManager.createTax(sender, name, percentOfBal, interval, receiverName);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case delete:
			if(args.length == 1) {
				this.taxManager.deleteTax(sender, args[0]);
				return true;
			} else if(args.length > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments.");
				return false;
			} else {
				sender.sendMessage(ChatColor.RED + "Not enough arguments.");
				return false;
			}
		case apply:
			if(args.length >= 3) {
				if(args[0].equals("group")) {
					if(this.cashFlow.permsManager.pluginDetected()) {
						this.taxManager.addGroups(sender, args[1], args[2]);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "You must install a permissions plugin to use this command.");
						return true;
					}
				} else if(args[0].equals("player")) {
					this.taxManager.addPlayers(sender, args[1], args[2]);
					return true;
				}
				sender.sendMessage(ChatColor.RED + "Incorrect argument.");
				return false;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case remove:
			if(args.length >= 3) {
				if(args[0].equals("group")) {
					if(this.cashFlow.permsManager.pluginDetected()) {
						this.taxManager.removeGroups(sender, args[1], args[2]);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "You must install a permissions plugin to use this command.");
						return true;
					}
				} else if(args[0].equals("player")) {
					this.taxManager.removePlayers(sender, args[1], args[2]);
					return true;
				}
				sender.sendMessage(ChatColor.RED + "Incorrect argument.");
				return false;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case addexception:
			if(args.length == 2) {
				this.taxManager.addException(sender, args[0], args[1]);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case removeexception:
			if(args.length == 2) {
				this.taxManager.removeException(sender, args[0], args[1]);
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
				this.taxManager.listTaxes(sender);
				return true;
			}
		case info:
			if(args.length == 1) {
				this.taxManager.taxInfo(sender, args[0]);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				return false;
			}
		case setonlineonly:
			if(args.length == 2 || args.length == 3) {
				if(args[1].equals("true") || args[1].equals("false")) {
					Double onlineInterval = 0.0;
					if(args.length == 3) {
						onlineInterval = Math.abs(Double.parseDouble(args[2]));
					}
					this.taxManager.setOnlineOnly(args[0], Boolean.parseBoolean(args[1]), onlineInterval);
					sender.sendMessage(ChatColor.GREEN + "Online only set to " + args[1]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Online only can only be set to true or false.");
					return false;
				}
			} else {
				return false;
			}
		case setrate:
			if(args.length == 2) {
				String taxName = args[0];
				String tax = args[1];
				this.taxManager.setRate(sender, taxName, tax);
				return true;
			} else {
				return false;
			}
		case fire:
			if(args.length == 1)
			{
				String taxName = args[0];
				if(cashFlow.getPluginConfig().getStringList("taxes.list").contains(taxName))
				{
					this.cashFlow.log.info(this.cashFlow.prefix
		    				+ " Paying tax " + taxName);
					this.taxManager.payTax(taxName);
					return true;
				}
				else
				{
					sender.sendMessage(ChatColor.RED + " Invalid tax name: " + taxName);
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + " No tax name given");
			}
			return false;
		default:
			return false;
		}

	}

	//TODO ability to enable/disable specific salaries
	//TODO get current state of a salary
	public boolean salaryCommand(CommandSender sender, String[] tempArgs) {
		CashFlowCommands cmd;

		try {
			cmd = CashFlowCommands.valueOf(tempArgs[0]);
		} catch(Exception e) {
			return false;
		}

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

					this.salaryManager.createSalary(sender, name, salary, interval, employer);
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

				    this.salaryManager.createSalary(sender, name, salary, interval, employer);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case delete:
				if(args.length == 1) {
					this.salaryManager.deleteSalary(sender, args[0]);
					return true;
				} else if(args.length > 1) {
					sender.sendMessage(ChatColor.RED + "Too many arguments.");
					return false;
				} else {
					sender.sendMessage(ChatColor.RED + "Not enough arguments.");
					return false;
				}
			case apply:
				if(args.length >= 3) {
					if(args[0].equals("group")) {
						if(this.cashFlow.permsManager.pluginDetected()) {
							this.salaryManager.addGroups(sender, args[1], args[2]);
							return true;
						} else {
							sender.sendMessage(ChatColor.RED + "You must install a permissions plugin to use this command.");
							return true;
						}
					} else if(args[0].equals("player")) {
						this.salaryManager.addPlayers(sender, args[1], args[2]);
						return true;
					}
					sender.sendMessage(ChatColor.RED + "Incorrect argument.");
					return false;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case remove:
				if(args.length >= 3) {
					if(args[0].equals("group")) {
						if(this.cashFlow.permsManager.pluginDetected()) {
							this.salaryManager.removeGroups(sender, args[1], args[2]);
							return true;
						} else {
							sender.sendMessage(ChatColor.RED + "You must install a permissions plugin to use this command.");
							return true;
						}
					} else if(args[0].equals("player")) {
						this.salaryManager.removePlayers(sender, args[1], args[2]);
						return true;
					}
					sender.sendMessage(ChatColor.RED + "Incorrect argument.");
					return false;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case addexception:
				if(args.length == 2) {
					this.salaryManager.addException(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case removeexception:
				if(args.length == 2) {
					this.salaryManager.removeException(sender, args[0], args[1]);
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
					this.salaryManager.listSalaries(sender);
					return true;
				}
			case info:
				if(args.length == 1) {
					this.salaryManager.salaryInfo(sender, args[0]);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			case setonlineonly:
				if(args.length == 2 || args.length == 3) {
					if(args[1].equals("true") || args[1].equals("false")) {
						Double onlineInterval = 0.0;
						if(args.length == 3) {
							onlineInterval = Math.abs(Double.parseDouble(args[2]));
						}
						this.salaryManager.setOnlineOnly(args[0], Boolean.parseBoolean(args[1]), onlineInterval);
						sender.sendMessage(ChatColor.GREEN + "Online only set to " + args[1]);
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Online only can only be set to true or false.");
						return false;
					}
				} else {
					return false;
				}
			case setrate:
				if(args.length == 2) {
					String salaryName = args[0];
					String salaryrate = args[1];
					this.salaryManager.setRate(sender, salaryName, salaryrate);
					return true;
				} else {
					return false;
				}
			case fire:
				if(args.length == 1)
				{
					String salaryName = args[0];
					if(cashFlow.getPluginConfig().getStringList("salaries.list").contains(salaryName))
					{
						this.cashFlow.log.info(this.cashFlow.prefix
			    				+ " Paying salary " + salaryName);
						this.salaryManager.paySalary(salaryName);
						return true;
					}
					else
					{
						sender.sendMessage(ChatColor.RED + " Invalid salary name: " + salaryName);
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + " No salary name given");
				}
				return false;
			default:
				return false;
		}
	}

	public boolean cashflowCommand(CommandSender sender, String[] tempArgs) {
		CashFlowCommands cmd;

		try {
			cmd = CashFlowCommands.valueOf(tempArgs[0]);
		} catch(Exception e) {
			return false;
		}

		String args[] = new String[tempArgs.length - 1];
		for(int i = 1; i < tempArgs.length; i++) {
			args[i-1] = tempArgs[i];
		}

		switch(cmd) {
			case enable:
				if(args.length == 0) {
					this.taxManager.enable();
					this.salaryManager.enable();
					Buffer.getInstance().start();
					sender.sendMessage(ChatColor.GREEN + "Taxes and salaries enabled.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
					return false;
				}
			case disable:
				if(args.length == 0) {
					this.taxManager.disable();
					this.salaryManager.disable();
					Buffer.getInstance().cancelBuffer();
					sender.sendMessage(ChatColor.GREEN + "Taxes and salaries disabled.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
					return false;
				}
			case restart:
				if(args.length == 0) {
					this.taxManager.disable();
					this.salaryManager.disable();
					Buffer.getInstance().cancelBuffer();
					this.taxManager.enable();
					this.salaryManager.enable();
					Buffer.getInstance().start();
					sender.sendMessage(ChatColor.GREEN + "Taxes and salaries restarted.");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Command takes no arguments.");
					return true;
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
			case addplayers:
				if(args.length == 1)
				{
					String worldName = args[0];
					World w = this.cashFlow.getServer().getWorld(worldName);
					if(w != null)
					{
						this.cashFlow.permsManager.importPlayers(worldName);
						sender.sendMessage(ChatColor.GREEN + "Imported players of world '" + worldName + "' into master database.");
						return true;
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "World not found.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "World name not given.");
				}
			case status:
				for(Taxer t : this.taxManager.taxTasks)
				{
					sender.sendMessage(t.getName() + " : " + t.getState());
				}
				for(Taxer t : this.salaryManager.salaryTasks)
				{
					sender.sendMessage(t.getName() + " : " + t.getState());
				}
				sender.sendMessage("Ops in Buffer : " + Buffer.getInstance().size());
				return true;
			default:
				return false;
		}
	}

}