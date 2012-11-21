package com.mveritym.cashflow.config;

/**
 * Represents a configuration node from the config.yml.
 * 
 * @author Mitsugaru
 * 
 */
public enum RootConfigNode implements ConfigNode {
   /**
    * MySQL.
    */
   MYSQL_USE("mysql.use", VarType.BOOLEAN, false),
   MYSQL_HOST("mysql.host", VarType.STRING, "localhost"),
   MYSQL_PORT("mysql.port", VarType.INTEGER, 3306),
   MYSQL_DATABASE("mysql.database", VarType.STRING, "minecraft"),
   MYSQL_USER("mysql.user", VarType.STRING, "username"),
   MYSQL_PASSWORD("mysql.password", VarType.STRING, "pass"),
   MYSQL_TABLE_PREFIX("mysql.tablePrefix", VarType.STRING, "ks_"),
   MYSQL_IMPORT("mysql.import", VarType.BOOLEAN, false),
   /**
    * Debug.
    */
   DEBUG_CONFIG("debug.config", VarType.BOOLEAN, false),
   DEBUG_ECONOMY("debug.economy", VarType.BOOLEAN, false),
   /**
    * Version.
    */
   VERSION("version", VarType.STRING, "2.0");

   /**
    * Config path.
    */
   private String path;
   /**
    * Default value.
    */
   private Object def;
   /**
    * Variable type.
    */
   private VarType vartype;

   /**
    * Private constructor
    * 
    * @param path
    *           - Config path.
    * @param vartype
    *           - Variable type.
    * @param def
    *           - Default value.
    */
   private RootConfigNode(String path, VarType vartype, Object def) {
      this.path = path;
      this.vartype = vartype;
      this.def = def;
   }

   /**
    * Get the config path.
    * 
    * @return Config path.
    */
   public String getPath() {
      return this.path;
   }

   /**
    * Get the variable type.
    * 
    * @return Variable type.
    */
   public VarType getVarType() {
      return this.vartype;
   }

   /**
    * Get the default value.
    * 
    * @return Default value.
    */
   public Object getDefaultValue() {
      return this.def;
   }
}
