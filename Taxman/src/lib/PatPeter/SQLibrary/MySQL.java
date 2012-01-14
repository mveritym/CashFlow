/**
 * MySQL
 * Inherited subclass for making a connection to a MySQL server.
 *
 * Date Created: 2011-08-26 19:08
 * @author PatPeter
 */
package lib.PatPeter.SQLibrary;

/*
 * MySQL
 */
//import java.net.MalformedURLException;

/*
 * Both
 */
//import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.logging.Logger;
import java.util.logging.Logger;
//import com.sun.rowset.JdbcRowSetImpl;

public class MySQL extends DatabaseHandler {
	private String hostname = "localhost";
	private String portnmbr = "3306";
	private String username = "minecraft";
	private String password = "";
	private String database = "minecraft";

	public MySQL(Logger log,
				 String prefix,
				 String hostname,
				 String portnmbr,
				 String database,
				 String username,
				 String password) {
		super(log,prefix,"[MySQL] ");
		this.hostname = hostname;
		this.portnmbr = portnmbr;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	/*@Override
	public void writeInfo(String toWrite) {
		if (toWrite != null) {
			this.log.info(this.PREFIX + this.DATABASE_PREFIX + toWrite);
		}
	}

	@Override
	public void writeError(String toWrite, boolean severe) {
		if (toWrite != null) {
			if (severe) {
				this.log.severe(this.PREFIX + this.DATABASE_PREFIX + toWrite);
			} else {
				this.log.warning(this.PREFIX + this.DATABASE_PREFIX + toWrite);
			}
		}
	}*/

	@Override
	protected boolean initialize() {
		try {
			Class.forName("com.mysql.jdbc.Driver"); // Check that server's Java has MySQL support.
			return true;
	    } catch (ClassNotFoundException e) {
	    	this.writeError("Class Not Found Exception: " + e.getMessage() + ".", true);
	    	return false;
	    }
	}

	@Override
	public Connection open() {
		if (initialize()) {
			String url = "";
		    try {
				url = "jdbc:mysql://" + this.hostname + ":" + this.portnmbr + "/" + this.database;
				return DriverManager.getConnection(url, this.username, this.password);
		    } catch (SQLException e) {
		    	this.writeError(url,true);
		    	this.writeError("Could not be resolved because of an SQL Exception: " + e.getMessage() + ".", true);
		    }
		}
		return null;
	}

	@Override
	public void close() {
		Connection connection = open();
		try {
			if (connection != null)
				connection.close();
		} catch (Exception e) {
			this.writeError("Failed to close database connection: " + e.getMessage(), true);
		}
	}

	@Override
	public Connection getConnection() {
		//if (this.connection == null)
		return open();
		//return this.connection;
	}

	@Override
	public boolean checkConnection() { // http://forums.bukkit.org/threads/lib-tut-mysql-sqlite-bukkit-drivers.33849/page-4#post-701550
		Connection connection = this.open();
		if (connection != null)
			return true;
		return false;
	}

	@Override
	public ResultSet query(String query) {
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null/*new JdbcRowSetImpl()*/;
		try {
			connection = open();
			//WARN ODR_OPEN_DATABASE_RESOURCE
			/*
			 * The method creates a database resource
			 * (such as a database connection or row set),
			 * does not assign it to any fields, pass it to other
			 * methods, or return it, and does not appear to close
			 * the object on all paths out of the method.
			 * Failure to close database resources on all paths out
			 * of a method may result in poor performance, and could
			 *  cause the application to have problems communicating
			 *   with the database.
			 */
		    statement = connection.createStatement();
		    result = statement.executeQuery("SELECT CURTIME()");

		    switch (this.getStatement(query)) {
			    case SELECT:
				    result = statement.executeQuery(query);
				    return result;

			    default:
			    	statement.executeUpdate(query);
			    	return result;
		    }
		} catch (SQLException e) {
			this.writeError("Error in SQL query: " + e.getMessage(), false);
		}
		return result;
	}

	@Override
	public PreparedStatement prepare(String query) {
		Connection connection = null;
		PreparedStatement ps = null;
		try
		{
			connection = open();
			ps = connection.prepareStatement(query);
			return ps;
		} catch(SQLException e) {
			if(!e.toString().contains("not return ResultSet"))
				this.writeError("Error in SQL prepare() query: " + e.getMessage(), false);
		}
		return ps;
	}

	@Override
	public boolean createTable(String query) {
		Statement statement = null;
		try {
			this.connection = this.open();
			//WARN RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE
			/*
			 * A value is checked here to see whether it is null,
			 * but this value can't be null because it was previously
			 * dereferenced and if it were null a null pointer
			 * exception would have occurred at the earlier
			 * dereference. Essentially, this code and the previous
			 * dereference disagree as to whether this value is
			 * allowed to be null. Either the check is redundant or
			 * the previous dereference is erroneous.
			 */
			if (query.equals("") || query == null) {
				this.writeError("SQL query empty: createTable(" + query + ")", true);
				return false;
			}
			//WARN ODR_OPEN_DATABASE_RESOURCE
			/*
			 * The method creates a database resource
			 * (such as a database connection or row set),
			 * does not assign it to any fields, pass it to other
			 * methods, or return it, and does not appear to close
			 * the object on all paths out of the method.
			 * Failure to close database resources on all paths out
			 * of a method may result in poor performance, and could
			 *  cause the application to have problems communicating
			 *   with the database.
			 */
			statement = connection.createStatement();
		    statement.execute(query);
		    return true;
		} catch (SQLException e) {
			this.writeError(e.getMessage(), true);
			return false;
		} catch (Exception e) {
			this.writeError(e.getMessage(), true);
			return false;
		}
	}

	@Override
	public boolean checkTable(String table) {
		try {
			Connection connection = open();
			//this.connection = this.open();
			//WARN ODR_OPEN_DATABASE_RESOURCE
			/*
			 * The method creates a database resource
			 * (such as a database connection or row set),
			 * does not assign it to any fields, pass it to other
			 * methods, or return it, and does not appear to close
			 * the object on all paths out of the method.
			 * Failure to close database resources on all paths out
			 * of a method may result in poor performance, and could
			 *  cause the application to have problems communicating
			 *   with the database.
			 */
		    Statement statement = connection.createStatement();

		    ResultSet result = statement.executeQuery("SELECT * FROM " + table);

		    if (result == null)
		    	return false;
		    if (result != null)
		    	return true;
		} catch (SQLException e) {
			if (e.getMessage().contains("exist")) {
				return false;
			} else {
				this.writeError("Error in SQL query: " + e.getMessage(), false);
			}
		}


		if (query("SELECT * FROM " + table) == null) return true;
		return false;
	}

	@Override
	public boolean wipeTable(String table) {
		//Connection connection = null;
		Statement statement = null;
		String query = null;
		try {
			if (!this.checkTable(table)) {
				this.writeError("Error wiping table: \"" + table + "\" does not exist.", true);
				return false;
			}
			//connection = open();
			this.connection = this.open();
		    statement = this.connection.createStatement();
		    query = "DELETE FROM " + table + ";";
		    statement.executeUpdate(query);

		    return true;
		} catch (SQLException e) {
			if (!e.toString().contains("not return ResultSet"))
				return false;
		}
		return false;
	}
}