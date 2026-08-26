package org.wispcrm.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wispcrm.interfaces.ClienteInterface;
import org.wispcrm.interfaces.OrdenInterface;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.ordenes.Orden;
import org.wispcrm.services.WhatsappMessageService;
import org.wispcrm.utils.ConstantMensaje;

import java.util.Date;

@Controller
@AllArgsConstructor
public class OrdenController {

    private final OrdenInterface ordenService;
    private final ClienteInterface clienteService;
    private final WhatsappMessageService whatsappService;

    @GetMapping("/ordenes")
    public String listarOrdenes(Model model) {
        model.addAttribute("ordenes", ordenService.findAll());
        return "ordenes/listaOrdenes";
    }

    @GetMapping("/orden")
    public String formOrden(@RequestParam(required = false) Integer clienteID, Model model) {
        Orden orden = new Orden();
        if (clienteID != null) {
            Cliente cliente = clienteService.findById(clienteID);
            orden.setCliente(cliente);
        }
        model.addAttribute("orden", orden);
        model.addAttribute("titulo", "Nueva Orden de Trabajo");
        model.addAttribute("listatipo", ordenService.findAllTipoOrden());
        model.addAttribute("listatecnico", ordenService.findAllOperario());
        model.addAttribute("clientes", clienteService.findAll());
        return "ordenes/formOrdenes";
    }

    @PostMapping("/saveOrden")
    public String saveOrden(@ModelAttribute Orden orden, RedirectAttributes flash) {
        if (orden.getFechaInicio() == null) orden.setFechaInicio(new Date());
        Orden saved = ordenService.createOrden(orden);

        Cliente cliente = saved.getCliente();
        String msg = String.format(
            "🔧 *Nueva Orden de Trabajo #%d*\n" +
            "👤 *Cliente:* %s\n" +
            "📋 *Tipo:* %s\n" +
            "📝 *Descripción:* %s\n" +
            "👷 *Técnico:* %s\n" +
            "📅 *Fecha:* %s",
            saved.getId(),
            cliente != null ? cliente.getNombres() + " " + cliente.getApellidos() : "N/A",
            saved.getTipoOrden() != null ? saved.getTipoOrden().getDescripcion() : "N/A",
            saved.getDescripcion() != null ? saved.getDescripcion() : saved.getObservacion(),
            saved.getOperario() != null ? saved.getOperario().getNombres() : "N/A",
            saved.getFechaInicio()
        );
        whatsappService.sendSimpleMessageToGroupWasApiSender(ConstantMensaje.WHATSAPP_GROUP_ID, msg);

        flash.addFlashAttribute("success", "Orden #" + saved.getId() + " creada exitosamente.");
        return "redirect:/ordenes";
    }

    @GetMapping("/orden/cerrar/{id}")
    public String formCerrarOrden(@PathVariable Integer id, Model model) {
        model.addAttribute("orden", ordenService.findOrdenById(id));
        return "ordenes/cerrarOrden";
    }

    @PostMapping("/orden/cerrar/{id}")
    public String cerrarOrden(@PathVariable Integer id,
                               @RequestParam String comentarioCierre,
                               RedirectAttributes flash) {
        Orden orden = ordenService.cerrarOrden(id, comentarioCierre);

        Cliente cliente = orden.getCliente();
        String msg = String.format(
            "✅ *Orden #%d CERRADA*\n" +
            "👤 *Cliente:* %s\n" +
            "📋 *Tipo:* %s\n" +
            "🔧 *Trabajo realizado:* %s\n" +
            "👷 *Técnico:* %s\n" +
            "📅 *Fecha cierre:* %s",
            orden.getId(),
            cliente != null ? cliente.getNombres() + " " + cliente.getApellidos() : "N/A",
            orden.getTipoOrden() != null ? orden.getTipoOrden().getDescripcion() : "N/A",
            comentarioCierre,
            orden.getOperario() != null ? orden.getOperario().getNombres() : "N/A",
            orden.getFechaFin()
        );
        whatsappService.sendSimpleMessageToGroupWasApiSender(ConstantMensaje.WHATSAPP_GROUP_ID, msg);

        flash.addFlashAttribute("success", "Orden #" + id + " cerrada exitosamente.");
        return "redirect:/ordenes";
    }
}
