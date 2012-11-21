package com.mveritym.cashflow.config.localization;

import com.mveritym.cashflow.config.ConfigNode;

public enum LocalizeNode implements ConfigNode {
   
   /**
    * General.
    */
   PERMISSION_DENY("message.noPermission", "&7%tag &cLack permission: &b%extra"),
   CONSOLE_DENY("message.noConsole", "&7%tag &cCannot use command as console"),
   NOT_INTEGER("message.notIntenger", "&7%tag &6%extra &cis not an integer"),
   ENABLED("message.enabled", "&7%tag &fCashFlow &aenabled"),
   DISABLED("message.disabled", "&7%tag &fCashFlow &cdisabled"),
   /**
    * Command.
    */
   COMMAND_UNKNOWN("message.command.unknown",
         "&7%tag &cUnknown command '%extra'"),
   COMMAND_GIVE("message.command.give", "&7%tag &9/points give <name> <points>"),
   COMMAND_TAKE("message.command.take", "&7%tag &9/points take <name> <points>"),
   COMMAND_LOOK("message.command.look", "&7%tag &9/points look <name>"),
   COMMAND_PAY("message.command.pay", "&7%tag &9/points give <name> <points>"),
   COMMAND_SET("message.command.set", "&7%tag &9/points set <name> <points>"),
   COMMAND_RESET("message.command.reset", "&7%tag &9/points reset <name>"),
   COMMAND_ME("message.command.me", "&7%tag &9/points me"),
   /**
    * Economy.
    */
   ECONOMY_FAILURE("message.economy.failure",
         "&7%tag &cSomething went wrong with the transaction D:"),
   
   /**
    * Help.
    */
   HELP_HEADER("message.help.header", "&9======= &7%tag &9======="),
   HELP_ME("message.help.me", "&7/points me &6: Show current points"),
   HELP_GIVE("message.help.give",
         "&7/points give <name> <points> &6: Generate points for given player"),
   HELP_TAKE("message.help.take",
         "&7/points take <name> <points> &6: Take points from player"),
   HELP_LOOK("message.help.look",
         "&7/points give <name> &6: Lookup player's points"),
   HELP_SET("message.help.set",
         "&7/points set <name> <points> &6: Set player's points to amount"),
   HELP_RESET("message.help.reset",
         "&7/points reset <name> &6: Reset player's points to 0"),
   HELP_PAY("message.help.pay",
         "&7/points pay <name> <points> &6: Send points to given player");

   private String path, def;

   private LocalizeNode(String path, String def) {
      this.path = path;
      this.def = def;
   }

   @Override
   public String getPath() {
      return path;
   }

   @Override
   public String getDefaultValue() {
      return def;
   }

   @Override
   public VarType getVarType() {
      return VarType.STRING;
   }
}
