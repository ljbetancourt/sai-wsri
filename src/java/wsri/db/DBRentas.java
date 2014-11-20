/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wsri.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.tomcat.jdbc.pool.DataSource;
import wsri.exception.DAOException;

/**
 *
 * @author Admin
 */
public class DBRentas {

    static Connection con;

    public static Connection getConnection() {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/rentas");
            con = ds.getConnection();

        } catch (NamingException ex) {
            Logger.getLogger(DBRentas.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBRentas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    public static void close() {
        try {
            con.close();
        } catch (SQLException ex) {
        }
    }
}
