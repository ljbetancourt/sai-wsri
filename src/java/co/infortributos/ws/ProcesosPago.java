/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.infortributos.ws;

import java.io.IOException;
import java.sql.PreparedStatement;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import wsri.db.DBIndustria;
import wsri.db.DBPredial;
import wsri.db.DBRentas;

/**
 *
 * @author Admin
 */
@WebService()
public class ProcesosPago {

    //Variables para Proceso Validar Factura
    String ResultValFact = "";
    //Variables para Proceso RecaudoPago
    String ResultRecPago = "";

    /**
     * Web service operation
     *
     * @param COD_EAN
     * @param NMRO_DCMNTO
     * @param VLOR_PAGO
     * @param FCHA_VENCI
     * @return
     * @throws java.io.IOException
     */
    //Esta función se encarga de Validar que los datos de una factura sean
    //totalmente validos para iniciar el proceso de recaudo y recibe como
    //parametros de entrada los datos contenidos en el codigo de barras que son:
    //Código EAN, Numero de Factura, Valor a pagar y fecha de vencimiento;
    //y retorna solo un parametro ResultValFact donde indica el estado de la factura.
    @WebMethod(operationName = "ValidarFactura")
    public String ValidarFactura(@WebParam(name = "COD_EAN") String COD_EAN, @WebParam(name = "NMRO_DCMNTO") String NMRO_DCMNTO, @WebParam(name = "VLOR_PAGO") String VLOR_PAGO, @WebParam(name = "FCHA_VENCI") String FCHA_VENCI) throws IOException {
        //TODO write your implementation code here:

        String StrValFact = "";
        String StrValNumDocDec = "";
        String StrValRan = "";
        String StrValNumDoc = "";
        String VlorTtalPagar = "";
        String TipoProceso = "";
        String FechaVenci = "";
        String FCHA_Actual = "";
        String HRA_Actual = "";
        String TipoRecibo = "";

        if (COD_EAN.equals("") || NMRO_DCMNTO.equals("") || VLOR_PAGO.equals("") || FCHA_VENCI.equals("")) {
            ResultValFact = "1+Debe diligenciar todos los campos.";
        } else {
            if (isNumeric(COD_EAN) == false) { //Valida que sea un dato numérico
                ResultValFact = "1+Código EAN no valido, verifique que sea un dato numérico.";
            } else {
                if (isNumeric(NMRO_DCMNTO) == false) { //Valida que sea un dato numérico
                    ResultValFact = "1+Número de documento no valido, verifique que sea un dato numérico.";
                } else {
                    if (isNumeric(VLOR_PAGO) == false) { //Valida que sea un dato numérico
                        ResultValFact = "1+Monto a pagar no valido, verifique que sea un dato numérico.";
                    } else {
                        //logica del programa
                        ResultValFact = "";
                        try {
                            //Se valida que sea el codigo EAN indicado para inicar el proceso
                            if (COD_EAN.equals("7709998010185")) {
                                //----------------------------------------------//
                                //          OPERACION PREDIAL                  //
                                //--------------------------------------------//

                                PreparedStatement pst = null;
                                ResultSet rs = null;

                                try {

                                    //Validar si existe un pago registrado para este numero de documento.
                                    StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = ?";
                                    pst = DBPredial.getConnection().prepareStatement(StrValNumDoc);
                                    pst.setString(1, NMRO_DCMNTO.trim());
                                    rs = pst.executeQuery();

                                    if (rs.next()) {
                                        ResultValFact = "2+Existe un pago registrado para este recibo.";
                                        pst.close();
                                        rs.close();
                                    } else {
                                        //Busca los datos pertinentes para realizar validaciones de la factura
                                        StrValFact = "SELECT trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCMNTO, D.TPO_DCMNTO, "
                                                + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual "
                                                + " FROM DOCUMENTOS D "
                                                + " WHERE D.NMRO_DCMNTO = ? and to_char(D.FCHA_VNCE,'yyyymmdd') = ? and rownum <= 1";

                                        pst = DBPredial.getConnection().prepareStatement(StrValFact);
                                        pst.setString(1, NMRO_DCMNTO);
                                        pst.setString(2, FCHA_VENCI);
                                        rs = pst.executeQuery();

                                        if (rs.next()) {
                                            VlorTtalPagar = rs.getString(1);  //valor del documento
                                            FechaVenci = rs.getString(2);     //Fecha vencimiento de la factura
                                            TipoProceso = rs.getString(3);
                                            //Fecha y Hora actual tomada del servidor.
                                            FCHA_Actual = rs.getString(4);
                                            HRA_Actual = rs.getString(5);

                                            //Valida que el pago se acepte en el horario estipulado
                                            int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));

                                            //Calculo de fechas.
                                            int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                            //si validar_fecha es mayor que cero el documento esta vencido

                                            if (validar_fecha > 0) {
                                                ResultValFact = "4+El documento se encuentra vencido.";
                                            } else {
                                                //Valida que el valor a pagar de la factura sea el indicado
                                                if (VlorTtalPagar.equals(VLOR_PAGO)) {
                                                    //para pagos total
                                                    ResultValFact = "0+Ok";
                                                } else {
                                                    ResultValFact = "5+El valor a pagar no coresponde para este número de recibo.";
                                                }

                                            }
                                        } else {
                                            ResultValFact = "6+Recibo no valido para procesar.";
                                        }
                                    }
                                } catch (java.sql.SQLException ex) {
                                    ResultValFact = ex.getMessage();
                                } finally {
                                    rs.close();
                                    pst.close();
                                    StrValFact = "";
                                }//FIN OPERACIONES PREDIAL
                            } else if (COD_EAN.equals("7709998069305")
                                    || COD_EAN.equals("7709998642836")
                                    || COD_EAN.equals("7709998372979")
                                    || COD_EAN.equals("7709998666740")
                                    || COD_EAN.equals("7709998310780")
                                    || COD_EAN.equals("7709998704633")
                                    || COD_EAN.equals("7709998078888")
                                    || COD_EAN.equals("7709998755956")
                                    || COD_EAN.equals("7709998026872")
                                    || COD_EAN.equals("7709998123090")) {//OPERACION RENTAS
                                //----------------------------------------------//
                                //          OPERACION RENTAS                   //
                                //--------------------------------------------//
                                PreparedStatement pst = null;
                                ResultSet rs = null;

                                try {

                                    //Validar si existe un pago registrado para este numero de documento.
                                    StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = " + NMRO_DCMNTO.trim() + "";
                                    pst = DBRentas.getConnection().prepareStatement(StrValNumDoc);
                                    pst.setString(1, NMRO_DCMNTO.trim());
                                    rs = pst.executeQuery();
                                    if (rs.next()) {
                                        ResultValFact = "2+Existe un pago registrado para este recibo.";
                                        pst.close();
                                        rs.close();
                                    } else {
                                        //Busca los datos pertinentes para realizar validaciones de la factura
                                        StrValFact = "SELECT trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCE,D.TPO_DCMNTO, "
                                                + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual "
                                                + " FROM DOCUMENTOS D "
                                                + " WHERE D.NMRO_DCMNTO = ? and to_char(D.FCHA_VNCE ,'yyyymmdd') = ? and rownum <= 1";

                                        pst = DBRentas.getConnection().prepareStatement(StrValFact);
                                        pst.setString(1, NMRO_DCMNTO);
                                        pst.setString(2, FCHA_VENCI);
                                        rs = pst.executeQuery();

                                        if (rs.next()) {
                                            VlorTtalPagar = rs.getString(1);  //valor de la deuda
                                            FechaVenci = rs.getString(2);        //Fecha vencimiento de la factura
                                            TipoProceso = rs.getString(3);
                                            //Fecha y Hora actual tomada del servidor.
                                            FCHA_Actual = rs.getString(4);
                                            HRA_Actual = rs.getString(5);

                                            //Valida que el pago se acepte en el horario estipulado
                                            int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));
                                            //Calculo de fechas.
                                            int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                            //si validar_fecha es mayor que cero el documento esta vencido
                                            //si validar_fecha en igual o menor que cero puede pagar
                                            if (validar_fecha > 0) {
                                                ResultValFact = "4+El documento se encuentra vencido.";
                                            } else {
                                                //Valida que el valor a pagar de la factura sea el indicado
                                                if (VlorTtalPagar.equals(VLOR_PAGO)) {
                                                    ResultValFact = "0+Ok";
                                                } else {
                                                    ResultValFact = "5+El valor a pagar no coresponde para este número de recibo.";
                                                }
                                            }

                                        } else {
                                            ResultValFact = "6+Recibo no valido.";
                                        }
                                    }
                                } catch (java.sql.SQLException ex) {
                                    ResultValFact = ex.getMessage();
                                } finally {
                                    pst.close();
                                    rs.close();
                                    StrValFact = "";
                                }
                            } else if (COD_EAN.equals("7709998151109")) {//OPERACION INDUSTRIA
                                //----------------------------------------------//
                                //          OPERACION INDUSTRIA                  //
                                //--------------------------------------------//
                                PreparedStatement pst = null;
                                ResultSet rs = null;

                                try {

                                    //Validar si existe una declaracion registrado para este numero.
                                    StrValRan = "SELECT R.CDGO FROM RANGO_DOCUMENTOS R WHERE ? BETWEEN R.CNSCTVO_INCIAL AND R.CNSCTVO_FNAL";
                                    pst = DBIndustria.getConnection().prepareStatement(StrValRan);
                                    pst.setString(1, NMRO_DCMNTO);
                                    rs = pst.executeQuery();
                                    if (rs.next()) {
                                        TipoRecibo = rs.getString(1);
                                        //Validar si existe un pago registrado para este numero de documento.
                                        StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = ?";
                                        pst = DBIndustria.getConnection().prepareStatement(StrValNumDoc);
                                        pst.setString(1, NMRO_DCMNTO.trim());
                                        rs = pst.executeQuery();
                                        if (rs.next()) {
                                            ResultValFact = "2+Existe un pago registrado para este recibo.";
                                        } else {
                                            //Teniendo en cuenta el tipo de documento, recupera los datos para realizar validaciones de la factura
                                            if (TipoRecibo.equals("DOL")) {
                                                //es una declaracion
                                                StrValFact = "SELECT trim(ODDP.VLOR_DCLRDO) VLOR_DCLRDO , to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') FCHA_VNCMNTO, 'DNO' TPO_DCMNTO, "
                                                        + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual "
                                                        + " FROM OL_DECLARACION_PRIVADA ODP, OL_DETALLE_DECLARACION_PRIVADA ODDP "
                                                        + " WHERE ODP.CNSCTVO = ODDP.CNSCTVO AND ODP.CNSCTVO = ? and to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') >= to_char(sysdate,'yyyymmdd') "
                                                        + " AND ODDP.CDGO_ITM = (SELECT MAX(DE.CDGO_ITM) FROM OL_DETALLE_DECLARACION_PRIVADA DE WHERE DE.CNSCTVO = ?)";
                                            } else {
                                                //Busca los datos pertinentes para realizar validaciones de la factura
                                                StrValFact = "SELECT trim(ODDP.VLOR_DCLRDO) VLOR_DCLRDO , to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') FCHA_VNCMNTO, D.TPO_DCMNTO, "
                                                        + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual"
                                                        + " FROM DOCUMENTOS D WHERE D.NMRO_DCMNTO = ?";
                                            }

                                            pst = DBIndustria.getConnection().prepareStatement(StrValFact);
                                            pst.setString(1, NMRO_DCMNTO);
                                            pst.setString(2, NMRO_DCMNTO);
                                            rs = pst.executeQuery();

                                            if (rs.next()) {
                                                VlorTtalPagar = rs.getString(1);  //valor de la deuda
                                                FechaVenci = rs.getString(2);        //Fecha vencimiento de la factura
                                                TipoProceso = rs.getString(3);
                                                //Fecha y Hora actual tomada del servidor.
                                                FCHA_Actual = rs.getString(4);
                                                HRA_Actual = rs.getString(5);

                                                //Valida que el pago se acepte en el horario estipulado
                                                int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));

                                                //Calculo de fechas.
                                                int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                                //si validar_fecha es mayor que cero el documento esta vencido
                                                //si validar_fecha en igual o menor que cero puede pagar
                                                if (validar_fecha > 0) {
                                                    ResultValFact = "4+El documento se encuentra vencido.";
                                                } else {
                                                    //Valida que el valor a pagar de la factura sea el indicado
                                                    if (VlorTtalPagar.equals(VLOR_PAGO)) {
                                                        ResultValFact = "0+Ok";
                                                    } else {
                                                        ResultValFact = "5+El valor a pagar no coresponde para este número de recibo.";
                                                    }
                                                }
                                            } else {
                                                ResultValFact = "6+Recibo no valido.";
                                            }
                                        }
                                    } else {
                                        ResultValFact = "6+Recibo no valido para procesar.";
                                    }
                                } catch (java.sql.SQLException ex) {
                                    ResultValFact = ex.getMessage();
                                } finally {
                                    pst.close();
                                    rs.close();
                                    StrValFact = "";
                                }
                            } else {
                                ResultValFact = "7+Código EAN no admitido.";
                            }//FIN OPERACIONES RENTAS
                        } catch (Exception ex) {
                        } finally {
                            COD_EAN = "";
                        }
                    }
                }
            }
        }
        return ResultValFact;
    }

    /**
     * Web service operation
     *
     * @param COD_EAN
     * @param NMRO_DCMNTO
     * @param VLOR_PAGO
     * @param FCHA_VENCI
     * @param FCHA_PAGO
     * @param FOR_PAGO
     * @param COD_BANCO
     * @param REF_SUC
     * @return
     */
    //Esta función se encarga de Registrar el pago en la base de datos,
