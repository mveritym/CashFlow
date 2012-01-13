package mveritym.cashflow;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaxManager {
	protected CashFlow cashFlow;
	protected Config conf;
	protected File confFile;
	List<String> taxes;
	List<String> payingGroups;
	List<String> payingPlayers;
	ListIterator<String> iterator;
	Collection<Taxer> taxTasks = new ArrayList<Taxer>();

	public TaxManager(CashFlow cashFlow) {
		this.cashFlow = cashFlow;
		conf = cashFlow.getPluginConfig();

		taxes = conf.getStringList("taxes.list");
	}

	public void createTax(CommandSender sender, String name, String tax,
			String interval, String taxReceiver) {
		String taxName = name;
		double taxInterval = Double.parseDouble(interval);
		List<String> payingGroups = null;
		List<String> payingPlayers = null;

		taxes = conf.getStringList("taxes.list");
		iterator = taxes.listIterator();

		// Checks for if tax is a percent.
		if (tax.contains("%"))
		{
			double percentIncome = Double.parseDouble(tax.split("%")[0]);
			if (percentIncome > 100 || percentIncome <= 0)
			{
				sender.sendMessage(ChatColor.RED
						+ "Please choose a % of income between 0 and 100.");
				return;
			}
		}

		// Checks arguments in general.
		if (taxInterval <= 0)
		{
			sender.sendMessage(ChatColor.RED
					+ "Please choose a tax interval greater than 0.");
			return;
		}
		else if ((this.cashFlow.eco.bankBalance(taxReceiver).type) == EconomyResponse.ResponseType.FAILURE
				&& !(taxReceiver.equals("null")))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return;
		}
		else
		{
			while (iterator.hasNext())
			{
				if (iterator.next().equals(taxName))
				{
					sender.sendMessage(ChatColor.RED
							+ "A tax with that name has already been created.");
					return;
				}
			}
		}

		taxes.add(taxName);
		conf.setProperty("taxes.list", taxes);
		conf.setProperty("taxes." + taxName + ".tax", tax);
		conf.setProperty("taxes." + taxName + ".taxInterval", taxInterval);
		conf.setProperty("taxes." + taxName + ".receiver", taxReceiver);
		conf.setProperty("taxes." + taxName + ".payingGroups", payingGroups);
		conf.setProperty("taxes." + taxName + ".payingPlayers", payingPlayers);
		conf.setProperty("taxes." + taxName + ".lastPaid", null);
		conf.setProperty("taxes." + taxName + ".exceptedPlayers", null);
		conf.setProperty("taxes." + taxName + ".onlineOnly.isEnabled", false);
		conf.setProperty("taxes." + taxName + ".onlineOnly.interval", 0.0);
		conf.save();

		sender.sendMessage(ChatColor.GREEN + "New tax " + taxName
				+ " created successfully.");
	}

	public void deleteTax(CommandSender sender, String name) {
		String taxName = name;

		taxes = conf.getStringList("taxes.list");

		if (taxes.contains(taxName))
		{
			taxes.remove(taxName);

			for (Taxer task : taxTasks)
			{
				if (task.getName().equals(name))
				{
					task.cancel();
				}
			}

			conf.setProperty("taxes.list", taxes);
			conf.removeProperty("taxes." + taxName);
			conf.save();

			sender.sendMessage(ChatColor.GREEN + "Tax " + taxName
					+ " deleted successfully.");
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "No tax, " + taxName);
		}

		return;
	}

	public void taxInfo(CommandSender sender, String taxName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");

		if (taxes.contains(taxName))
		{
			sender.sendMessage(ChatColor.BLUE + "Tax: "
					+ conf.getProperty("taxes." + taxName + ".tax"));
			sender.sendMessage(ChatColor.BLUE + "Interval: "
					+ conf.getProperty("taxes." + taxName + ".taxInterval")
					+ " hours");
			sender.sendMessage(ChatColor.BLUE + "Receiving player: "
					+ conf.getProperty("taxes." + taxName + ".receiver"));
			sender.sendMessage(ChatColor.BLUE + "Paying groups: "
					+ conf.getStringList("taxes." + taxName + ".payingGroups"));
			sender.sendMessage(ChatColor.BLUE + "Paying players: "
					+ conf.getStringList("taxes." + taxName + ".payingPlayers"));
			sender.sendMessage(ChatColor.BLUE
					+ "Excepted users: "
					+ conf.getStringList("taxes." + taxName
							+ ".exceptedPlayers"));
			sender.sendMessage(ChatColor.BLUE
					+ "Online only: "
					+ conf.getBoolean("taxes." + taxName
							+ ".onlineOnly.isEnabled", false)
					+ ", Online interval: "
					+ conf.getDouble("taxes." + taxName
							+ ".onlineOnly.interval", 0.0) + " hours");
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}

		return;
	}

	public void listTaxes(CommandSender sender) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		iterator = taxes.listIterator();

		if (taxes.size() != 0)
		{
			while (iterator.hasNext())
			{
				sender.sendMessage(ChatColor.BLUE + iterator.next());
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "No taxes to list.");
		}
	}

	public void addGroups(CommandSender sender, String taxName, String groups) {
		conf.save();
		String[] groupNames = groups.split(",");
		for (String name : groupNames)
		{
			addGroup(sender, taxName, name);
		}
	}

	public void addGroup(CommandSender sender, String taxName, String groupName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		payingGroups = conf.getStringList("taxes." + taxName + ".payingGroups");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		else if (!(this.cashFlow.permsManager.isGroup(groupName)))
		{
			sender.sendMessage(ChatColor.RED + "Group not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + taxName + ChatColor.GREEN
					+ " applied successfully to " + ChatColor.GRAY + groupName);
			payingGroups.add(groupName);
			conf.setProperty("taxes." + taxName + ".payingGroups", payingGroups);
			conf.save();
		}

		return;
	}

	public void addPlayers(CommandSender sender, String taxName, String players) {
		conf.save();
		String[] playerNames = players.split(",");
		for (String name : playerNames)
		{
			addPlayer(sender, taxName, name);
		}
	}

	public void addPlayer(CommandSender sender, String taxName,
			String playerName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		payingPlayers = conf.getStringList("taxes." + taxName
				+ ".payingPlayers");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		else if (!(this.cashFlow.permsManager
				.isPlayer(playerName.toLowerCase())))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
		}
		else if (payingPlayers.contains(playerName.toLowerCase()))
		{
			sender.sendMessage(ChatColor.RED + playerName
					+ " is already paying this tax.");
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + taxName + ChatColor.GREEN
					+ " applied successfully to " + ChatColor.GRAY + playerName);
			payingPlayers.add(playerName);
			conf.setProperty("taxes." + taxName + ".payingPlayers",
					payingPlayers);
			conf.save();
		}

		return;
	}

	public void removeGroups(CommandSender sender, String taxName, String groups) {
		conf.save();
		String[] groupNames = groups.split(",");
		for (String name : groupNames)
		{
			removeGroup(sender, taxName, name);
		}
	}

	public void removeGroup(CommandSender sender, String taxName,
			String groupName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		payingGroups = conf.getStringList("taxes." + taxName + ".payingGroups");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		else if (!(payingGroups.contains(groupName)))
		{
			sender.sendMessage(ChatColor.RED + "Group not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + taxName + ChatColor.GREEN
					+ " removed successfully from " + ChatColor.GRAY + groupName);
			payingGroups.remove(groupName);
			conf.setProperty("taxes." + taxName + ".payingGroups", payingGroups);
			conf.save();
		}
	}

	public void removePlayers(CommandSender sender, String taxName,
			String players) {
		conf.save();
		String[] playerNames = players.split(",");
		for (String name : playerNames)
		{
			removePlayer(sender, taxName, name);
		}
	}

	public void removePlayer(CommandSender sender, String taxName,
			String playerName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		payingPlayers = conf.getStringList("taxes." + taxName
				+ ".payingPlayers");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		else if (!(payingPlayers.contains(playerName)))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + taxName + ChatColor.GREEN
					+ " removed successfully from " + ChatColor.GRAY  + playerName);
			payingPlayers.remove(playerName);
			conf.setProperty("taxes." + taxName + ".payingPlayers",
					payingPlayers);
			conf.save();
		}
	}

	public void enable() {
		conf.save();
		taxes = conf.getStringList("taxes.list");

		for (String taxName : taxes)
		{
			enableTax(taxName);
		}
	}

	public void enableTax(String tax)
	{
		boolean has = false;
		for(Taxer t : taxTasks.toArray(new Taxer[0]))
		{
			if(t.getName().equals(tax))
			{
				t.start();
				has = true;
			}
		}
		if(!has)
		{
			Double hours = conf.getDouble(("taxes." + tax + ".taxInterval"), 1);
			this.cashFlow.log.info(this.cashFlow.prefix + " Enabling " + tax);
			Taxer taxer = new Taxer(this, tax, hours);
			taxTasks.add(taxer);
		}
	}

	public List<String> checkOnline(List<String> users, Double interval) {
		conf.save();
		List<String> tempPlayerList = new ArrayList<String>();
		for (String player : users)
		{
			if (interval == 0
					&& this.cashFlow.getServer().getPlayer(player) != null)
			{
				tempPlayerList.add(player);
			}
		}
		return tempPlayerList;
	}


	public void disableTax(String tax)
	{
		for(Taxer t : this.taxTasks)
		{
			if(t.getName().equals(tax))
			{
				t.cancel();
			}
		}
	}

	public List<String> getUsers(String taxName) {
		List<String> groups = conf.getStringList("taxes." + taxName
				+ ".payingGroups");
		List<String> players = conf.getStringList("taxes." + taxName
				+ ".payingPlayers");
		List<String> exceptedPlayers = conf.getStringList("taxes." + taxName
				+ ".exceptedPlayers");

		return this.cashFlow.permsManager.getUsers(groups, players,
				exceptedPlayers);
	}

	public void payTax(String taxName) {
		conf.save();

		List<String> users = this.getUsers(taxName);

		if (conf.getBoolean("taxes." + taxName + ".onlineOnly.isEnabled", false))
		{
			Double onlineInterval = conf.getDouble("taxes." + taxName
					+ ".onlineOnly.interval", 0);
			users = checkOnline(users, onlineInterval);
		}

		for (String user : users)
		{
			Buffer.getInstance().addToBuffer(user, taxName, true);
		}
	}

	public void payTaxToUser(String user, String taxName) {
		conf.save();

		String tax = conf.getString("taxes." + taxName + ".tax");
		String receiver = conf.getString("taxes." + taxName + ".receiver");
		Double taxRate;
		boolean withdraw = true;
		boolean ico5 = false;
		if(this.cashFlow.eco.getName().equals("iConomy 5"))
		{
			ico5 = true;
		}
		EconomyResponse er = new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "EconomyResponse object not initialized.");
		if(ico5)
		{
			er = this.cashFlow.eco.withdrawPlayer(user, 0);
		}
		else
		{
			er = this.cashFlow.eco.bankBalance(user);
		}
		if (er.type == EconomyResponse.ResponseType.SUCCESS)
		{
			Player player = this.cashFlow.getServer().getPlayer(user);
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			double balance = 0;
			if(ico5)
			{
				balance = this.cashFlow.eco.getBalance(user);
			}
			else
			{
				balance = this.cashFlow.eco.bankBalance(user).balance;
			}
			if (tax.contains("%"))
			{
				taxRate = Double.parseDouble(tax.split("%")[0]) / 100.0;
				taxRate *= balance;
			}
			else
			{
				taxRate = Double.parseDouble(tax);
			}

			taxRate = Double.valueOf(twoDForm.format(taxRate));
			if (balance != 0)
			{
				if (balance < taxRate)
				{
					// If they don't have enough in their account, set
					// it to take everything
					taxRate = balance;
				}

				if(ico5)
				{
					er = this.cashFlow.eco.withdrawPlayer(user, taxRate);
				}
				else
				{
					er = this.cashFlow.eco.bankWithdraw(user, taxRate);
				}
				if (er.type == EconomyResponse.ResponseType.SUCCESS)
				{
					if (player != null)
					{
						String message = "You have paid $" + taxRate
								+ " in tax";
						if (receiver.equals("null"))
						{
							message += ".";
						}
						else
						{
							message += " to " + receiver + ".";
						}
						player.sendMessage(ChatColor.BLUE + message);
					}
				}
				else
				{
					withdraw = false;
					this.cashFlow.log.warning(this.cashFlow.prefix + " "
							+ er.errorMessage + ": " + user);
				}

				if (!(receiver.equals("null")) && withdraw)
				{
					if(ico5)
					{
						er = this.cashFlow.eco.depositPlayer(receiver, taxRate);
					}
					else
					{
						er = this.cashFlow.eco.bankDeposit(receiver, taxRate);
					}
					if (er.type == EconomyResponse.ResponseType.SUCCESS)
					{
						if (this.cashFlow.getServer().getPlayer(receiver) != null)
						{
							Player receiverPlayer = this.cashFlow.getServer()
									.getPlayer(receiver);
							receiverPlayer.sendMessage(ChatColor.BLUE
									+ "You have received $" + taxRate
									+ " in tax from " + user + ".");
						}
					}
					else
					{
						this.cashFlow.log
								.warning(this.cashFlow.prefix
										+ " "
										+ this.cashFlow.eco.bankBalance(user).errorMessage
										+ ": " + receiver);
					}
				}
				else
				{
					if (this.cashFlow.getServer().getPlayer(receiver) != null)
					{
						Player receiverPlayer = this.cashFlow.getServer()
								.getPlayer(receiver);
						receiverPlayer.sendMessage(ChatColor.RED
								+ "Could not retrieve tax from " + user + ".");
					}
				}
			}
			else
			{
				if (player != null)
				{
					String message = "No money to pay tax";
					if (receiver.equals("null"))
					{
						message += ".";
					}
					else
					{
						message += " to " + receiver + ".";
					}
					player.sendMessage(ChatColor.BLUE + message);
				}
			}
		}
		else
		{
			// Account does not exist

			 /*cashFlow.log .warning("[" +
			 cashFlow.info.getName() + "] " +
			 cashFlow.eco.bankBalance(user).errorMessage + ": " +
			 user);*/

		}
	}

	public void disable() {
		conf.save();
		for (Taxer taxTask : taxTasks)
		{
			taxTask.cancel();
		}
	}

	public void setOnlineOnly(String taxName, Boolean online, Double interval) {
		conf.save();
		conf.setProperty("taxes." + taxName + ".onlineOnly.isEnabled", online);
		conf.setProperty("taxes." + taxName + ".onlineOnly.interval", interval);
		conf.save();
		return;
	}

	public void addException(CommandSender sender, String taxName,
			String userName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		List<String> exceptedPlayers = conf.getStringList("taxes." + taxName
				+ ".exceptedPlayers");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		else if (taxes.contains(userName))
		{
			sender.sendMessage(ChatColor.RED + userName
					+ " is already listed as excepted.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + userName
					+ " added as an exception.");
			exceptedPlayers.add(userName);
			conf.setProperty("taxes." + taxName + ".exceptedPlayers",
					exceptedPlayers);
			conf.save();
		}

		return;
	}

	public void removeException(CommandSender sender, String taxName,
			String userName) {
		conf.save();
		taxes = conf.getStringList("taxes.list");
		List<String> exceptedPlayers = conf.getStringList("taxes." + taxName
				+ ".exceptedPlayers");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
		}
		else if (!(exceptedPlayers.contains(userName)))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + userName
					+ " removed as an exception.");
			exceptedPlayers.remove(userName);
			conf.setProperty("taxes." + taxName + ".exceptedPlayers",
					exceptedPlayers);
			conf.save();
		}
	}

	public void setRate(CommandSender sender, String taxName, String tax) {
		conf.save();
		taxes = conf.getStringList("taxes.list");

		if (!(taxes.contains(taxName)))
		{
			sender.sendMessage(ChatColor.RED + "Tax not found.");
			return;
		}
		else if (tax.contains("%"))
		{
			double percentIncome = Double.parseDouble(tax.split("%")[0]);
			if (percentIncome > 100 || percentIncome <= 0)
			{
				sender.sendMessage(ChatColor.RED
						+ "Please choose a % of income between 0 and 100.");
				return;
			}
		}

		conf.setProperty("taxes." + taxName + ".tax", tax);
		conf.save();
		sender.sendMessage(ChatColor.GREEN + "Rate of tax " + taxName
				+ " is set to " + tax + ".");
	}
}
