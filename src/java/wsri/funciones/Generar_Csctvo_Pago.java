/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wsri.funciones;

import java.sql.Connection;
import wsri.db.DBIndustria;
import wsri.db.DBPredial;
import wsri.db.DBRentas;

/**
 *
 * @author Admin
 */

//Esta clase contiene funciones destinadas a generar los 
//consecutivos de pago para el proceso de recaudo en línea
//de los impuestos PREDIAL y RENTAS VARIAS.
public class Generar_Csctvo_Pago {

    java.sql.CallableStatement cstm = null;
    

    //Genera el csctvo de pago para impuestos PREDIAL
    public String Csctvo_Pago_PREDIAL() throws java.sql.SQLException {

        String Csctvo_Predial = "";

        try {
            //Funcion que retorna el csctvo de Predial
            cstm = DBPredial.getConnection().prepareCall("{?=call SITFN002(?,?)}");

            //Registrar parametros de entrada
            cstm.setString(2, "PAL"); //Parametro asignado para el csctvo de recaudo en linea
            cstm.setString(3, "CSCTVO_PAGO.txt");

            //Registrar parametros de salida
            cstm.registerOutParameter(1, java.sql.Types.VARCHAR);

            cstm.execute();
            //respuesta
            Csctvo_Predial = cstm.getString(1);

        } catch (java.sql.SQLException ex) {
            System.out.println("Error al generar csctvo de pago recaudo en linea: " + ex.getMessage());
        } finally {
            cstm.close();
        }
        return Csctvo_Predial;
    }

    //Obtiene el csctvo de pago para impuestos de RENTAS
    public String Csctvo_Pago_RENTAS() throws java.sql.SQLException {

        String Csctvo_Rentas = "";
        try {
            //Funcion que retorna el csctvo de Rentas
            cstm = DBRentas.getConnection().prepareCall("{?=call SITFN002(?,?)}");

            //Registrar parametros de entrada
            cstm.setString(2, "PAL"); //Parametro asignado para el csctvo de recaudo en linea
            cstm.setString(3, "CSCTVO_PAGO.txt");

            //Registrar parametros de salida
            cstm.registerOutParameter(1, java.sql.Types.VARCHAR);

            cstm.execute();
            //respuesta
            Csctvo_Rentas = cstm.getString(1);

        } catch (java.sql.SQLException ex) {
            System.out.println("Error al generar csctvo de pago recaudo en linea: " + ex.getMessage());
        } finally {
            //Cierra conexion a base de datos
            cstm.close();
        }
        return Csctvo_Rentas;
    }

     //Obtiene el csctvo de pago para impuestos de Industria
    public String Csctvo_Pago_INDUSTRIA() throws java.sql.SQLException {

        String Csctvo_Industria = "";

        try {
            //Funcion que retorna el csctvo de Rentas
            cstm = DBIndustria.getConnection().prepareCall("{?=call SITFN002(?,?)}");

            //Registrar parametros de entrada
            cstm.setString(2, "PAL"); //Parametro asignado para el csctvo de recaudo en linea
            cstm.setString(3, "CSCTVO_PAGO.txt");

            //Registrar parametros de salida
            cstm.registerOutParameter(1, java.sql.Types.VARCHAR);

            cstm.execute();
            //respuesta
            Csctvo_Industria = cstm.getString(1);

        } catch (java.sql.SQLException ex) {
            System.out.println("Error al generar csctvo de pago recaudo en linea: " + ex.getMessage());
        } finally {
            //Cierra conexion a base de datos
            cstm.close();
        }
        return Csctvo_Industria;
    }
}
