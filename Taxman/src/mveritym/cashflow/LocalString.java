package mveritym.cashflow;

import java.util.EnumMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import mveritym.cashflow.config.LocalizeConfig;

public enum LocalString
{
	PERMISSION_DENY(LocalizeConfig.permissionDeny), LACK_MESSAGE(
			LocalizeConfig.lackMessage), ECONOMY_FAILURE(
			LocalizeConfig.econFailure), INVALID_NAME(
			LocalizeConfig.invalidName), UNKNOWN_COMMAND(
			LocalizeConfig.unknownCommand), COMMAND_CASHFLOW_RESTART(
			LocalizeConfig.restart), COMMAND_CASHFLOW_ENABLE(
			LocalizeConfig.cashflowEnable), COMMAND_CASHFLOW_DISABLE(
			LocalizeConfig.cashflowDisable);

	private String string;

	private LocalString(String s)
	{
		this.string = s;
	}

	public String parseString(EnumMap<Flag, String> replace)
	{
		/**
		 * Thanks to @Njol for the following
		 * http://forums.bukkit.org/threads/multiple-classes-config-colours.79719/#post-1154761
		 */
		String out = ChatColor.translateAlternateColorCodes('&', string);
		if (replace != null)
		{
			for (Entry<Flag, String> entry : replace.entrySet())
			{
				out = out
						.replaceAll(entry.getKey().getFlag(), entry.getValue());
			}
		}
		return out;
	}

	public enum Flag
	{
		NAME("%name"), EVENT("%event"), REASON("%reason"), EXTRA("%extra"), TAG(
				"%tag"), AMOUNT("%amount");

		private String flag;

		private Flag(String flag)
		{
			this.flag = flag;
		}

		public String getFlag()
		{
			return flag;
		}
	}
}
