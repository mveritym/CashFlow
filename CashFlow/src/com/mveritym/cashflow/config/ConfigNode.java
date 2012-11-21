package com.mveritym.cashflow.config;

public interface ConfigNode {
   
   /**
    * Get the config path.
    * 
    * @return Config path.
    */
   public String getPath();

   /**
    * Get the variable type.
    * 
    * @return Variable type.
    */
   public VarType getVarType();

   /**
    * Get the default value.
    * 
    * @return Default value.
    */
   public Object getDefaultValue();

   /**
    * Variable Types.
    */
   public enum VarType {
      STRING,
      INTEGER,
      DOUBLE,
      BOOLEAN,
      LIST;
   }

}
