package org.wispcrm.interfaces;

import java.util.List;

import org.wispcrm.modelo.facturas.Factura;
import org.wispcrm.modelo.facturas.FacturaDto;

public interface FacturaInterface {

    List<Factura> findAll();

    Factura findFacturabyid(Integer id);

    Long pendientes();

    Long pagadas();

    Long facturas();

    List<FacturaDto> listadoFacturas();

    void delete(int id);

    Factura save(Factura factura);
}
