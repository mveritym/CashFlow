package mveritym.cashflow.database;

import mveritym.cashflow.config.Config;

public enum Table
{
	CASHFLOW(Config.tablePrefix + "cashflow"), BUFFER(Config.tablePrefix + "buffer");
	
	private String name;
	
	private Table(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}
