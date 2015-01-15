/**
 * 
 */
package edu.phema.knime.exceptions;

/**
 * @author Huan
 *
 */
public class WrittenAlreadyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4478177970893314050L;

	/**
	 * 
	 */
	public WrittenAlreadyException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public WrittenAlreadyException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public WrittenAlreadyException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WrittenAlreadyException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public WrittenAlreadyException(int nodeId){
		super("Node " + nodeId + "has been written, and its folder has been created. ");
	}
	
	public WrittenAlreadyException(int nodeId, Throwable cause){
		super("Node " + nodeId + "has been written, and its folder has been created. ", cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public WrittenAlreadyException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
