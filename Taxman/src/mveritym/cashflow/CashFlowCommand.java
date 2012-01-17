package mveritym.cashflow;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CashFlowCommand implements CommandExecutor {
	private CashFlow cashFlow;
	private TaxManager taxManager;
	private SalaryManager salaryManager;

	public CashFlowCommand(CashFlow cf, TaxManager tax, SalaryManager salary)
	{
		cashFlow = cf;
		taxManager = tax;
		salaryManager = salary;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
			String[] args) {
		if(args.length == 0)
		{
			//No arguments given
			showHelp(sender);
		}
		else
		{
			final CashFlowCommands com = CashFlowCommands.valueOf(args[0].toLowerCase());
			switch(com)
			{
				case enable:
						this.taxManager.enable();
						this.salaryManager.enable();
						Buffer.getInstance().start();
						sender.sendMessage(ChatColor.GREEN
								+ "Taxes and salaries enabled.");
				case disable:
						this.taxManager.disable();
						this.salaryManager.disable();
						Buffer.getInstance().cancelBuffer();
						sender.sendMessage(ChatColor.GREEN
								+ "Taxes and salaries disabled.");
				case restart:
						this.taxManager.disable();
						this.salaryManager.disable();
						Buffer.getInstance().cancelBuffer();
						this.taxManager.enable();
						this.salaryManager.enable();
						Buffer.getInstance().start();
						sender.sendMessage(ChatColor.GREEN
								+ "Taxes and salaries restarted.");
				case setworld:
					if (args.length == 2)
					{
						if (this.cashFlow.permsManager.setWorld(args[1]))
						{
							sender.sendMessage(ChatColor.GREEN + "World set.");
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "World not found.");
						}
						return true;
					}
					else
					{
						sender.sendMessage(ChatColor.RED + " World not given.");
					}
				case addplayers:
					if (args.length == 2)
					{
						String worldName = args[1];
						World w = this.cashFlow.getServer().getWorld(worldName);
						if (w != null)
						{
							this.cashFlow.permsManager.importPlayers(sender,
									worldName);
							sender.sendMessage(ChatColor.GREEN
									+ "Importing players of world '" + worldName
									+ "' into master database...");
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
				default:
					showHelp(sender);
					break;

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
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW +"enable " + ChatColor.GREEN+"- Enables payment of taxes and salaries.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW +"disable " + ChatColor.GREEN+"- Disables payment of taxes and salaries.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW +"restart " + ChatColor.GREEN+"- Updates all taxes and salaries. Restarts time intervals.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW +"status " + ChatColor.GREEN+"- Check status of all taxes and salaries and buffered operations.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW +"setworld " + ChatColor.GREEN+"- Specifies world name. Legacy: ONLY for v1.0.0 and lower.");
		sender.sendMessage(ChatColor.AQUA + "/cashflow " + ChatColor.YELLOW +"addplayers <world> " + ChatColor.GREEN+"- Imports player.dat files from specified world to the internal database.");
	}

}
