package org.wispcrm.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.Orden;
import org.wispcrm.services.ClienteServiceImpl;
import org.wispcrm.services.FacturaReportService;
import org.wispcrm.services.OrdenService;
import org.wispcrm.services.WhatsappMessageService;
import org.wispcrm.utils.ConstantMensaje;

import lombok.AllArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

@Controller
@AllArgsConstructor
@Slf4j
public class OrdenController {

    private static final String REDIRECT_LISTAR_FACTURA = "redirect:/listarfactura";
    private final OrdenService ordenService;
    private final ClienteServiceImpl clienteService;
    private final FacturaReportService reporte;
        private final WhatsappMessageService whatsappMessageService;


    @RequestMapping(value = "/listarordenes")
    public String listarOrdenes(Model modelo) {
        List<Orden> ordenes = this.ordenService.findAll();
        modelo.addAttribute("listaordenes", ordenes);
        return "ordenes/listaOrdenes";
    }

    @RequestMapping(value = "/orden")
    public String mostrarFormularioDeNuevaOrden(@RequestParam(name = "clienteID") Integer clienteID, Model modelo,
            RedirectAttributes flash) {
        Cliente cliente = clienteService.findOne(clienteID);
        Orden orden = new Orden();
        orden.setCliente(cliente);
        modelo.addAttribute("listatipo", this.ordenService.findAllTipoOrden());
        modelo.addAttribute("listatecnico", this.ordenService.findAllOperario());
        modelo.addAttribute("orden", orden);
        modelo.addAttribute("titulo", "Nueva Orden");
        return "ordenes/formOrdenes";
    }

    @PostMapping(value = "/saveOrden")
    public String crearNuevaOrdenYEnviarSms(@ModelAttribute @Validated Orden orden, Model modelo,
            RedirectAttributes flash, HttpServletResponse response, BindingResult result) {
        Orden ordenSaved = this.ordenService.createOrden(orden);
        sendWhatsAppMessageOrdenGenerada(ordenSaved);
        flash.addFlashAttribute("success", " Se Agregado la Orden correctamente").addFlashAttribute("clase", "success");
        return REDIRECT_LISTAR_FACTURA;
    }

    @GetMapping("/descargarorden/{id}")
    public void descargarOrdenPorId(@PathVariable(value = "id") Integer id, HttpServletResponse response)
            throws IOException, JRException, SQLException {
        Orden orden = this.ordenService.findOrdenById(id);
        this.descargarPdfOrdenDeServicio(response, orden);
    }

    /**
     * 
     * @param response
     * @param ordenServicio
     * @throws JRException
     * @throws SQLException
     * @throws IOException
     */
    private void descargarPdfOrdenDeServicio(HttpServletResponse response, Orden ordenServicio)
            throws JRException, SQLException, IOException {
        if (ordenServicio != null) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + ordenServicio.getId() + ".pdf");
            JasperPrint jasperPrint = reporte.ordenDeServicioPdfFile(ordenServicio.getId());
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
        }
    }

    private void sendWhatsAppMessageOrdenGenerada(Orden orden) {
        if (orden == null || orden.getCliente() == null) {
            log.error("No se puede enviar mensaje: Orden o cliente es null");
            return;
        }

        try {
            // Use StringBuilder for better performance with string concatenation
            StringBuilder messageBuilder = new StringBuilder().append(
                            "Estimado Tecnico, Se ha generado una orden de servicio para el cliente : ")
                    .append(orden.getCliente().getNombres()).append(" ")
                    .append(ConstantMensaje.RUTA_DESCARGA_ORDEN_DE_SERVICIO)
                    .append(orden.getId());

            whatsappMessageService.sendSimpleMessageToGroupWasApiSender(
                    "120363146011086828@g.us", messageBuilder.toString());
        } catch (IOException | InterruptedException e) {
            log.error("Error al enviar mensaje de WhatsApp: ", e);

        }
    }


}
