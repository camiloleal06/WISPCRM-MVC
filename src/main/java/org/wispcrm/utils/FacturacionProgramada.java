package org.wispcrm.utils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.interfaces.ClienteInterface;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.Factura;
import org.wispcrm.services.ClienteServiceImpl;
import org.wispcrm.services.FacturaReportService;
import org.wispcrm.services.MailService;

import net.sf.jasperreports.engine.JRException;

@Component

public class FacturacionProgramada {

    @Autowired
    ClienteInterface clienteDao;
    @Autowired
    ClienteServiceImpl datacliente;
    @Autowired
    FacturaReportService reporte;
    @Autowired
    MailService mailService;
    @Autowired
    InterfaceFacturas facturaDao;

    public void scheduleTaskWithFixedRate() {

   /*     Calendar fechaactual = Calendar.getInstance();
        int diaactual = fechaactual.get(Calendar.DAY_OF_MONTH);
        Calendar fechavencimiento = Calendar.getInstance();

        fechavencimiento.setTime(new Date());
        fechavencimiento.add(Calendar.DAY_OF_YEAR, 10);

        List<Cliente> clientes = clienteDao.findAll();
        Cliente cl = new Cliente();

        for (Cliente cliente : clientes) {
            Factura factura = new Factura();
            cl.setId(cliente.getId());
            cl.setEmail(cliente.getEmail());
            cl.setIdentificacion(cliente.getIdentificacion());
            cl.setPlanes(cliente.getPlanes());
            factura.setFechapago(new Date());
            factura.setFechavencimiento(fechavencimiento.getTime());
            factura.setCliente(cl);
            if (diaactual == cliente.getDiapago()
                    && facturaDao.findFirstFacturaByCliente(factura.getCliente()) == null) {
                datacliente.saveFactura(factura);
                String client = factura.getCliente().getIdentificacion();
                try {
                    reporte.createPdfReport(factura.getId(), client + ".pdf");
                    mailService.sendEmailAttachment("Llegó tu factura!!",
                            "Estimado(a) Cliente  se ha generado una factura a su nombre Gracias por su Preferencia",
                            "administracion@nacionaldemuebles.com.co", factura.getCliente().getEmail(), true,
                            new File(client + ".pdf"));
                } catch (JRException e) {
                    e.printStackTrace();
                }
            }

        }
    }*/
    }
}