//recibe como parametros de entrada los datos contenidos en el codigo de
//barras que son:Código EAN, Numero de Factura, Valor a pagar, fecha de vencimiento
//fecha de pago, forma de pago, código banco y codigo sucursal; y retorna
//solo un parametro ResultRecPago donde indica el estado del proceso.
    @WebMethod(operationName = "RecaudoPago")
    public String RecaudoPago(
            @WebParam(name = "COD_EAN") String COD_EAN,
            @WebParam(name = "NMRO_DCMNTO") String NMRO_DCMNTO,
            @WebParam(name = "VLOR_PAGO") String VLOR_PAGO,
            @WebParam(name = "FCHA_VENCI") String FCHA_VENCI,
            @WebParam(name = "FCHA_PAGO") String FCHA_PAGO,
            @WebParam(name = "FOR_PAGO") String FOR_PAGO,
            @WebParam(name = "COD_BANCO") String COD_BANCO,
            @WebParam(name = "REF_SUC") String REF_SUC) {

        String CSCTVO_PAGO = ""; //Consecutivo de guardado
        String StrValFact = "";
        String StrRegPago = "";
        String StrValNumDoc = "";
        String Rfrncia_catastral = "";
        String VlorTtalPagar = "";
        String FechaVenci = "";
        String TipoProceso = "";
        String FCHA_Actual = "";
        String HRA_Actual = "";
        String FCHA_HORA_REGISTRO = "";
        String TipoRecibo = "";
        String StrValRan = "";
        String CODIGO_BANCO = "";

        if (COD_EAN.equals("") || NMRO_DCMNTO.equals("") || VLOR_PAGO.equals("") || FCHA_VENCI.equals("") || FCHA_PAGO.equals("") || FOR_PAGO.equals("") || COD_BANCO.equals("") || REF_SUC.equals("")) {
            ResultRecPago = "1+Debe diligenciar todos los campos.";
        } else {
            if (isNumeric(COD_EAN) == false) { //Valida que sea un dato numérico
                ResultRecPago = "1+Código EAN no valido, verifique que sea un dato numérico.";
            } else {
                if (isNumeric(NMRO_DCMNTO) == false) { //Valida que sea un dato numérico
                    ResultRecPago = "1+Número de documento no valido, verifique que sea un dato numérico.";
                } else {
                    if (isNumeric(VLOR_PAGO) == false) { //Valida que sea un dato numérico
                        ResultRecPago = "1+Monto a pagar no valido, verifique que sea un dato numérico.";
                    } else {
                        if (isDate(FCHA_PAGO) == false) {
                            ResultRecPago = "1+Formato de fecha invalido, debe estar en YYYYMMDD.";
                        } else {
                            //logica del programa
                            ResultRecPago = "";
                            try {
                                //Se valida que sea el codigo EAN indicado para inicar el proceso
                                if (COD_EAN.equals("7709998010185")) {
                                    //----------------------------------------------//
                                    //          OPERACION PREDIAL                  //
                                    //--------------------------------------------//

                                    PreparedStatement pst = null;
                                    ResultSet rsp = null;

                                    try {

                                        //Validar si existe un pago registrado para este numero de documento.
                                        StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = ?";
                                        pst = DBPredial.getConnection().prepareStatement(StrValNumDoc);
                                        pst.setString(1, NMRO_DCMNTO.trim());
                                        rsp = pst.executeQuery();
                                        if (rsp.next()) {
                                            ResultRecPago = "2+Existe un pago registrado para este recibo.";
                                        } else {
                                            //Busca los datos pertinentes para realizar validaciones de la factura
                                            StrValFact = "SELECT D.IDNTFCCION, trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCMNTO, D.TPO_DCMNTO, "
                                                    + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual, to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora "
                                                    + " FROM DOCUMENTOS D "
                                                    + " WHERE D.NMRO_DCMNTO = ? and rownum <= 1";

                                            pst = DBPredial.getConnection().prepareStatement(StrValFact);
                                            pst.setString(1, NMRO_DCMNTO.trim());
                                            rsp = pst.executeQuery(StrValFact);

                                            if (rsp.next()) {
                                                Rfrncia_catastral = rsp.getString(1);
                                                VlorTtalPagar = rsp.getString(2);  //valor de la deuda
                                                FechaVenci = rsp.getString(3);        //Fecha vencimiento de la factura                                                
                                                TipoProceso = rsp.getString(4);         // Codigo proceso, indica si es pago cuotas o total

                                                //Fecha y Hora actual tomada del servidor.
                                                FCHA_Actual = rsp.getString(5);
                                                HRA_Actual = rsp.getString(6);
                                                FCHA_HORA_REGISTRO = rsp.getString(7);

                                                //Valida que el pago se acepte en el horario estipulado
                                                int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));

                                                int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                                //si validar_fecha es mayor que cero el documento esta vencido
                                                //si validar_fecha en igual o menor que cero puede pagar
                                                if (validar_fecha > 0) {
                                                    ResultValFact = "4+El documento se encuentra vencido.";
                                                } else {
                                                    CODIGO_BANCO = COD_BANCO;
                                                    //Valida que el valor a pagar de la factura sea el indicado
                                                    wsri.funciones.Generar_Csctvo_Pago csctvo = new wsri.funciones.Generar_Csctvo_Pago();
                                                    if (VlorTtalPagar.equals(VLOR_PAGO)) {
                                                        //Tipo de pagos normal
                                                        CSCTVO_PAGO = csctvo.Csctvo_Pago_PREDIAL();
                                                        StrRegPago = "insert into PAGOS_EN_LINEA PEL (PEL.NMRO_DCMNTO, PEL.MNTO_PGO, PEL.FCHA_VNCMNTO, PEL.FCHA_PGO, PEL.FRMA_PGO, PEL.CDGO_BNCO, PEL.CDGO_SCURSAL, PEL.CNSCTVO_PGO, PEL.APLCDO, PEL.FCHA_REGISTRO, PEL.RFRNCIA_CTSTRAL) "
                                                                + " values (" + NMRO_DCMNTO + ", " + VLOR_PAGO + ", to_date('" + FCHA_VENCI + "','yyyy/mm/dd'), to_date('" + FCHA_PAGO + "','yyyy/mm/dd'), '" + FOR_PAGO + "', '" + CODIGO_BANCO + "', '" + REF_SUC + "', '" + CSCTVO_PAGO + "', 'N', to_date('" + FCHA_HORA_REGISTRO + "','yyyy/mm/dd HH24:MI'), '" + Rfrncia_catastral + "')";

                                                        pst = DBPredial.getConnection().prepareStatement(StrRegPago);
                                                        pst.executeUpdate();
                                                        ResultRecPago = "0+Ok, pago registrado.";

                                                    } else {
                                                        ResultRecPago = "5+El valor a pagar no coresponde para este número de recibo.";
                                                    }
                                                }
                                            } else {
                                                ResultRecPago = "6+Recibo no valido para procesar.";
                                            }
                                        }
                                    } catch (java.sql.SQLException ex) {
                                        ResultRecPago = ex.getMessage();
                                    } finally {
                                        pst.close();
                                        rsp.close();
                                        StrValFact = "";
                                        COD_EAN = "";
                                    }//FIN OPERACION PREDIAL

                                } else if (COD_EAN.equals("7709998069305")
                                        || COD_EAN.equals("7709998642836")
                                        || COD_EAN.equals("7709998372979")
                                        || COD_EAN.equals("7709998666740")
                                        || COD_EAN.equals("7709998310780")
                                        || COD_EAN.equals("7709998704633")
                                        || COD_EAN.equals("7709998078888")
                                        || COD_EAN.equals("7709998755956")
                                        || COD_EAN.equals("7709998026872")
                                        || COD_EAN.equals("7709998123090")) {
                                    //----------------------------------------------//
                                    //          OPERACION RENTAS                   //
                                    //--------------------------------------------//

                                    PreparedStatement pst = null;
                                    ResultSet rsp = null;

                                    try {
                                        //Validar si existe un pago registrado para este numero de documento.
                                        StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = ? ";
                                        pst = DBRentas.getConnection().prepareStatement(StrValNumDoc);
                                        pst.setString(1, NMRO_DCMNTO.trim());
                                        rsp = pst.executeQuery(StrValNumDoc);

                                        if (rsp.next()) {
                                            ResultRecPago = "2+Existe un pago registrado para este recibo.";
                                        } else {
                                            //Busca los datos pertinentes para realizar validaciones de la factura
                                            StrValFact = "SELECT trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCE,D.TPO_DCMNTO,"
                                                    + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual, to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora"
                                                    + " FROM DOCUMENTOS D "
                                                    + " WHERE D.NMRO_DCMNTO = ?";

                                            pst = DBRentas.getConnection().prepareStatement(StrValFact);
                                            pst.setString(1, NMRO_DCMNTO.trim());
                                            rsp = pst.executeQuery(StrValFact);
                                            //ResultRecPago = StrValFact;
                                            if (rsp.next()) {
                                                VlorTtalPagar = rsp.getString(1);  //valor de la deuda
                                                FechaVenci = rsp.getString(2);        //Fecha vencimiento de la factura
                                                TipoProceso = rsp.getString(3);
                                                //Fecha y Hora actual tomada del servidor.
                                                FCHA_Actual = rsp.getString(4);
                                                HRA_Actual = rsp.getString(5);
                                                FCHA_HORA_REGISTRO = rsp.getString(6);

                                                //Valida que el pago se acepte en el horario estipulado
                                                int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));

                                                //Calculo de fechas.
                                                int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                                //si validar_fecha es mayor que cero el documento esta vencido
                                                //si validar_fecha en igual o menor que cero puede pagar
                                                if (validar_fecha > 0) {
                                                    ResultRecPago = "4+El documento se encuentra vencido.";
                                                } else {
                                                    CODIGO_BANCO = COD_BANCO;
                                                    //Valida que el valor a pagar de la factura sea el indicado
                                                    wsri.funciones.Generar_Csctvo_Pago csctvo = new wsri.funciones.Generar_Csctvo_Pago();
                                                    if (VlorTtalPagar.equals(VLOR_PAGO)) {
                                                        CSCTVO_PAGO = csctvo.Csctvo_Pago_RENTAS();
                                                        StrRegPago = "insert into PAGOS_EN_LINEA PEL (PEL.NMRO_DCMNTO, PEL.MNTO_PGO, PEL.FCHA_VNCMNTO, PEL.FCHA_PGO, PEL.FRMA_PGO, PEL.CDGO_BNCO, PEL.CDGO_SCURSAL, PEL.CNSCTVO_PGO, PEL.APLCDO, PEL.FCHA_REGISTRO)"
                                                                + " values (" + NMRO_DCMNTO + ", " + VLOR_PAGO + ", to_date('" + FCHA_VENCI + "','yyyy/mm/dd'), to_date('" + FCHA_PAGO + "','yyyy/mm/dd'),'" + FOR_PAGO + "', '" + CODIGO_BANCO + "', '" + REF_SUC + "', '" + CSCTVO_PAGO + "', 'N', to_date('" + FCHA_HORA_REGISTRO + "','yyyy/mm/dd HH24:MI'))";
                                                        //System.out.println("PAGO RENTAS -->" + StrRegPago);

                                                        pst = DBRentas.getConnection().prepareStatement(StrRegPago);
                                                        pst.executeUpdate(StrRegPago);
                                                        ResultRecPago = "0+Ok, pago registrado.";
                                                    } else {
                                                        ResultRecPago = "5+El valor a pagar no coresponde para este número de recibo.";
                                                    }
                                                }
                                            } else {
                                                ResultRecPago = "6+Numero de recibo no valido.";
                                            }
                                        }
                                    } catch (java.sql.SQLException ex) {
                                        ResultRecPago = ex.getMessage();
                                    } finally {
                                        pst.close();
                                        rsp.close();
                                        StrValFact = "";
                                        COD_EAN = "";
                                    }
                                } else if (COD_EAN.equals("7709998151109")) {
                                    //----------------------------------------------//
                                    //          OPERACION INDUSTRIA                   //
                                    //--------------------------------------------//

                                    PreparedStatement pst = null;
                                    ResultSet rsp = null;

                                    try {

                                        //Validar si existe una declaracion registrado para este numero.
                                        StrValRan = "SELECT R.CDGO FROM RANGO_DOCUMENTOS R WHERE ? BETWEEN R.CNSCTVO_INCIAL AND R.CNSCTVO_FNAL";
                                        pst = DBIndustria.getConnection().prepareStatement(StrValRan);
                                        pst.setString(1, NMRO_DCMNTO);
                                        rsp = pst.executeQuery(StrValRan);
                                        if (rsp.next()) {
                                            TipoRecibo = rsp.getString(1);
                                            //Validar si existe un pago registrado para este numero de documento.
                                            StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = ?";
                                            pst = DBIndustria.getConnection().prepareStatement(StrValNumDoc);
                                            pst.setString(1, NMRO_DCMNTO.trim());
                                            rsp = pst.executeQuery();
                                            if (rsp.next()) {
                                                ResultRecPago = "2+Existe un pago registrado para este recibo.";
                                            } else {
                                                //Teniendo en cuenta el tipo de documento, recupera los datos para realizar validaciones de la factura
                                                if (TipoRecibo.equals("DOL")) {
                                                    //es una declaracion
                                                    StrValFact = "SELECT trim(ODDP.VLOR_DCLRDO) VLOR_DCLRDO , to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') FCHA_VNCMNTO, 'DNO' TPO_DCMNTO,";
                                                    StrValFact = StrValFact + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual,to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora";
                                                    StrValFact = StrValFact + " FROM OL_DECLARACION_PRIVADA ODP, OL_DETALLE_DECLARACION_PRIVADA ODDP, DUAL";
                                                    StrValFact = StrValFact + " WHERE ODP.CNSCTVO = ODDP.CNSCTVO AND ODP.CNSCTVO = ? and to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') >= to_char(sysdate,'yyyymmdd')";
                                                    StrValFact = StrValFact + " AND ODDP.CDGO_ITM = (SELECT MAX(DE.CDGO_ITM) FROM OL_DETALLE_DECLARACION_PRIVADA DE WHERE DE.CNSCTVO = ?)";
                                                } else {
                                                    //Busca los datos pertinentes para realizar validaciones de la factura
                                                    StrValFact = "SELECT trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCE, D.TPO_DCMNTO,";
                                                    StrValFact = StrValFact + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual,to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora";
                                                    StrValFact = StrValFact + " FROM DOCUMENTOS D ";
                                                    StrValFact = StrValFact + " WHERE D.NMRO_DCMNTO = ? ";
                                                }
                                                pst = DBIndustria.getConnection().prepareStatement(StrValFact);
                                                pst.setString(1, NMRO_DCMNTO);
                                                pst.setString(2, NMRO_DCMNTO);
                                                rsp = pst.executeQuery();
                                                //ResultRecPago = StrValFact;
                                                if (rsp.next()) {
                                                    VlorTtalPagar = rsp.getString(1);  //valor de la deuda
                                                    FechaVenci = rsp.getString(2);        //Fecha vencimiento de la factura
                                                    TipoProceso = rsp.getString(3);
                                                    //Fecha y Hora actual tomada del servidor.
                                                    FCHA_Actual = rsp.getString(4);
                                                    HRA_Actual = rsp.getString(5);
                                                    FCHA_HORA_REGISTRO = rsp.getString(6);

                                                    //Valida que el pago se acepte en el horario estipulado
                                                    int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));

                                                    //Calculo de fechas.
                                                    int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                                    //si validar_fecha es mayor que cero el documento esta vencido
                                                    //si validar_fecha en igual o menor que cero puede pagar
                                                    if (validar_fecha > 0) {
                                                        ResultRecPago = "4+El documento se encuentra vencido.";
                                                    } else {
                                                        CODIGO_BANCO = COD_BANCO;
                                                        //Valida que el valor a pagar de la factura sea el indicado
                                                        wsri.funciones.Generar_Csctvo_Pago csctvo = new wsri.funciones.Generar_Csctvo_Pago();

                                                        if (VlorTtalPagar.equals(VLOR_PAGO)) {
                                                            CSCTVO_PAGO = csctvo.Csctvo_Pago_INDUSTRIA();
                                                            StrRegPago = "insert into PAGOS_EN_LINEA PEL (PEL.NMRO_DCMNTO, PEL.MNTO_PGO, PEL.FCHA_VNCMNTO, PEL.FCHA_PGO, PEL.FRMA_PGO, PEL.CDGO_BNCO, PEL.CDGO_SCURSAL, PEL.CNSCTVO_PGO, PEL.APLCDO, PEL.FCHA_REGISTRO) "
                                                            + " values (" + NMRO_DCMNTO + ", " + VLOR_PAGO + ", to_date('" + FCHA_VENCI + "','yyyy/mm/dd'), to_date('" + FCHA_PAGO + "','yyyy/mm/dd'),'" + FOR_PAGO + "', '" + CODIGO_BANCO + "', '" + REF_SUC + "', '" + CSCTVO_PAGO + "', 'N', to_date('" + FCHA_HORA_REGISTRO + "','yyyy/mm/dd HH24:MI'))";
                                                            //System.out.println("PAGO INDUSTRIA --> " + StrRegPago);

                                                            pst = DBIndustria.getConnection().prepareStatement(StrRegPago);
                                                            pst.executeUpdate();
                                                            ResultRecPago = "0+Ok, pago registrado.";
                                                        } else {
                                                            ResultRecPago = "5+El valor a pagar no coresponde para este número de recibo.";
                                                        }
                                                    }
                                                } else {
                                                    ResultRecPago = "6+Numero de recibo no valido.";
                                                }
                                            }
                                        }
                                    } catch (java.sql.SQLException ex) {
                                        ResultRecPago = ex.getMessage();
                                    } finally {
                                        pst.close();
                                        rsp.close();
                                        StrValFact = "";
                                        COD_EAN = "";
                                    }
                                } else {
                                    ResultRecPago = "6+Numero de recibo no valido.";
                                }
                            } catch (Exception ex) {
                            } finally {
                                COD_EAN = "";
                            }
                        }
                    }
                }
            }
        }
        return ResultRecPago;
    }

    /**
     * Web service operation
     */
    //Funcion para determinar si una cadena es numerica
    private static boolean isNumeric(String cadena) {
        try {
            Double.parseDouble(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }

    }//End isNumeric

    //Funcion para validar formato de fechas
    public boolean isDate(String fechax) {
        try {
            java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat("yyyymmdd");
            java.util.Date fecha = formatoFecha.parse(fechax);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /*
     *
     * Web service operation
     *
     */
    @WebMethod(operationName = "PagoElectronico")
    public String ValidaPSE(@WebParam(name = "NMRO_DCMNTO") String NMRO_DCMNTO, @WebParam(name = "IDNTFCCION") String IDNTFCCION, @WebParam(name = "CUS") String CUS, @WebParam(name = "FCHA_INCIO") String FCHA_INCIO, @WebParam(name = "FCHA_FIN") String FCHA_FIN, @WebParam(name = "BANCO") String BANCO, @WebParam(name = "ESTADO") String ESTADO, @WebParam(name = "VALOR") String VALOR, @WebParam(name = "RENTA") String RENTA) {
        String CSCTVO_PAGO = ""; //Consecutivo de guardado
        /*String StrValFact = "";*/
        String StrRegPago = "";
        String StrValNumDoc = "";
        String StrValFact = "";
        String Rfrncia_catastral = "";
        String VlorTtalPagar = "";
        String FechaVenci = "";
        String TipoProceso = "";
        String FCHA_Actual = "";
        String HRA_Actual = "";
        String FCHA_HORA_REGISTRO = "";
        String TipoRecibo = "";
        String StrValRan = "";
        String CODIGO_BANCO = "";
        String StrPagoElectronico = "";
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        try {
            //Se valida que sea el codigo EAN indicado para inicar el proceso
            if (RENTA.equals("PRE")) {
                //----------------------------------------------//
                //          OPERACION PREDIAL                  //
                //--------------------------------------------//

                Statement stmt = null;
                ResultSet rsp = null;
                DBPredial dbp = new DBPredial();

                try {

                    //Validar si existe un pago registrado para este numero de documento.
                    StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = " + NMRO_DCMNTO.trim() + "";
                    stmt = dbp.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    rsp = stmt.executeQuery(StrValNumDoc);

                    if (rsp.next()) {
                        ResultRecPago = "2+Existe un pago registrado para este recibo.";
                    } else {
                        //Busca los datos pertinentes para realizar validaciones de la factura
                        StrValFact = "SELECT D.IDNTFCCION, trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCE, D.TPO_DCMNTO, to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual, to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora FROM DOCUMENTOS D, DUAL";
                        StrValFact = StrValFact + " WHERE D.NMRO_DCMNTO = '" + NMRO_DCMNTO.trim() + "' and to_char(D.FCHA_VNCE,'yyyymmdd')>=to_char(sysdate,'yyyymmdd') and rownum <= 1";

                        stmt = dbp.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        rsp = stmt.executeQuery(StrValFact);

                        if (rsp.next()) {
                            Rfrncia_catastral = rsp.getString(1);
                            VlorTtalPagar = rsp.getString(2);  //valor de la deuda
                            FechaVenci = rsp.getString(3);        //Fecha vencimiento de la factura                                                
                            TipoProceso = rsp.getString(4);         // Codigo proceso, indica si es pago cuotas o total

                            //Fecha y Hora actual tomada del servidor.
                            FCHA_Actual = rsp.getString(5);
                            HRA_Actual = rsp.getString(6);
                            FCHA_HORA_REGISTRO = rsp.getString(7);

                            CODIGO_BANCO = BANCO;
                            //Valida que el valor a pagar de la factura sea el indicado
                            wsri.funciones.Generar_Csctvo_Pago csctvo = new wsri.funciones.Generar_Csctvo_Pago();
                            //System.out.println("CONSECUTIVO DE PAGO " + csctvo.Csctvo_Pago_PREDIAL());
                            StrPagoElectronico = "insert into PAGOS_EN_LINEA_PSE(nmro_dcmnto,idntfccion,cus,fcha_incio,fcha_fnal,cdgo_bnco,estdo,vlor_pgdo)"
                                    + "values (" + NMRO_DCMNTO + ", '" + Rfrncia_catastral + "', '" + CUS + "', to_date('" + FCHA_INCIO + "','dd/mm/yyyy HH24:MI'), "
                                    + "to_date('" + FCHA_FIN + "','dd/mm/yyyy HH24:MI'), '" + BANCO + "', '" + ESTADO + "', " + VALOR + ")";
                            System.out.println("PSE -- PAGO_EN_LINEA_PSE --> " + StrPagoElectronico);

                            stmt = (Statement) dbp.getConnection().createStatement();
                            stmt.executeUpdate(StrPagoElectronico);

                            if ((ESTADO.equals("OK")) || (ESTADO.equals("PENDING"))) {
                                if (VlorTtalPagar.equals(VALOR)) {
                                    //Tipo de pagos normal
                                    CSCTVO_PAGO = csctvo.Csctvo_Pago_PREDIAL();

                                    StrRegPago = "insert into PAGOS_EN_LINEA PEL (PEL.NMRO_DCMNTO, PEL.MNTO_PGO, PEL.FCHA_VNCMNTO, PEL.FCHA_PGO, PEL.FRMA_PGO, PEL.CDGO_BNCO, PEL.CDGO_SCURSAL, PEL.CNSCTVO_PGO, PEL.APLCDO, PEL.FCHA_REGISTRO, PEL.RFRNCIA_CTSTRAL) ";
                                    StrRegPago = StrRegPago + " values (" + NMRO_DCMNTO + ", " + VALOR + ", to_date('" + FechaVenci + "','yyyymmdd'), to_date('" + formato.format(new java.util.Date()) + "','yyyy/mm/dd HH24:MI'), 'PE', '" + CODIGO_BANCO + "', 'VIRT', '" + CSCTVO_PAGO + "', 'A', to_date('" + FCHA_HORA_REGISTRO + "','yyyy/mm/dd HH24:MI'), '" + Rfrncia_catastral + "')";
                                    System.out.println("PSE -- PAGO_EN_LINEA --> " + StrRegPago);
                                    stmt = (Statement) dbp.getConnection().createStatement();
                                    stmt.executeUpdate(StrRegPago);

                                    ResultRecPago = "0+Ok, pago registrado.";
                                } else {
                                    ResultRecPago = "5+El valor a pagar no corresponde para este número de recibo.";
                                }
                            }
                            /*}*/
                        } else {
                            ResultRecPago = "6+Recibo no valido para procesar.";
                        }
                    }
                } catch (java.sql.SQLException ex) {
                    ResultRecPago = ex.getMessage();
                } finally {
                    stmt.close();
                    rsp.close();
                    dbp.close();
                    StrValFact = "";
                    //COD_EAN = "";
                }//FIN OPERACION PREDIAL

            } else if (RENTA.equals("REN")) {
                //----------------------------------------------//
                //          OPERACION RENTAS                   //
                //--------------------------------------------//

                Statement stmt = null;
                ResultSet rsp = null;
                DBRentas dbr = new DBRentas();

                try {

                    //Validar si existe un pago registrado para este numero de documento.
                    StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = " + NMRO_DCMNTO.trim() + "";
                    stmt = dbr.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    rsp = stmt.executeQuery(StrValNumDoc);

                    if (rsp.next()) {
                        ResultRecPago = "2+Existe un pago registrado para este recibo.";
                    } else {
                        //Busca los datos pertinentes para realizar validaciones de la factura
                        StrValFact = "SELECT trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCE, D.TPO_DCMNTO, to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual, to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora FROM DOCUMENTOS D, DUAL";
                        StrValFact = StrValFact + " WHERE D.NMRO_DCMNTO = '" + NMRO_DCMNTO + "'";

                        stmt = dbr.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        rsp = stmt.executeQuery(StrValFact);
                        //ResultRecPago = StrValFact;
                        if (rsp.next()) {
                            VlorTtalPagar = rsp.getString(1);  //valor de la deuda
                            FechaVenci = rsp.getString(2);        //Fecha vencimiento de la factura
                            TipoProceso = rsp.getString(3);
                            //Fecha y Hora actual tomada del servidor.
                            FCHA_Actual = rsp.getString(4);
                            HRA_Actual = rsp.getString(5);
                            FCHA_HORA_REGISTRO = rsp.getString(6);

                            //Calculo de fechas.
                            int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                            //si validar_fecha es mayor que cero el documento esta vencido
                            //si validar_fecha en igual o menor que cero puede pagar
                            if (validar_fecha > 0) {
                                ResultRecPago = "4+El documento se encuentra vencido.";
                            } else {
                                StrPagoElectronico = "insert into PAGOS_EN_LINEA_PSE(nmro_dcmnto,idntfccion,cus,fcha_incio,fcha_fnal,cdgo_bnco,estdo,vlor_pgdo)"
                                        + "values (" + NMRO_DCMNTO + ", '" + Rfrncia_catastral + "', '" + CUS + "', to_date('" + FCHA_INCIO + "','dd/mm/yyyy HH24:MI'), "
                                        + "to_date('" + FCHA_FIN + "','dd/mm/yyyy HH24:MI'), '" + BANCO + "', '" + ESTADO + "', " + VALOR + ")";
                                System.out.println("StrPagoElectronico " + StrPagoElectronico);

                                stmt = (Statement) dbr.getConnection().createStatement();
                                stmt.executeUpdate(StrPagoElectronico);
                                //Valida que el valor a pagar de la factura sea el indicado
                                wsri.funciones.Generar_Csctvo_Pago csctvo = new wsri.funciones.Generar_Csctvo_Pago();
                                if ((ESTADO.equals("OK")) || (ESTADO.equals("PENDING"))) {
                                    if (VlorTtalPagar.equals(VALOR)) {
                                        CSCTVO_PAGO = csctvo.Csctvo_Pago_RENTAS();
                                        StrRegPago = "insert into PAGOS_EN_LINEA PEL (PEL.NMRO_DCMNTO, PEL.MNTO_PGO, PEL.FCHA_VNCMNTO, PEL.FCHA_PGO, PEL.FRMA_PGO, PEL.CDGO_BNCO, PEL.CDGO_SCURSAL, PEL.CNSCTVO_PGO, PEL.APLCDO, PEL.FCHA_REGISTRO) ";
                                        StrRegPago = StrRegPago + " values (" + NMRO_DCMNTO + ", " + VALOR + ", to_date('" + FechaVenci + "','yyyy/mm/dd'), to_date('" + new java.util.Date() + "','yyyy/mm/dd'),'PE', '" + CODIGO_BANCO + "', '001', '" + CSCTVO_PAGO + "', 'N', to_date('" + FCHA_HORA_REGISTRO + "','yyyy/mm/dd HH24:MI'))";

                                        stmt = (Statement) dbr.getConnection().createStatement();
                                        stmt.executeUpdate(StrRegPago);
                                        ResultRecPago = "0+Ok, pago registrado.";
                                    } else {
                                        ResultRecPago = "5+El valor a pagar no coresponde para este número de recibo.";
                                    }
                                }
                            }
                            //}
                        } else {
                            ResultRecPago = "6+Numero de recibo no valido.";
                        }
                    }
                } catch (java.sql.SQLException ex) {
                    ResultRecPago = ex.getMessage();
                } finally {
                    stmt.close();
                    rsp.close();
                    dbr.close();
                    StrValFact = "";
                    //COD_EAN = "";
                }
            } else if (RENTA.equals("ICA")) {
                //----------------------------------------------//
                //          OPERACION INDUSTRIA                   //
                //--------------------------------------------//

                Statement stmt = null;
                ResultSet rsp = null;
                DBIndustria dbi = new DBIndustria();

                try {

                    //Validar si existe una declaracion registrado para este numero.
                    StrValRan = "SELECT R.CDGO FROM RANGO_DOCUMENTOS R WHERE '" + NMRO_DCMNTO + "' BETWEEN R.CNSCTVO_INCIAL AND R.CNSCTVO_FNAL";
                    stmt = dbi.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    rsp = stmt.executeQuery(StrValRan);
                    if (rsp.next()) {
                        TipoRecibo = rsp.getString("CDGO");
                        //Validar si existe un pago registrado para este numero de documento.
                        StrValNumDoc = "select PL.Nmro_Dcmnto from PAGOS_EN_LINEA PL where PL.NMRO_DCMNTO = " + NMRO_DCMNTO.trim() + "";
                        stmt = dbi.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        rsp = stmt.executeQuery(StrValNumDoc);
                        if (rsp.next()) {
                            ResultValFact = "2+Existe un pago registrado para este recibo.";
                        } else {
                            //Teniendo en cuenta el tipo de documento, recupera los datos para realizar validaciones de la factura
                            if (TipoRecibo.equals("DOL")) {
                                //es una declaracion
                                StrValFact = "SELECT trim(ODDP.VLOR_DCLRDO) VLOR_DCLRDO , to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') FCHA_VNCMNTO, 'DNO' TPO_DCMNTO,";
                                StrValFact = StrValFact + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual,to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora";
                                StrValFact = StrValFact + " FROM OL_DECLARACION_PRIVADA ODP, OL_DETALLE_DECLARACION_PRIVADA ODDP, DUAL";
                                StrValFact = StrValFact + " WHERE ODP.CNSCTVO = ODDP.CNSCTVO AND ODP.CNSCTVO = '" + NMRO_DCMNTO + "' and to_char(ODP.FCHA_VNCMNTO,'yyyymmdd') >= to_char(sysdate,'yyyymmdd')";
                                StrValFact = StrValFact + " AND ODDP.CDGO_ITM = (SELECT MAX(DE.CDGO_ITM) FROM OL_DETALLE_DECLARACION_PRIVADA DE WHERE DE.CNSCTVO ='" + NMRO_DCMNTO + "')";
                            } else {
                                //Busca los datos pertinentes para realizar validaciones de la factura
                                StrValFact = "SELECT trim(D.VLOR_DCMNTO) VLOR_DCMNTO , to_char(D.FCHA_VNCE ,'yyyymmdd') FCHA_VNCE, D.TPO_DCMNTO,";
                                StrValFact = StrValFact + " to_char(sysdate,'yyyymmdd') fechaActual, to_char(sysdate,'HH24:MI:SS') horaActual,to_char(sysdate,'yyyymmdd HH24:MI') fecha_hora";
                                StrValFact = StrValFact + " FROM DOCUMENTOS D, DUAL";
                                StrValFact = StrValFact + " WHERE D.NMRO_DCMNTO = '" + NMRO_DCMNTO + "'";
                            }

                            stmt = dbi.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                            rsp = stmt.executeQuery(StrValFact);
                            //ResultRecPago = StrValFact;
                            if (rsp.next()) {
                                VlorTtalPagar = rsp.getString(1);  //valor de la deuda
                                FechaVenci = rsp.getString(2);        //Fecha vencimiento de la factura
                                TipoProceso = rsp.getString(3);
                                //Fecha y Hora actual tomada del servidor.
                                FCHA_Actual = rsp.getString(4);
                                HRA_Actual = rsp.getString(5);
                                FCHA_HORA_REGISTRO = rsp.getString(6);

                                //Valida que el pago se acepte en el horario estipulado
                                int HoraRecibo = Integer.parseInt(HRA_Actual.substring(0, 2));

                                int validar_fecha = (Integer.parseInt(FCHA_Actual) - Integer.parseInt(FechaVenci));
                                //si validar_fecha es mayor que cero el documento esta vencido
                                //si validar_fecha en igual o menor que cero puede pagar
                                if (validar_fecha > 0) {
                                    ResultRecPago = "4+El documento se encuentra vencido.";
                                } else {
                                    CODIGO_BANCO = BANCO;
                                    //Valida que el valor a pagar de la factura sea el indicado

                                    StrPagoElectronico = "insert into PAGOS_EN_LINEA_PSE(nmro_dcmnto,idntfccion,cus,fcha_incio,fcha_fnal,cdgo_bnco,estdo,vlor_pgdo)"
                                            + "values (" + NMRO_DCMNTO + ", '" + Rfrncia_catastral + "', '" + CUS + "', to_date('" + FCHA_INCIO + "','dd/mm/yyyy HH24:MI'), "
                                            + "to_date('" + FCHA_FIN + "','dd/mm/yyyy HH24:MI'), '" + BANCO + "', '" + ESTADO + "', " + VALOR + ")";
                                    System.out.println("StrPagoElectronico " + StrPagoElectronico);

                                    stmt = (Statement) dbi.getConnection().createStatement();
                                    stmt.executeUpdate(StrPagoElectronico);
                                    wsri.funciones.Generar_Csctvo_Pago csctvo = new wsri.funciones.Generar_Csctvo_Pago();
                                    if ((ESTADO.equals("OK")) || (ESTADO.equals("PENDING"))) {
                                        if (VlorTtalPagar.equals(VALOR)) {
                                            CSCTVO_PAGO = csctvo.Csctvo_Pago_RENTAS();
                                            StrRegPago = "insert into PAGOS_EN_LINEA PEL (PEL.NMRO_DCMNTO, PEL.MNTO_PGO, PEL.FCHA_VNCMNTO, PEL.FCHA_PGO, PEL.FRMA_PGO, PEL.CDGO_BNCO, PEL.CDGO_SCURSAL, PEL.CNSCTVO_PGO, PEL.APLCDO, PEL.FCHA_REGISTRO) ";
                                            StrRegPago = StrRegPago + " values (" + NMRO_DCMNTO + ", " + VALOR + ", to_date('" + FechaVenci + "','yyyy/mm/dd'), to_date('" + new java.util.Date() + "','yyyy/mm/dd'),'PE', '" + CODIGO_BANCO + "', '001', '" + CSCTVO_PAGO + "', 'N', to_date('" + FCHA_HORA_REGISTRO + "','yyyy/mm/dd HH24:MI'))";

                                            stmt = (Statement) dbi.getConnection().createStatement();
                                            stmt.executeUpdate(StrRegPago);
                                            ResultRecPago = "0+Ok, pago registrado.";
                                        } else {
                                            ResultRecPago = "5+El valor a pagar no coresponde para este número de recibo.";
                                        }
                                    }
                                }
                                //}
                            } else {
                                ResultRecPago = "6+Numero de recibo no valido.";
                            }
                        }
                    }
                } catch (java.sql.SQLException ex) {
                    ResultRecPago = ex.getMessage();
                } finally {
                    stmt.close();
                    rsp.close();
                    dbi.close();
                    StrValFact = "";
                    //COD_EAN = "";
                }
            }
        } catch (Exception ex) {
        } finally {
            //COD_EAN = "";
        }

        return ResultRecPago;
    }
}
