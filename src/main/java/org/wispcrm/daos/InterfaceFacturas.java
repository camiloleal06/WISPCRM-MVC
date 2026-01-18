package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.facturas.Factura;
import org.wispcrm.modelo.facturas.FacturaDto;
import org.wispcrm.modelo.facturas.ResumenFacturasDTO;

@Repository
public interface InterfaceFacturas extends CrudRepository<Factura, Integer> {

    Factura findFirstFacturaByCliente(Cliente cliente);

    List<Factura> findByCliente(Cliente cliente);

    @Query("SELECT new org.wispcrm.modelo.facturas.FacturaDto(f.id, CONCAT(c.nombres,' ',c.apellidos) as nombres, "
            + "c.telefono,  f.valor, c.diapago, DATEDIFF(CURRENT_TIMESTAMP() , f.fechapago) as mora) "
            + " FROM Factura f JOIN f.cliente c WHERE f.estado=true ")
    List<FacturaDto> listadoFacturasPendientes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=true and periodo=MONTH(CURRENT_TIMESTAMP)")
    Long totalFacturasPendientesMes();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=true and periodo=MONTH(CURRENT_TIMESTAMP)")
    Long totalCantidadFacturasMes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=false and periodo=MONTH(CURRENT_TIMESTAMP)")
    Long totalFacturasPagadasMes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=true")
    Long totalFacturasPendientesHistorico();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=true")
    Long totalCantidadFacturasHistorico();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=false")
    Long totalFacturasPagadasHistorico();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=false and periodo=MONTH(CURRENT_TIMESTAMP)")
    int existFacturaCreada();

    @Query("SELECT new org.wispcrm.modelo.facturas.ResumenFacturasDTO(" +
            "COUNT(*), " +
            "SUM(valor)) " +
            "FROM Factura " +
            "WHERE estado = true " +
            "AND MONTH(createAt) = MONTH(CURRENT_DATE)")
    ResumenFacturasDTO getResumenFacturasMes();

}
