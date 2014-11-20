/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wsri.exception;

/**
 *
 * @author Admin
 */
public class DataAccessException extends DAOException {

    /**
	 * contructor
	 */
	public DataAccessException() {
		super();
	}

	/**
	 * contructor
	 */
	public DataAccessException( String message ) {
		super( message );
	}

	/**
	 * contructor
	 */
	public DataAccessException( Throwable cause ) {
		super( cause );
	}

	/**
	 * contructor
	 */
	public DataAccessException( String message, Throwable cause ) {
		super( message, cause );
	}
}
