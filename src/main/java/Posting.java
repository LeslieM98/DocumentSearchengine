/**
 * A class Posting that represent
 * 
 * @author Mahan Karimi Zaree
 *
 */
public class Posting {

	// did represented the document id
	private long did = 0;

	// tf represented the term frequency
	private int tf = 0;

	/**
	 * Constructor to create a Posting object
	 * 
	 * @param did - document id
	 * @param tf  - term frequency
	 */
	public Posting(long did, int tf) {
		this.did = did;
		this.tf = tf;
	}

	/**
	 * Get method for the attribute did
	 * 
	 * @return did - document id
	 */
	public long getDid() {
		return did;
	}

	/**
	 * Get method for the attribute tf
	 * 
	 * @return tf - term frequency
	 */
	public int getTf() {
		return tf;
	}

	/**
	 * Set method for the attribute did
	 * 
	 * @param did - new document id
	 */
	public void setDid(long did) {
		this.did = did;
	}

	/**
	 * Set method for the attribute tf
	 * 
	 * @param tf - new term frequency
	 */
	public void setTf(int tf) {
		this.tf = tf;
	}

	@Override
	public String toString() {
		return "Posting [did=" + did + ", tf=" + tf + "]";
	}
}