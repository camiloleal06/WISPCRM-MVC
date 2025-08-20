package org.wispcrm.controller;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wispcrm.daos.ClienteDao;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.ClienteDTO;
import org.wispcrm.modelo.EstadoCliente;
import org.wispcrm.services.ClienteServiceImpl;
import org.wispcrm.services.EnviarSMS;
import org.wispcrm.services.PlanServiceImpl;

@Controller
@RequiredArgsConstructor
public class ClienteController {
    private static final String CLASE = "clase";
    private static final String SUCCESS = "success";
    private static final String REDIRECT_LISTAR = "redirect:/listar";
    private static final String TITULO = "titulo";
    private static final String CLIENTE = "cliente";
    private static final String VER_LISTA_CLIENTE = "cliente/listaCliente";
    private static final String VER_FORM_CLIENTE = "cliente/formCliente";

    private final PlanServiceImpl planService;
    private final EnviarSMS smsService;
    private final ClienteServiceImpl clienteService;
    private final ClienteDao clienteRepository;
    private final InterfaceFacturas daoFacturas;

    @GetMapping(value = "/vercliente")
    public String ver(@RequestParam(name = "id") Integer id,
            Map<String, Object> model) {
        Cliente cliente = clienteService.findOne(id);
        model.put(CLIENTE, cliente);
        model.put("facturas", daoFacturas.findByCliente(cliente));
        return "cliente/ver";
    }

    @GetMapping("/listar")
    public String listarClientes(Model modelo) {
        List<ClienteDTO> cliente = clienteRepository.lista();
        modelo.addAttribute(CLIENTE, cliente);
        return VER_LISTA_CLIENTE;
    }

    @GetMapping("/form")
    public String crear(Model modelo) {
        modelo.addAttribute(CLIENTE, new Cliente());
        modelo.addAttribute("listaplan", planService.findAll());
        modelo.addAttribute(TITULO, "Nuevo Cliente");
        return VER_FORM_CLIENTE;
    }

    @PostMapping(value = "/save")
    public String save(@ModelAttribute @Validated Cliente cliente, Model modelo,
            RedirectAttributes flash, BindingResult result,
            SessionStatus status) {
        modelo.addAttribute(TITULO, "Nuevo Cliente");
        clienteService.save(cliente);
        status.setComplete();
        flash.addFlashAttribute(SUCCESS,
                        cliente.getNombres() + "Agregado correctamente")
                .addFlashAttribute(CLASE, SUCCESS);
        return REDIRECT_LISTAR;
    }

    /**
     * @param id
     * @param modelo
     * @return
     */
    @RequestMapping(value = "/editar")
    public String editar(@RequestParam(name = "id") Integer id, Model modelo) {
        modelo.addAttribute(CLIENTE, clienteService.findById(id));
        modelo.addAttribute("listaplan", planService.listPlanes());
        modelo.addAttribute(TITULO, "Actualizar Cliente");
        return VER_FORM_CLIENTE;
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id) {
        Cliente cliente = clienteService.findById(id);
        cliente.setEstado(EstadoCliente.INACTIVO);
        clienteRepository.save(cliente);
        return REDIRECT_LISTAR;
    }

    @GetMapping("/reactivar/{id}")
    public String reactivar(@PathVariable int id) {
        Cliente cliente = clienteService.findById(id);
        cliente.setEstado(EstadoCliente.ACTIVO);
        clienteRepository.save(cliente);
        return REDIRECT_LISTAR;
    }

}
