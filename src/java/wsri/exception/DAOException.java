/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wsri.exception;

import java.sql.SQLException;
/**
 *
 * @author Admin
 */
public class DAOException extends SQLException {

    public DAOException() {
		super();
	}

	/**
	 * contructor
	 */
	public DAOException( String message ) {
		super( message );
	}

	/**
	 * contructor
	 */
	public DAOException( Throwable cause ) {
		//super( cause );
	}

	/**
	 * contructor
	 */
	public DAOException( String message, Throwable cause ) {
		//super( message, cause );
	}
}
