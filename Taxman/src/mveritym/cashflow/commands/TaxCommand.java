package mveritym.cashflow.commands;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.PermissionsManager;
import mveritym.cashflow.taxer.TaxManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class TaxCommand implements CommandExecutor {
	private CashFlow cashFlow;
	private TaxManager taxManager;
	private PermissionsManager permsManager;

	public TaxCommand (CashFlow plugin, PermissionsManager perm, TaxManager tax)
	{
		cashFlow = plugin;
		taxManager = tax;
		permsManager = perm;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		boolean playerCanDo = false;
		boolean isConsole = false;
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			String node = "cashflow." + command.getName();
			if (player.isOp()
					|| permsManager.hasPermission(player, node))
			{
				playerCanDo = true;
			}
		}
		else if (sender instanceof ConsoleCommandSender)
		{
			isConsole = true;
		}
		if (!playerCanDo && !isConsole)
		{
			sender.sendMessage(ChatColor.RED + cashFlow.prefix
					+ " Lack permission: cashflow." + command.getName());
			return true;
		}
		if(args.length == 0)
		{
			//No arguments given
			showHelp(sender);
		}
		else
		{
			try
			{
			final CashFlowCommands com = CashFlowCommands.valueOf(args[0].toLowerCase());
			switch(com)
			{
				case create:
					String name;
					String percentOfBal;
					String interval;
					String receiverName;

					if (args.length == 5)
					{
						name = args[1];
						percentOfBal = args[2];
						interval = args[3];
						receiverName = args[4];

						this.taxManager.createTax(sender, name, percentOfBal,
								interval, receiverName);
					}
					else if (args.length == 4)
					{
						try
						{
							@SuppressWarnings ("unused")
							double testDouble = Double.parseDouble(args[3]);
							name = args[1];
							percentOfBal = args[2];
							interval = args[3];
							receiverName = "null";

							this.taxManager.createTax(sender, name, percentOfBal,
								interval, receiverName);
						}
						catch (Exception e)
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix
									+ " Invalid interval.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case delete:
					if (args.length >= 2)
					{
						for(int i = 1; i < args.length; i++)
						{
							this.taxManager.deleteTax(sender, args[i]);
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix + " No tax name given.");
					}
					break;
				case apply:
					if (args.length >= 4)
					{
						final String com2 = args[1].toLowerCase();
						if (com2.equals("group"))
						{
							if (this.cashFlow.permsManager.pluginDetected())
							{
								this.taxManager.addGroups(sender, args[2], args[3]);
							}
							else
							{
								sender.sendMessage(ChatColor.RED + cashFlow.prefix
										+ " You must install a permissions plugin to use this command.");
							}
						}
						else if (com2.equals("player"))
						{
							this.taxManager.addPlayers(sender, args[2], args[3]);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix + " Incorrect argument.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case remove:
					if (args.length >= 4)
					{
						final String com2 = args[1].toLowerCase();
						if (com2.equals("group"))
						{
							if (this.cashFlow.permsManager.pluginDetected())
							{
								this.taxManager.removeGroups(sender, args[2],
										args[3]);
							}
							else
							{
								sender.sendMessage(ChatColor.RED + cashFlow.prefix
										+ " You must install a permissions plugin to use this command.");
							}
						}
						else if (com2.equals("player"))
						{
							this.taxManager.removePlayers(sender, args[2], args[3]);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix  + " Incorrect argument.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case addexception:
					if (args.length == 3)
					{
						this.taxManager.addException(sender, args[1], args[2]);
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case removeexception:
					if (args.length == 3)
					{
						this.taxManager.removeException(sender, args[1], args[2]);
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case list:
						this.taxManager.listTaxes(sender);
						break;
				case info:
					if (args.length == 2)
					{
						this.taxManager.taxInfo(sender, args[1]);
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case setonlineonly:
					if (args.length == 3 || args.length == 4)
					{
						final String booleanString = args[2].toLowerCase();
						if (booleanString.equals("true") || booleanString.equals("false"))
						{
							Double onlineInterval = 0.0;
							if (args.length == 4)
							{
								onlineInterval = Math.abs(Double
										.parseDouble(args[3]));
							}
							this.taxManager.setOnlineOnly(args[1],
									Boolean.parseBoolean(booleanString), onlineInterval);
							sender.sendMessage(ChatColor.GREEN + cashFlow.prefix
									+ " Online only set to " + ChatColor.GRAY+ args[1]);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix
									+ " Online only can only be set to true or false.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case setrate:
					if (args.length == 3)
					{
						String taxName = args[1];
						String tax = args[2];
						this.taxManager.setRate(sender, taxName, tax);
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix
								+ " Incorrect number of arguments.");
					}
					break;
				case fire:
					if (args.length == 2)
					{
						String taxName = args[1];
						if (cashFlow.getPluginConfig().getStringList("taxes.list")
								.contains(taxName))
						{
							this.cashFlow.log.info(cashFlow.prefix
									+ " Paying tax " + taxName);
							sender.sendMessage(cashFlow.prefix
									+ " Paying tax " + taxName);
							this.taxManager.payTax(taxName);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix
									+ " Invalid tax name: " + taxName);
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix + " No tax name given");
					}
					break;
				case enable:
					if (args.length == 2)
					{
						String taxName = args[1];
						if (cashFlow.getPluginConfig().getStringList("taxes.list")
								.contains(taxName))
						{
							sender.sendMessage(ChatColor.GREEN + cashFlow.prefix + " Enabling tax - "
									+ ChatColor.GOLD + taxName);
							this.taxManager.enableTax(taxName);
						}
						else
						{
							sender.sendMessage(ChatColor.RED
									+ cashFlow.prefix + " Invalid tax name: " + taxName);
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix + " No tax name given");
					}
					break;
				case disable:
					if (args.length == 2)
					{
						String taxName = args[1];
						if (cashFlow.getPluginConfig().getStringList("taxes.list")
								.contains(taxName))
						{

							sender.sendMessage(ChatColor.YELLOW
									+ cashFlow.prefix + " Disabling tax - " + ChatColor.AQUA + taxName);
							this.taxManager.disableTax(taxName);
						}
						else
						{
							sender.sendMessage(ChatColor.RED
									+ cashFlow.prefix + " Invalid tax name: " + taxName);
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + cashFlow.prefix + " No tax name given");
					}
					break;
				default:
					showHelp(sender);
					break;
			}
			}catch (IllegalArgumentException e)
			{
				sender.sendMessage(ChatColor.RED + cashFlow.prefix
						+ " Syntax error. For help, use /tax");
			}
		}
		return true;
	}

	private void showHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "=====" + ChatColor.WHITE
				+ "Tax Help" + ChatColor.GREEN + "=====");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"create <taxname> <tax" +ChatColor.LIGHT_PURPLE + "[%]" + ChatColor.YELLOW + "> <interval> " + ChatColor.LIGHT_PURPLE + "[payee] " + ChatColor.GREEN+"- Creates a tax.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"delete <taxname> " + ChatColor.GREEN+"- Deletes a tax.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"<apply/remove> <group/player> <taxname> <group/player names> " + ChatColor.GREEN+"- Applies a tax to group(s) or player(s). For multiple entries, use commas.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"addexception <taxname> <playername> " + ChatColor.GREEN+"- Adds a player as an exception. Case sensitive.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"removeexception <taxname> <playername>" + ChatColor.GREEN+"- Removes a player as an exception. Case sensitive.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"list" + ChatColor.GREEN+"- Lists all taxes.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"info <taxname> " + ChatColor.GREEN+"- Lists info on given tax.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"setonlineonly <taxname> <true/false> [interval]" + ChatColor.GREEN+"- Sets specified tax to be collected only for players online in the given hourly interval.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"setrate <taxname> <tax>" + ChatColor.GREEN+"- Sets the tax rate.");
		sender.sendMessage(ChatColor.AQUA + "/tax " + ChatColor.YELLOW +"<enable/disable> <taxname> " + ChatColor.GREEN+"- Enable or disable a tax.");
	}

}
