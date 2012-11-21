package com.mveritym.cashflow.permissions;

public enum PermissionNode
{
	BASIC(".basic"), TAX(".tax"), SALARY(".salary");
	
	private static final String prefix = "cashflow.";
	private String node;
	
	private PermissionNode(String node)
	{
		this.node = prefix + node;
	}
	
	public String getNode()
	{
		return node;
	}
}
