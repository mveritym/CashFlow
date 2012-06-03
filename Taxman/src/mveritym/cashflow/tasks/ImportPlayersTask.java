package mveritym.cashflow.tasks;

import java.io.File;
import java.sql.SQLException;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.database.SQLibrary.Database.Query;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ImportPlayersTask implements Runnable
{
	private CashFlow cashflow;
	private String worldName;
	private CommandSender sender;

	public ImportPlayersTask(CashFlow cashflow, CommandSender sender, String world)
	{
		this.cashflow = cashflow;
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
							+ cashflow.getPluginConfig().tablePrefix
							+ "cashflow WHERE playername='" + name + "';";
					final Query rs = cashflow.getDatabaseHandler().select(
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
								+ cashflow.getPluginConfig().tablePrefix
								+ "cashflow (playername) VALUES('" + name
								+ "');";
						cashflow.getDatabaseHandler().standardQuery(query);
					}
				}
				catch (SQLException e)
				{
					cashflow.getLogger().warning(
							cashflow.prefix + " SQL Exception");
					e.printStackTrace();
				}
			}
		}
		sender.sendMessage(ChatColor.GREEN + cashflow.prefix
				+ " Done importing players from '" + ChatColor.GRAY
				+ worldName + ChatColor.GREEN + "' into database");
		cashflow.getLogger().info(
				cashflow.prefix + " Done importing players from "
						+ worldName + " into database");
	}
}
