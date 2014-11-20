/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wsri.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.tomcat.jdbc.pool.DataSource;

/**
 *
 * @author Admin
 */
public class DBPredial {

    static Connection con;

    public static Connection getConnection() {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/predial");
            con = ds.getConnection();
        } catch (NamingException ex) {
            Logger.getLogger(DBPredial.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBPredial.class.getName()).log(Level.SEVERE, null, ex);
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
