package org.wispcrm.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.daos.InterfacePagos;
import org.wispcrm.modelo.ClienteDTO;
import org.wispcrm.modelo.FacturaDto;
import org.wispcrm.modelo.Note;
import org.wispcrm.modelo.PagoDTO;
import org.wispcrm.services.ClienteServiceImpl;
import org.wispcrm.services.FirebaseMessagingService;

import com.google.firebase.messaging.FirebaseMessagingException;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/clientes")
@Slf4j
public class ClienteRestController {

    @Autowired
    ClienteServiceImpl clienteservice;

    @Autowired
    InterfacePagos interfacePago;

    @Autowired
    InterfaceFacturas facturasDao;

    @Autowired
    FirebaseMessagingService firebaseService;


    @GetMapping("/test-log")
    public String testLog() {
        log.info("Prueba de log INFO");
        log.error("Prueba de log ERROR");
        return "Log generado!";
    }

    @GetMapping(value = "/paginador")
    public ResponseEntity<Map<String, Object>> obtenerTodos(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable paging = PageRequest.of(page, size);
        Page<ClienteDTO> pageTuts;
        pageTuts = clienteservice.listaPageable(paging);
        List<ClienteDTO> clientesDTO = pageTuts.getContent();
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("clientes", clientesDTO);
        resultado.put("currentPage", pageTuts.getNumber());
        resultado.put("totalItems", pageTuts.getTotalElements());
        resultado.put("totalPages", pageTuts.getTotalPages());
        return new ResponseEntity<>(resultado, HttpStatus.OK);

    }

    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> listaClientes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(clienteservice.listaPageable(PageRequest.of(page, size)), HttpStatus.OK);

    }

    @GetMapping("/facturas")
    public ResponseEntity<List<FacturaDto>> listaFacturas() {
        return new ResponseEntity<>(facturasDao.listadoFacturasPendientes(), HttpStatus.OK);

    }

    /*
     * @GetMapping("/{id}") public ResponseEntity<EditarClienteDTO>
     * clienteID(@PathVariable Integer id) { return new
     * ResponseEntity<>(clienteservice.editarCliente(id), HttpStatus.OK);
     * 
     * }
     * 
     */
    @GetMapping(value = "/pagos")
    public ResponseEntity<Map<String, Object>> getAllPagos(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Pageable paging = PageRequest.of(page, size);
        Page<PagoDTO> pageTuts;
        pageTuts = interfacePago.lista(paging);
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("pagos", pageTuts.getContent());
        resultado.put("currentPage", pageTuts.getNumber());
        resultado.put("totalItems", pageTuts.getTotalElements());
        resultado.put("totalPages", pageTuts.getTotalPages());
        return new ResponseEntity<>(resultado, HttpStatus.OK);
    }

    @PostMapping("/send-notification")
    public String sendNotification(@RequestBody Note note, @RequestParam String token)
            throws FirebaseMessagingException {
        return firebaseService.sendNotification(note, token);
    }

    @GetMapping(value = "/bill")
    public Bill getBill() {
        return Bill.builder().value("500000").transactionId("uu-8456-569-25").
        build();
    }


}
