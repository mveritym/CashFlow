package mveritym.cashflow.database;

import java.sql.SQLException;

import mveritym.cashflow.CashFlow;
import mveritym.cashflow.config.Config;
import mveritym.cashflow.database.SQLibrary.MySQL;
import mveritym.cashflow.database.SQLibrary.SQLite;
import mveritym.cashflow.database.SQLibrary.Database.Query;

public class DBHandler
{
	// Class Variables
	private CashFlow plugin;
	private Config config;
	private SQLite sqlite;
	private MySQL mysql;
	private boolean useMySQL;

	public DBHandler(CashFlow ks, Config conf)
	{
		plugin = ks;
		config = conf;
		useMySQL = config.useMySQL;
		checkTables();
		if (config.importSQL)
		{
			if (useMySQL)
			{
				importSQL();
			}
			config.set("mysql.import", false);
		}
	}

	private void checkTables()
	{
		if (useMySQL)
		{
			// Connect to mysql database
			mysql = new MySQL(plugin.getLogger(), CashFlow.TAG, config.host,
					config.port, config.database, config.user, config.password);
			// Check if master table exists
			if (!mysql.checkTable(Table.CASHFLOW.getName()))
			{
				plugin.getLogger().info(
						CashFlow.TAG + " Created master list table");
				// Master table
				// TODO primary key row id
				mysql.createTable("CREATE TABLE "
						+ Table.CASHFLOW.getName()
						+ " (playername varchar(32) NOT NULL, laston REAL, checks INT, UNIQUE(`playername`));");
			}
			if (!mysql.checkTable(Table.BUFFER.getName()))
			{
				plugin.getLogger().info(CashFlow.TAG + " Created buffer table");
				// Table to save buffer items
				// TODO primary key row id
				mysql.createTable("CREATE TABLE "
						+ Table.BUFFER.getName()
						+ " (name varchar(32) NOT NULL, contract TEXT NOT NULL, tax INT NOT NULL);");
			}
		}
		else
		{
			// Connect to sql database
			sqlite = new SQLite(plugin.getLogger(), CashFlow.TAG, "database",
					plugin.getDataFolder().getAbsolutePath());
			// Check if master table exists
			if (!sqlite.checkTable(Table.CASHFLOW.getName()))
			{
				plugin.getLogger().info(
						CashFlow.TAG + " Created master list table");
				// Master table
				// TODO primary key row id
				sqlite.createTable("CREATE TABLE "
						+ Table.CASHFLOW.getName()
						+ " (playername varchar(32) NOT NULL, laston REAL, checks INTEGER, UNIQUE(playername));");
			}
			if (!sqlite.checkTable(Table.BUFFER.getName()))
			{
				plugin.getLogger().info(CashFlow.TAG + " Created buffer table");
				// Table to save buffer items
				// TODO primary key row id
				sqlite.createTable("CREATE TABLE "
						+ Table.BUFFER.getName()
						+ " (name varchar(32) NOT NULL, contract TEXT NOT NULL, tax INTEGER NOT NULL);");
			}
		}
	}

	private void importSQL()
	{
		// Connect to sql database
		try
		{
			StringBuilder sb = new StringBuilder();
			// Grab local SQLite database
			sqlite = new SQLite(plugin.getLogger(), CashFlow.TAG, "database",
					plugin.getDataFolder().getAbsolutePath());
			// Copy items
			Query rs = sqlite.select("SELECT * FROM "
					+ Table.CASHFLOW.getName() + ";");
			if (rs.getResult().next())
			{
				plugin.getLogger().info(
						CashFlow.TAG + " Importing master table...");
				do
				{
					boolean hasLast = false;
					final String name = rs.getResult().getString("playername");
					long laston = 0;
					try
					{
						laston = rs.getResult().getLong("laston");
						if (!rs.getResult().wasNull())
						{
							hasLast = true;
						}
					}
					catch (SQLException noColumn)
					{
						// Ignore
					}
					sb.append("INSERT INTO " + Table.CASHFLOW.getName()
							+ " (playername");
					if (hasLast)
					{
						sb.append(",laston");
					}
					sb.append(") VALUES('" + name + "'");
					if (hasLast)
					{
						sb.append(",'" + laston + "'");
					}
					sb.append(");");
					final String query = sb.toString();
					mysql.standardQuery(query);
					sb = new StringBuilder();
				} while (rs.getResult().next());
			}
			rs.closeQuery();
			sb = new StringBuilder();
			// Copy players
			rs = sqlite.select("SELECT * FROM " + Table.BUFFER.getName() + ";");
			if (rs.getResult().next())
			{
				plugin.getLogger().info(CashFlow.TAG + " Importing buffer...");
				do
				{
					// TODO use prepared statement
					final String name = rs.getResult().getString("name");
					final String contract = rs.getResult()
							.getString("contract");
					final int tax = rs.getResult().getInt("tax");
					sb.append("INSERT INTO " + Table.BUFFER.getName()
							+ " (name,contract,tax) VALUES('" + name + "','"
							+ contract + "','" + tax + "');");
					final String query = sb.toString();
					mysql.standardQuery(query);
					sb = new StringBuilder();
				} while (rs.getResult().next());
			}
			rs.closeQuery();
			plugin.getLogger().info(
					CashFlow.TAG + " Done importing SQLite into MySQL");
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning(
					CashFlow.TAG + " SQL Exception on Import");
			e.printStackTrace();
		}

	}

	public boolean checkConnection()
	{
		boolean connected = false;
		if (useMySQL)
		{
			connected = mysql.checkConnection();
		}
		else
		{
			connected = sqlite.checkConnection();
		}
		return connected;
	}

	public void close()
	{
		if (useMySQL)
		{
			mysql.close();
		}
		else
		{
			sqlite.close();
		}
	}

	public Query select(String query)
	{
		if (useMySQL)
		{
			return mysql.select(query);
		}
		else
		{
			return sqlite.select(query);
		}
	}

	public void standardQuery(String query)
	{
		if (useMySQL)
		{
			mysql.standardQuery(query);
		}
		else
		{
			sqlite.standardQuery(query);
		}
	}

	public void createTable(String query)
	{
		if (useMySQL)
		{
			mysql.createTable(query);
		}
		else
		{
			sqlite.createTable(query);
		}
	}
}
