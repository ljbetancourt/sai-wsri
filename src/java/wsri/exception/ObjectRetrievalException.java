/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wsri.exception;

/**
 *
 * @author Admin
 */
public class ObjectRetrievalException extends DataRetrievalException{

    /**
	 * contructor
	 */
	public ObjectRetrievalException() {
		super();
	}

	/**
	 * contructor
	 */
	public ObjectRetrievalException( String message ) {
		super( message );
	}

	/**
	 * contructor
	 */
	public ObjectRetrievalException( Throwable cause ) {
		super( cause );
	}

	/**
	 * contructor
	 */
	public ObjectRetrievalException( String message, Throwable cause ) {
		super( message, cause );
	}
}
