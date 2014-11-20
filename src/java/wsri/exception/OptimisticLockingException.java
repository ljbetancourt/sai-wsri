/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wsri.exception;

/**
 *
 * @author Admin
 */
public class OptimisticLockingException extends DataAccessException{

    /**
	 * contructor
	 */
	public OptimisticLockingException() {
		super();
	}

	/**
	 * contructor
	 */
	public OptimisticLockingException( String message ) {
		super( message );
	}

	/**
	 * contructor
	 */
	public OptimisticLockingException( Throwable cause ) {
		super( cause );
	}

	/**
	 * contructor
	 */
	public OptimisticLockingException( String message, Throwable cause ) {
		super( message, cause );
	}
}
