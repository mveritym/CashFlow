package mveritym.cashflow;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CashFlowCommand implements CommandExecutor {
	private CashFlow cashFlow;
	private TaxManager taxManager;
	private SalaryManager salaryManager;
	private PermissionsManager permsManager;

	public CashFlowCommand(CashFlow cf, PermissionsManager perm,
			TaxManager tax, SalaryManager salary) {
		cashFlow = cf;
		taxManager = tax;
		salaryManager = salary;
		permsManager = perm;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		boolean playerCanDo = false;
		boolean isConsole = false;
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			String node = "cashflow.basic";
			if (player.isOp() || permsManager.hasPermission(player, node))
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
					+ " Lack permission: cashflow.basic");
			return true;
		}
		if (args.length == 0)
		{
			// No arguments given
			showHelp(sender);
		}
		else
		{
			try
			{
				final CashFlowCommands com = CashFlowCommands.valueOf(args[0]
						.toLowerCase());
				switch (com)
				{
					case enable:
						this.taxManager.enable();
						this.salaryManager.enable();
						Buffer.getInstance().start();
						sender.sendMessage(ChatColor.YELLOW + cashFlow.prefix
								+ " Taxes and salaries " + ChatColor.GREEN
								+ "enabled.");
						break;
					case disable:
						this.taxManager.disable();
						this.salaryManager.disable();
						Buffer.getInstance().cancelBuffer();
						sender.sendMessage(ChatColor.YELLOW + cashFlow.prefix
								+ " Taxes and salaries " + ChatColor.RED
								+ "disabled.");
						break;
					case restart:
						this.taxManager.disable();
						this.salaryManager.disable();
						Buffer.getInstance().cancelBuffer();
						this.taxManager.enable();
						this.salaryManager.enable();
						Buffer.getInstance().start();
						sender.sendMessage(ChatColor.YELLOW + cashFlow.prefix
								+ " Taxes and salaries restarted.");
						break;
					case setworld:
						if (args.length == 2)
						{
							if (this.cashFlow.permsManager.setWorld(args[1]))
							{
								sender.sendMessage(ChatColor.GREEN
										+ cashFlow.prefix + " World set to: "
										+ ChatColor.GRAY + args[1]);
							}
							else
							{
								sender.sendMessage(ChatColor.YELLOW
										+ cashFlow.prefix + " World "
										+ ChatColor.GRAY + args[1]
										+ ChatColor.YELLOW + " not found.");
							}
							return true;
						}
						else
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix
									+ " World not given.");
						}
						break;
					case addplayers:
						if (args.length == 2)
						{
							String worldName = args[1];
							World w = this.cashFlow.getServer().getWorld(
									worldName);
							if (w != null)
							{
								this.cashFlow.permsManager.importPlayers(
										sender, worldName);
								sender.sendMessage(ChatColor.YELLOW
										+ cashFlow.prefix
										+ " Importing players of world '"
										+ ChatColor.GRAY + worldName
										+ ChatColor.YELLOW
										+ "' into master database...");
							}
							else
							{
								sender.sendMessage(ChatColor.YELLOW
										+ cashFlow.prefix + " World '"
										+ ChatColor.GRAY + worldName
										+ ChatColor.YELLOW + "' not found.");
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED + cashFlow.prefix
									+ " World name not given.");
						}
						break;
					case status:
						sender.sendMessage(ChatColor.GRAY + "====="
								+ ChatColor.WHITE + "CashFlow Status"
								+ ChatColor.GRAY + "=====");
						for (Taxer t : this.taxManager.taxTasks)
						{
							sender.sendMessage(ChatColor.RED + t.getName()
									+ ChatColor.GRAY + " : " + t.getState());
						}
						for (Taxer t : this.salaryManager.salaryTasks)
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
			catch (IllegalArgumentException e)
			{
				sender.sendMessage(ChatColor.RED + cashFlow.prefix
						+ " Syntax error. For help, use /cashflow");
			}
		}
		return true;
	}

	/**
	 * Show help on cashflow command to sender
	 *
	 * @param sender
	 */
	private void showHelp(CommandSender sender) {
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
