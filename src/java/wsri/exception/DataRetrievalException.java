/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wsri.exception;

/**
 *
 * @author Admin
 */
public class DataRetrievalException extends DataAccessException {

    /**
	 * contructor
	 */
	public DataRetrievalException() {
		super();
	}

	/**
	 * contructor
	 */
	public DataRetrievalException( String message ) {
		super( message );
	}

	/**
	 * contructor
	 */
	public DataRetrievalException( Throwable cause ) {
		super( cause );
	}

	/**
	 * contructor
	 */
	public DataRetrievalException( String message, Throwable cause ) {
		super( message, cause );
	}
}
