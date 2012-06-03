package mveritym.cashflow.commands;

import java.util.EnumMap;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.LocalString;
import mveritym.cashflow.LocalString.Flag;
import mveritym.cashflow.database.Buffer;
import mveritym.cashflow.managers.SalaryManager;
import mveritym.cashflow.managers.TaxManager;
import mveritym.cashflow.permissions.PermissionNode;
import mveritym.cashflow.permissions.PermissionsManager;
import mveritym.cashflow.tasks.CFTask;
import mveritym.cashflow.tasks.ImportPlayersTask;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CashFlowCommand implements CommandExecutor
{
	private CashFlow plugin;
	private TaxManager taxManager;
	private SalaryManager salaryManager;

	public CashFlowCommand(CashFlow plugin, TaxManager tax, SalaryManager salary)
	{
		this.plugin = plugin;
		taxManager = tax;
		salaryManager = salary;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args)
	{
		final EnumMap<LocalString.Flag, String> info = new EnumMap<LocalString.Flag, String>(
				LocalString.Flag.class);
		info.put(Flag.TAG, CashFlow.TAG);
		if (!PermissionsManager.hasPermission(sender, PermissionNode.BASIC))
		{
			info.put(Flag.EXTRA, PermissionNode.BASIC.getNode());
			sender.sendMessage(LocalString.PERMISSION_DENY.parseString(info));
			return true;
		}
		if (args.length == 0)
		{
			// No arguments given
			showHelp(sender);
			return true;
		}
		try
		{
			final CFCommand com = CFCommand.valueOf(args[0].toLowerCase());
			parseCommand(com, sender, args);
		}
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + CashFlow.TAG
					+ " Syntax error. For help, use /cashflow");
		}
		return true;
	}

	private void parseCommand(CFCommand com, CommandSender sender, String[] args)
	{
		switch (com)
		{
			case enable:
				this.taxManager.enable();
				this.salaryManager.enable();
				Buffer.getInstance().start();
				sender.sendMessage(ChatColor.YELLOW + CashFlow.TAG
						+ " Taxes and salaries " + ChatColor.GREEN + "enabled.");
				break;
			case disable:
				this.taxManager.disable();
				this.salaryManager.disable();
				Buffer.getInstance().cancelBuffer();
				sender.sendMessage(ChatColor.YELLOW + CashFlow.TAG
						+ " Taxes and salaries " + ChatColor.RED + "disabled.");
				break;
			case restart:
				this.taxManager.disable();
				this.salaryManager.disable();
				Buffer.getInstance().cancelBuffer();
				this.taxManager.enable();
				this.salaryManager.enable();
				Buffer.getInstance().start();
				sender.sendMessage(ChatColor.YELLOW + CashFlow.TAG
						+ " Taxes and salaries restarted.");
				break;
			case setworld:
				if (args.length == 2)
				{
					if (PermissionsManager.setWorld(args[1]))
					{
						sender.sendMessage(ChatColor.GREEN + CashFlow.TAG
								+ " World set to: " + ChatColor.GRAY + args[1]);
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + CashFlow.TAG
								+ " World " + ChatColor.GRAY + args[1]
								+ ChatColor.YELLOW + " not found.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + CashFlow.TAG
							+ " World not given.");
				}
				break;
			case addplayers:
				if (args.length == 2)
				{
					String worldName = args[1];
					World w = this.plugin.getServer().getWorld(worldName);
					if (w != null)
					{
						plugin.getServer()
								.getScheduler()
								.scheduleAsyncDelayedTask(
										plugin,
										new ImportPlayersTask(plugin, sender,
												worldName));
						sender.sendMessage(ChatColor.YELLOW + CashFlow.TAG
								+ " Importing players of world '"
								+ ChatColor.GRAY + worldName + ChatColor.YELLOW
								+ "' into master database...");
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + CashFlow.TAG
								+ " World '" + ChatColor.GRAY + worldName
								+ ChatColor.YELLOW + "' not found.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + CashFlow.TAG
							+ " World name not given.");
				}
				break;
			case status:
				sender.sendMessage(ChatColor.GRAY + "=====" + ChatColor.WHITE
						+ "CashFlow Status" + ChatColor.GRAY + "=====");
				for (CFTask t : this.taxManager.taxTasks)
				{
					sender.sendMessage(ChatColor.RED + t.getName()
							+ ChatColor.GRAY + " : " + t.getState());
				}
				for (CFTask t : this.salaryManager.salaryTasks)
				{
					sender.sendMessage(ChatColor.GREEN + t.getName()
							+ ChatColor.GRAY + " : " + t.getState());
				}
				sender.sendMessage("Ops in Buffer : "
						+ Buffer.getInstance().size());
				break;
			default:
				showHelp(sender);
				break;

		}
	}

	/**
	 * Show help on cashflow command to sender
	 * 
	 * @param sender
	 */
	private void showHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "=====" + ChatColor.WHITE
				+ "CashFlow" + ChatColor.GREEN + "=====");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW
				+ "enable " + ChatColor.GREEN
				+ "- Enables payment of taxes and salaries.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW
				+ "disable " + ChatColor.GREEN
				+ "- Disables payment of taxes and salaries.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW
				+ "restart " + ChatColor.GREEN
				+ "- Updates all taxes and salaries. Restarts time intervals.");
		sender.sendMessage(ChatColor.AQUA
				+ "/cashflow "
				+ ChatColor.YELLOW
				+ "status "
				+ ChatColor.GREEN
				+ "- Check status of all taxes and salaries and buffered operations.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW
				+ "setworld " + ChatColor.GREEN
				+ "- Specifies world name. Legacy: ONLY for v1.0.0 and lower.");
		sender.sendMessage(ChatColor.AQUA
				+ "/cashflow "
				+ ChatColor.YELLOW
				+ "addplayers <world> "
				+ ChatColor.GREEN
				+ "- Imports player.dat files from specified world to the internal database.");
	}

}
