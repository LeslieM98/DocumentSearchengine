import java.sql.*;

/**
 * A class that connect to an sqlite database
 * 
 * @author Mahan Karimi Zaree
 *
 */
public class DatabaseConnection {

	//the path to the sqlite database 
	private static final String SQCONN = "jdbc:sqlite:/home/leslie/InformationRetrieval/nyt.sqlite";

	/**
	 * A function that connect to a given database 
	 * @return the connection point that we can use to get interface between our application and database
	 * @throws SQLException - if something goes wrong with the connection to our database
	 */
	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection(SQCONN);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		 	return null;
		 } 
	}
}