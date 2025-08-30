package org.wispcrm.restcontroller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.daos.InterfacePagos;
import org.wispcrm.modelo.PagoDTO;
import org.wispcrm.modelo.ResumenFacturasDTO;
import org.wispcrm.services.ClienteServiceImpl;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/clientes")
@Slf4j
@RequiredArgsConstructor
public class ClienteRestController {

    private final ClienteServiceImpl clienteservice;
    private final InterfacePagos interfacePago;
    private final InterfaceFacturas facturasDao;

    @Cacheable(value = "clientes")
    @GetMapping(value = "/last-pagos-cliente")
    public ResponseEntity<List<PagoDTO>> getLastTenPagosCliente() {
        Pageable pageable = PageRequest.of(0, 10);
        List<PagoDTO> pagos = interfacePago.findLastTenPagos(pageable);
        return new ResponseEntity<>(pagos, HttpStatus.OK);
    }

    @Cacheable(value = "resumen-facturas")
    @GetMapping("/resumen-facturas-mes")
    public ResponseEntity<ResumenFacturasDTO> getResumenFacturasMes() {
        ResumenFacturasDTO resumen = facturasDao.getResumenFacturasMes();
        return new ResponseEntity<>(resumen, HttpStatus.OK);
    }
}
