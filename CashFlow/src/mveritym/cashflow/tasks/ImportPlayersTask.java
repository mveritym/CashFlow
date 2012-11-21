package mveritym.cashflow.tasks;

import java.io.File;
import java.sql.SQLException;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.database.Table;
import mveritym.cashflow.database.SQLibrary.Database.Query;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ImportPlayersTask implements Runnable
{
	private CashFlow plugin;
	private String worldName;
	private CommandSender sender;

	public ImportPlayersTask(CashFlow cashflow, CommandSender sender, String world)
	{
		this.plugin = cashflow;
		this.worldName = world;
		this.sender = sender;
	}

	@Override
	public void run()
	{
		final File folder = new File(worldName + File.separator + "players"
				+ File.separator);
		for (final File playerFile : folder.listFiles())
		{
			if (playerFile != null)
			{
				final String name = playerFile.getName().substring(0,
						playerFile.getName().length() - 4);
				try
				{
					boolean has = false;
					// Check if player already exists
					String query = "SELECT COUNT(*) FROM "
							+ Table.CASHFLOW.getName()
							+ " WHERE playername='" + name + "';";
					final Query rs = plugin.getDatabaseHandler().select(
							query);
					if (rs.getResult().next())
					{
						if (rs.getResult().getInt(1) >= 1)
						{
							// They're already in the database
							has = true;
						}
					}
					rs.closeQuery();
					if (!has)
					{
						// Add to master list
						query = "INSERT INTO "
								+ Table.CASHFLOW.getName()
								+ " (playername) VALUES('" + name
								+ "');";
						plugin.getDatabaseHandler().standardQuery(query);
					}
				}
				catch (SQLException e)
				{
					plugin.getLogger().warning(
							CashFlow.TAG + " SQL Exception");
					e.printStackTrace();
				}
			}
		}
		sender.sendMessage(ChatColor.GREEN + CashFlow.TAG
				+ " Done importing players from '" + ChatColor.GRAY
				+ worldName + ChatColor.GREEN + "' into database");
		plugin.getLogger().info(
				CashFlow.TAG + " Done importing players from "
						+ worldName + " into database");
	}
}
