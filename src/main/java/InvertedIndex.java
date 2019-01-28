import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements functions to get data from a database
 * 
 * @author Mahan Karimi Zaree
 *
 */
public class InvertedIndex {

	// connect to database  
	private Connection con = null;
	// a list to save the did and tf from a document

	public InvertedIndex() {
		try {
			con = DatabaseConnection.getConnection();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		if (con == null) {
			System.exit(1);
		}
	}

	/**
	 * A alternative function that checks if a database connection is available or
	 * not
	 * 
	 * @return true if database is connected and false if something goes wrong
	 * @throws SQLException - throw a sql exception if something goes wrong with our
	 *                      connection
	 */
	private boolean isDatabaseConnected() throws SQLException {
		return !con.isClosed();
	}

	/**
	 * A function that give by a given term the postings back
	 * 
	 * @param term - a term in a document
	 * @return indexList which contains Posting about a term
	 * @throws SQLException - if something goes wrong with the database
	 */
	public List<Posting> getIndexList(String term) throws SQLException {
		// prepare sql statement
		String sqlCommando = "SELECT did, tf FROM tfs WHERE term = ?";
		List <Posting> indexList = new ArrayList<>();
		if (isDatabaseConnected()) {
			PreparedStatement getPosting = con.prepareStatement(sqlCommando);
			getPosting.setString(1, term);

			ResultSet rs = getPosting.executeQuery();

			while (rs.next()) {
				Posting pos = new Posting(rs.getLong("did"), rs.getInt("tf"));
				indexList.add(pos);
			}

			rs.close();

		}

		return indexList;
	}
	
	
	/** 
	 * A function that gives a document frequency by given a Term 
	 * 
	 * @param term - a term in a document
	 * @return df - document frequency
	 * @throws SQLException - if something goes wrong with the database connection 
	 */
	
	public int getDF(String term) throws SQLException { 
		int df = 0 ;
		String sql = "SELECT df FROM dfs WHERE term = ?";
		
		if (isDatabaseConnected()) { 
			PreparedStatement getDF = con.prepareStatement(sql); 
			getDF.setString(1, term);
			
			ResultSet rs = getDF.executeQuery(); 
			while(rs.next()) {
			df = rs.getInt("df");
			}
			
			rs.close();
		}
		
		return df ; 
	}

	/**
	 * A function that gives the number of documents in a document collection 
	 * @return size - number of documents
	 * @throws SQLException - if something goes wrong with the database
	 */
	public int getSize() throws SQLException {
		int size = 0;
		String sqlCommando = "SELECT * FROM d";
		if (isDatabaseConnected()) {
			PreparedStatement getSize = con.prepareStatement(sqlCommando);

			ResultSet rs = getSize.executeQuery();

			size = rs.getInt("size");

			rs.close();
		}

		return size;
	}
	
	
	/**
	 * 
	 * @param id- document id
	 * @return length - the length from a given document 
	 * @throws SQLException - if something goes wrong with our database connection 
	 */
	
	public int getLength (long id) throws SQLException { 
		int length = 0 ; 
		String sql ="SELECT len from dls WHERE id = ? ";
		if (isDatabaseConnected()) {
			PreparedStatement getLength = con.prepareStatement(sql); 
			getLength.setLong(1, id);
			
			ResultSet rs = getLength.executeQuery(); 
			
			while (rs.next()) { 
				length = rs.getInt("");
			}
			
			rs.close();
		}
		
		return length;
	}

	public static void main(String[] args) {
		InvertedIndex test = new InvertedIndex();
		try {
			System.out.println(test.getSize());
		} catch (SQLException ex) {
			ex.getMessage();
		}
	}
}
