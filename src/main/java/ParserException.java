/**
 * An own Exception class for the parser class
 * 
 * @author Leslie Marxen
 * @version 0.0.1
 * @since 2018-11-17
 */

public class ParserException extends Exception {

	/**
	 * Default constructor
	 */
	public ParserException() {
		super();
	}

	/**
	 * Constructor that throws a message for the developer or tester that something
	 * goes wrong
	 * 
	 * @param msg - the message for the tester or developer
	 */

	public ParserException(String msg) {
		super(msg);
	}

}
