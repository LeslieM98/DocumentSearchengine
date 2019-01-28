/**
 * An own Exception class for document class
 * 
 * @author Meris Krupic 
 * @version 0.0.1 
 * @since 2018-11-15
 */

public class DocumentException extends Exception {

	/**
	 * Default constructor
	 */
	public DocumentException() {
		super();
	}

	/**
	 * Constructor that throws a message for the developer or tester that something
	 * goes wrong
	 * 
	 * @param msg - the message for the tester or developer
	 */

	public DocumentException(String msg) {
		super(msg);
	}

}
