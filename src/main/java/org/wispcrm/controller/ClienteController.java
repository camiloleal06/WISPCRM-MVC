package org.wispcrm.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import org.wispcrm.services.PlanServiceImpl;
import org.wispcrm.services.WhatsappMessageService;

import static org.wispcrm.utils.Util.logClienteOperation;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ClienteController {
    private static final String CLASE = "clase";
    private static final String SUCCESS = "success";
    private static final String REDIRECT_LISTAR = "redirect:/listar";
    private static final String TITULO = "titulo";
    private static final String CLIENTE = "cliente";
    private static final String VER_LISTA_CLIENTE = "cliente/listaCliente";
    private static final String VER_FORM_CLIENTE = "cliente/formCliente";
    public static final String CREAR_CLIENTE = "crearCliente";
    public static final String ERROR = "error";
    private final PlanServiceImpl planService;
    private final ClienteServiceImpl clienteService;
    private final ClienteDao clienteRepository;
    private final InterfaceFacturas daoFacturas;
    private  final WhatsappMessageService whatsappMessageService;
    private final CacheManager cacheManager;

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
        try {
            List<ClienteDTO> clientes = clienteRepository.lista();

             if (clientes == null) {
                log.warn("La consulta de clientes retornó null");
                modelo.addAttribute("mensaje", "Error al obtener los clientes");
                return ERROR;
            }

            if (clientes.isEmpty()) {
                log.warn("No se encontraron clientes en la base de datos");
                modelo.addAttribute("mensaje", "No hay clientes registrados");
            }

            modelo.addAttribute(CLIENTE, clientes);
            return VER_LISTA_CLIENTE;

        } catch (Exception e) {
            log.error("Error al listar clientes: ", e);
            modelo.addAttribute(ERROR, "Ocurrió un error al cargar la lista de clientes");
            return ERROR;
        }
    }



    @GetMapping("/form")
    public String crear(Model modelo) {
        modelo.addAttribute(CLIENTE, new Cliente());
        modelo.addAttribute("listaplan", planService.findAll());
        modelo.addAttribute(TITULO, "Nuevo Cliente");
        return VER_FORM_CLIENTE;
    }

    @PostMapping("/save")
    public String save(@ModelAttribute @Validated Cliente cliente,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            SessionStatus status) {

        try {
            clienteService.save(cliente);
            logClienteOperation(CREAR_CLIENTE, SUCCESS, "Se ha creado un nuevo cliente {}", cliente);
            status.setComplete();
            redirectAttributes.addFlashAttribute(SUCCESS, cliente.getNombres() + " Agregado correctamente")
                    .addFlashAttribute(CLASE, SUCCESS);

        }
        catch (DataIntegrityViolationException e) {
             logClienteOperation(CREAR_CLIENTE, "ERROR",
                    "Error de integridad de datos al crear cliente: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR, "El cliente ya existe o hay datos duplicados")
                    .addFlashAttribute(CLASE, "danger");
        }
        catch (Exception e) {
           logClienteOperation(CREAR_CLIENTE, "ERROR", "Error al crear cliente: {}", e.getMessage());
           redirectAttributes.addFlashAttribute(ERROR, "Error al guardar el cliente")
                    .addFlashAttribute(CLASE, "danger");

        }
        return REDIRECT_LISTAR;
    }

    @RequestMapping(value = "/editar")
    public String editar(@RequestParam(name = "id") Integer id, Model modelo) {
        Cliente cliente = clienteService.findById(id);
        modelo.addAttribute(CLIENTE, cliente);
        if (cliente == null) {
            logClienteOperation("editarCliente", "Fail", "No se encontró el cliente con id {}", null);
            return REDIRECT_LISTAR;
        }
        modelo.addAttribute("listaplan", planService.findAll());
        modelo.addAttribute(TITULO, "Actualizar Cliente");
        return VER_FORM_CLIENTE;
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id) {
        Cliente cliente = clienteService.findById(id);
        cliente.setEstado(EstadoCliente.INACTIVO);
        clienteRepository.save(cliente);
        logClienteOperation("eliminarCliente", SUCCESS,"Se ha eliminado el cliente {}",cliente);
        cacheManager.getCache("lista-clientes").clear();

        return REDIRECT_LISTAR;
    }

    @GetMapping("/reactivar/{id}")
    @Transactional
    public String reactivar(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.findById(id);

            if (cliente == null) {
                log.error("No se encontró el cliente con ID: {}", id);
                redirectAttributes.addFlashAttribute(ERROR, "Cliente no encontrado");
                return REDIRECT_LISTAR;
            }

            cliente.setEstado(EstadoCliente.ACTIVO);
            clienteRepository.save(cliente);
            logClienteOperation("reactivarCliente", SUCCESS,
                    "Se ha reactivado el cliente {} ",cliente);
            redirectAttributes.addFlashAttribute(SUCCESS,
                    String.format("Cliente %s reactivado exitosamente", cliente.getNombres()));
            return REDIRECT_LISTAR;

        } catch (Exception e) {
            log.error("Error al reactivar el cliente {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR,
                    "Ocurrió un error al reactivar el cliente");
            return REDIRECT_LISTAR;
        }
        finally {
            cacheManager.getCache("lista-clientes").clear();
        }
    }
}
