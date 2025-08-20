package org.wispcrm.services;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service
@Slf4j
public class FacturaReportService {

    private static final String FACTURA_ID = "factura_id";
    private static final String ORDER_ID = "orden_id";
    public static final String ERROR_GENERANDO_REPORTE = "Error generando reporte";

    @Value("${invoice.template.path}")
    private String invoiceTemplate;

    @Value("${recibo.template.path}")
    private String reciboTemplate;

    @Value("${orden.template.path}")
    private String ordenTemplate;

    @Value("${factura.path}")
    private String facturaPath;

   // @Qualifier("jdbcTemplate")
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public JasperPrint descargarPdfFile(Integer id) throws JRException, SQLException {
        InputStream stream = this.getClass().getResourceAsStream(invoiceTemplate);
        JasperReport report = JasperCompileManager.compileReport(stream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FACTURA_ID, id);
        DataSource dataSource = jdbcTemplate.getDataSource();
        validateDatasource(dataSource);
        return JasperFillManager.fillReport(report, parameters, dataSource.getConnection());
    }

    @Transactional
    public JasperPrint ordenDeServicioPdfFile(Integer id) throws JRException, SQLException {
        InputStream stream = this.getClass().getResourceAsStream(ordenTemplate);
        JasperReport report = JasperCompileManager.compileReport(stream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ORDER_ID, id);
        DataSource dataSource = jdbcTemplate.getDataSource();
        validateDatasource(dataSource);
        return JasperFillManager.fillReport(report, parameters, dataSource.getConnection());
    }

    @Transactional
    public JasperPrint descargarPagoFile(Integer id) throws JRException {
        InputStream stream = this.getClass().getResourceAsStream(reciboTemplate);
        JasperReport report = JasperCompileManager.compileReport(stream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FACTURA_ID, id);
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                return JasperFillManager.fillReport(report, parameters, connection);
            } catch (SQLException e) {

                log.error(ERROR_GENERANDO_REPORTE, e);
            }
        }
        return null;

    }

    public void createPdfReport(Integer id, String cliente) throws JRException {
        report(id, cliente);
    }


    public void pagoPdfReport(Integer id, String cliente) throws JRException {
        pagoReport(id, cliente);
    }

   public void ordenPdfReport(Integer id, String cliente, String parametro) throws JRException {
        report(id, cliente, parametro);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void report(Integer id, String cliente) throws JRException {
        final InputStream stream = this.getClass().getResourceAsStream(invoiceTemplate);
        JasperReport report = JasperCompileManager.compileReport(stream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FACTURA_ID, id);
        DataSource dataSource = jdbcTemplate.getDataSource();
        validateDatasource(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            JasperExportManager.exportReportToPdfFile(print, facturaPath + cliente);
        } catch (SQLException e) {
            log.error(ERROR_GENERANDO_REPORTE, e);
        }
    }

    private static void validateDatasource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource no está configurado correctamente.");
        }
    }

    @Transactional
    public void pagoReport(Integer id, String cliente) throws JRException {
        final InputStream stream = this.getClass().getResourceAsStream(reciboTemplate);
        JasperReport report = JasperCompileManager.compileReport(stream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FACTURA_ID, id);
        DataSource dataSource = jdbcTemplate.getDataSource();
        validateDatasource(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            JasperExportManager.exportReportToPdfFile(print, facturaPath + cliente);
        } catch (SQLException e) {
            log.error(ERROR_GENERANDO_REPORTE, e);
        }
    }

    @Transactional
    public void report(Integer id, String cliente, String parametro) throws JRException {
        final InputStream stream = this.getClass().getResourceAsStream(ordenTemplate);
        JasperReport report = JasperCompileManager.compileReport(stream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(parametro, id);
        DataSource dataSource = jdbcTemplate.getDataSource();
        try (Connection connection = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            JasperExportManager.exportReportToPdfFile(print, facturaPath + cliente);
                }
        catch (SQLException e) {
            log.error(ERROR_GENERANDO_REPORTE, e);
        }
    }
}
