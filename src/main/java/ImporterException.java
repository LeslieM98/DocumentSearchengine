/**
 * An own Exception class for the Importer class
 * 
 * @author Mahan Karimi Zaree
 * @version 0.0.1
 * @since 2018-11-13
 */

public class ImporterException extends Exception {

	/**
	 * Default constructor
	 */
	public ImporterException() {
		super();
	}

	/**
	 * Constructor that throws a message for the developer or tester that something
	 * goes wrong
	 * 
	 * @param msg - the message for the tester or developer
	 */

	public ImporterException(String msg) {
		super(msg);
	}

}
