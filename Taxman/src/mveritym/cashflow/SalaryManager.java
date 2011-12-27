package mveritym.cashflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SalaryManager {

	protected static CashFlow cashFlow;
	protected Config conf;
	protected File confFile;
	List<String> salaries;
	List<String> paidGroups;
	List<String> paidPlayers;
	ListIterator<String> iterator;
	Timer timer = new Timer();
	Collection<Taxer> salaryTasks = new ArrayList<Taxer>();

	public SalaryManager(CashFlow cashFlow) {
		SalaryManager.cashFlow = cashFlow;
		conf = cashFlow.getPluginConfig();
		salaries = conf.getStringList("salaries.list");
	}

	public void createSalary(CommandSender sender, String name,
			String paycheck, String interval, String employer) {
		conf.save();
		String salaryName = name;
		double salary = Double.parseDouble(paycheck);
		double salaryInterval = Double.parseDouble(interval);
		List<String> paidGroups = null;

		salaries = conf.getStringList("salaries.list");
		iterator = salaries.listIterator();

		if (salary <= 0)
		{
			sender.sendMessage(ChatColor.RED
					+ "Please choose a salary greater than 0.");
			return;
		}
		else if (salaryInterval <= 0)
		{
			sender.sendMessage(ChatColor.RED
					+ "Please choose a salary interval greater than 0.");
			return;
		}
		else if ((TaxManager.cashFlow.eco.bankBalance(employer).type) == EconomyResponse.ResponseType.FAILURE
				&& !(employer.equals("null")))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return;
		}
		else
		{
			while (iterator.hasNext())
			{
				if (iterator.next().equals(salaryName))
				{
					sender.sendMessage(ChatColor.RED
							+ "A salary with that name has already been created.");
					return;
				}
			}
		}

		salaries.add(salaryName);
		conf.setProperty("salaries.list", salaries);
		conf.setProperty("salaries." + salaryName + ".salary", paycheck);
		conf.setProperty("salaries." + salaryName + ".salaryInterval",
				salaryInterval);
		conf.setProperty("salaries." + salaryName + ".employer", employer);
		conf.setProperty("salaries." + salaryName + ".paidGroups", paidGroups);
		conf.setProperty("salaries." + salaryName + ".paidPlayers", paidPlayers);
		conf.setProperty("salaries." + salaryName + ".lastPaid", null);
		conf.setProperty("salaries." + salaryName + ".exceptedPlayers", null);
		conf.setProperty("salaries." + salaryName + ".onlineOnly.isEnabled",
				false);
		conf.setProperty("salaries." + salaryName + ".onlineOnly.interval", 0.0);
		conf.save();

		sender.sendMessage(ChatColor.GREEN + "New salary " + salaryName
				+ " created successfully.");
	}

	public void deleteSalary(CommandSender sender, String name) {
		conf.save();
		String salaryName = name;

		salaries = conf.getStringList("salaries.list");

		if (salaries.contains(salaryName))
		{
			salaries.remove(salaryName);

			for (Taxer task : salaryTasks)
			{
				if (task.getName().equals(name))
				{
					task.cancel();
				}
			}

			conf.setProperty("salaries.list", salaries);
			conf.removeProperty("salaries." + salaryName);
			conf.save();

			sender.sendMessage(ChatColor.GREEN + "Salary " + salaryName
					+ " deleted successfully.");
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "No salary, " + salaryName);
		}

		return;
	}

	public void salaryInfo(CommandSender sender, String salaryName) {
		conf.save();
		salaries = conf.getStringList("salaries.list");

		if (salaries.contains(salaryName))
		{
			sender.sendMessage(ChatColor.BLUE + "Salary: "
					+ conf.getProperty("salaries." + salaryName + ".salary"));
			sender.sendMessage(ChatColor.BLUE
					+ "Interval: "
					+ conf.getProperty("salaries." + salaryName
							+ ".salaryInterval") + " hours");
			sender.sendMessage(ChatColor.BLUE + "Receiving player: "
					+ conf.getString("salaries." + salaryName + ".employer"));
			sender.sendMessage(ChatColor.BLUE
					+ "Paid groups: "
					+ conf.getStringList("salaries." + salaryName
							+ ".paidGroups"));
			sender.sendMessage(ChatColor.BLUE
					+ "Paid players: "
					+ conf.getStringList("salaries." + salaryName
							+ ".paidPlayers"));
			sender.sendMessage(ChatColor.BLUE
					+ "Excepted users: "
					+ conf.getStringList("salaries." + salaryName
							+ ".exceptedPlayers"));
			sender.sendMessage(ChatColor.BLUE
					+ "Online only: "
					+ conf.getBoolean("salaries." + salaryName
							+ ".onlineOnly.isEnabled", false)
					+ ", Online interval: "
					+ conf.getDouble("salaries." + salaryName
							+ ".onlineOnly.interval", 0.0) + " hours");
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "No salary, " + salaryName
					+ ", found.");
		}

		return;
	}

	public void listSalaries(CommandSender sender) {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		iterator = salaries.listIterator();

		if (salaries.size() != 0)
		{
			while (iterator.hasNext())
			{
				sender.sendMessage(ChatColor.BLUE + iterator.next());
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "No salaries to list.");
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

	public void addGroup(CommandSender sender, String salaryName,
			String groupName) {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		paidGroups = conf.getStringList("salaries." + salaryName
				+ ".paidGroups");

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		else if (!(SalaryManager.cashFlow.permsManager.isGroup(groupName)))
		{
			sender.sendMessage(ChatColor.RED + "Group not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + salaryName
					+ " applied successfully to " + groupName);
			paidGroups.add(groupName);
			conf.setProperty("salaries." + salaryName + ".paidGroups",
					paidGroups);
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

	public void addPlayer(CommandSender sender, String salaryName,
			String playerName) {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		paidPlayers = conf.getStringList("salaries." + salaryName
				+ ".paidPlayers");

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		else if (!(SalaryManager.cashFlow.permsManager.isPlayer(playerName
				.toLowerCase())))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
		}
		else if (paidPlayers.contains(playerName.toLowerCase()))
		{
			sender.sendMessage(ChatColor.RED + playerName
					+ " is already paying this tax.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + salaryName
					+ " applied successfully to " + playerName);
			paidPlayers.add(playerName);
			conf.setProperty("salaries." + salaryName + ".paidPlayers",
					paidPlayers);
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

	public void removeGroup(CommandSender sender, String salaryName,
			String groupName) {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		paidGroups = conf.getStringList("salaries." + salaryName
				+ ".paidGroups");

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		else if (!(paidGroups.contains(groupName)))
		{
			sender.sendMessage(ChatColor.RED + "Group not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + salaryName
					+ " removed successfully from " + groupName);
			paidGroups.remove(groupName);
			conf.setProperty("salaries." + salaryName + ".paidGroups",
					paidGroups);
			conf.save();
		}

		return;
	}

	public void removePlayers(CommandSender sender, String salaryName,
			String players) {
		conf.save();
		String[] playerNames = players.split(",");
		for (String name : playerNames)
		{
			removePlayer(sender, salaryName, name);
		}
	}

	public void removePlayer(CommandSender sender, String salaryName,
			String playerName) {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		paidPlayers = conf.getStringList("salaries." + salaryName
				+ ".paidPlayers");

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		else if (!(paidPlayers.contains(playerName)))
		{
			sender.sendMessage(ChatColor.RED + "Player not found.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + salaryName
					+ " removed successfully from " + playerName);
			paidPlayers.remove(playerName);
			conf.setProperty("salaries." + salaryName + ".paidPlayers",
					paidPlayers);
			conf.save();
		}
	}

	public void enable() {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		Double hours;

		for (String salaryName : salaries)
		{

			hours = conf.getDouble(
					("salaries." + salaryName + ".salaryInterval"), 1);
			System.out.println("[" + SalaryManager.cashFlow.info.getName()
					+ "] Enabling " + salaryName);
			Taxer taxer = new Taxer(this, salaryName, hours);
			salaryTasks.add(taxer);
		}
	}

	public void disable() {
		for (Taxer salaryTask : salaryTasks)
		{
			salaryTask.cancel();
		}
	}

	public void setOnlineOnly(String salaryName, Boolean online, Double interval) {
		conf.save();
		conf.setProperty("salaries." + salaryName + ".onlineOnly.isEnabled",
				online);
		conf.setProperty("salaries." + salaryName + ".onlineOnly.interval",
				interval);
		conf.save();
		return;
	}

	public void addException(CommandSender sender, String salaryName,
			String userName) {
		conf.save();
		salaries = conf.getStringList("salaries.list");
		List<String> exceptedPlayers = conf.getStringList("salaries."
				+ salaryName + ".exceptedPlayers");

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		else if (salaries.contains(userName))
		{
			sender.sendMessage(ChatColor.RED + userName
					+ " is already listed as excepted.");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + userName
					+ " added as an exception.");
			exceptedPlayers.add(userName);
			conf.setProperty("salaries." + salaryName + ".exceptedPlayers",
					exceptedPlayers);
			conf.save();
		}

		return;
	}

	public void removeException(CommandSender sender, String salaryName,
			String userName) {
		salaries = conf.getStringList("salaries.list");
		List<String> exceptedPlayers = conf.getStringList("salaries."
				+ salaryName + ".exceptedPlayers");

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
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
			conf.setProperty("salaries." + salaryName + ".exceptedPlayers",
					exceptedPlayers);
			conf.save();
		}
	}

	public List<String> checkOnline(List<String> users, Double interval) {
		List<String> tempPlayerList = new ArrayList<String>();
		for (String player : users)
		{
			if (PermissionsManager.cashflow.getServer().getPlayer(player) != null)
			{
				tempPlayerList.add(player);
			}
		}
		return tempPlayerList;
	}

	public void paySalary(String salaryName) {
		conf.save();
		System.out.println("[" + SalaryManager.cashFlow.info.getName()
				+ "] Paying salary " + salaryName);

		salaries = conf.getStringList("salaries.list");

		List<String> groups = conf.getStringList("salaries." + salaryName
				+ ".paidGroups");
		List<String> players = conf.getStringList("salaries." + salaryName
				+ ".paidPlayers");
		List<String> exceptedPlayers = conf.getStringList("salaries."
				+ salaryName + ".exceptedPlayers");
		Double salary = Double.parseDouble(conf.getString("salaries."
				+ salaryName + ".salary"));
		String employer = conf
				.getString("salaries." + salaryName + ".employer");

		List<String> users = SalaryManager.cashFlow.permsManager.getUsers(
				groups, players, exceptedPlayers);

		if (conf.getBoolean("salaries." + salaryName + ".onlineOnly.isEnabled",
				false))
		{
			Double onlineInterval = conf.getDouble("salaries." + salaryName
					+ ".onlineOnly.interval", 0);
			users = checkOnline(users, onlineInterval);
		}

		for (String user : users)
		{
			if (SalaryManager.cashFlow.eco.bankBalance(user).type == EconomyResponse.ResponseType.SUCCESS)
			{

				if (!(employer.equals("null")))
				{
					double tempSalary = TaxManager.cashFlow.eco
							.bankBalance(employer).amount;
					Player player = SalaryManager.cashFlow.getServer()
							.getPlayer(user);
					boolean withdraw = false;
					if (tempSalary != 0)
					{
						if (tempSalary > salary)
						{
							tempSalary = salary;
						}
						EconomyResponse er = SalaryManager.cashFlow.eco
								.bankWithdraw(employer, tempSalary);
						if (er.type == EconomyResponse.ResponseType.SUCCESS)
						{
							if (PermissionsManager.cashflow.getServer()
									.getPlayer(employer) != null)
							{
								Player employerPlayer = TaxManager.cashFlow
										.getServer().getPlayer(employer);
								employerPlayer.sendMessage(ChatColor.BLUE
										+ "You have paid $" + tempSalary
										+ " in salary to " + user + ".");
							}
							withdraw = true;
						}
						else
						{
							if (PermissionsManager.cashflow.getServer()
									.getPlayer(employer) != null)
							{
								Player employerPlayer = TaxManager.cashFlow
										.getServer().getPlayer(employer);
								employerPlayer.sendMessage(ChatColor.RED
										+ "Could not withdraw salary.");
							}
							SalaryManager.cashFlow.log.warning("["
									+ SalaryManager.cashFlow.info.getName()
									+ "] " + er.errorMessage + ": " + user);
						}
						er = SalaryManager.cashFlow.eco.bankDeposit(user,
								tempSalary);
						if (er.type == EconomyResponse.ResponseType.SUCCESS
								&& !withdraw)
						{
							if (player != null)
							{
								String message = "You have received $"
										+ tempSalary + " for your salary from "
										+ employer + ".";
								player.sendMessage(ChatColor.BLUE + message);
							}
						}
						else
						{
							if (player != null)
							{
								String message = "Could not add salary.";
								player.sendMessage(ChatColor.RED + message);
							}
							SalaryManager.cashFlow.log.warning("["
									+ SalaryManager.cashFlow.info.getName()
									+ "] " + er.errorMessage + ": " + user);
						}
					}
					else
					{
						if (player != null)
						{
							String message = employer
									+ " does not have enough money to pay your salary.";
							player.sendMessage(ChatColor.BLUE + message);
						}
					}
				}
				else
				{
					// Generate money
					TaxManager.cashFlow.eco.bankDeposit(user, salary);
					Player player = SalaryManager.cashFlow.getServer()
							.getPlayer(user);

					if (player != null)
					{
						String message = "You have received $" + salary
								+ " for your salary";
						if (employer.equals("null"))
						{
							message += ".";
						}
						else
						{
							message += " from " + employer + ".";
						}
						player.sendMessage(ChatColor.BLUE + message);
					}
				}
			}
			else
			{
				// Acount does not exist
				/*
				 * SalaryManager.cashFlow.log.warning("[" +
				 * SalaryManager.cashFlow.info.getName() + "] " +
				 * SalaryManager.cashFlow.eco.bankBalance(user).errorMessage
				 * +": " + user);
				 */
			}
		}
	}

	public void setRate(CommandSender sender, String salaryName, String salary) {
		salaries = conf.getStringList("salaries.list");
		Double rate = Double.parseDouble(salary);

		if (!(salaries.contains(salaryName)))
		{
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		else if (rate <= 0)
		{
			sender.sendMessage(ChatColor.RED
					+ "Please choose a salary greater than 0.");
		}
		else
		{
			conf.setProperty("salaries." + salaryName + ".salary", salary);
			conf.save();
			sender.sendMessage(ChatColor.GREEN + "Rate of salary " + salaryName
					+ " is set to " + salary + ".");
		}
	}
}
