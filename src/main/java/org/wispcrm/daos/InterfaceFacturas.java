package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.facturas.DeudorDTO;
import org.wispcrm.modelo.facturas.Factura;
import org.wispcrm.modelo.facturas.FacturaDto;
import org.wispcrm.modelo.facturas.ResumenFacturasDTO;

@Repository
public interface InterfaceFacturas extends CrudRepository<Factura, Integer> {

    List<Factura> findByCliente(Cliente cliente);

    @Query("SELECT new org.wispcrm.modelo.facturas.FacturaDto(f.id, CONCAT(c.nombres,' ',c.apellidos) as nombres, "
            + "c.telefono, f.valor, c.diapago, DATEDIFF(CURRENT_TIMESTAMP(), f.fechapago) as mora, c.id) "
            + " FROM Factura f JOIN f.cliente c WHERE f.estado=true ")
    List<FacturaDto> listadoFacturasPendientes();

    @Query(value = "SELECT COALESCE(sum(valor),0) FROM Factura WHERE estado=true AND MONTH(create_at)=MONTH(CURRENT_TIMESTAMP) AND YEAR(create_at)=YEAR(CURRENT_TIMESTAMP)")
    Long totalFacturasPendientesMes();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=true AND MONTH(create_at)=MONTH(CURRENT_TIMESTAMP) AND YEAR(create_at)=YEAR(CURRENT_TIMESTAMP)")
    Long totalCantidadFacturasMes();

    @Query(value = "SELECT COALESCE(sum(valor),0) FROM Factura WHERE estado=false AND MONTH(create_at)=MONTH(CURRENT_TIMESTAMP) AND YEAR(create_at)=YEAR(CURRENT_TIMESTAMP)")
    Long totalFacturasPagadasMes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=true")
    Long totalFacturasPendientesHistorico();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=true")
    Long totalCantidadFacturasHistorico();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=false")
    Long totalFacturasPagadasHistorico();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=false and periodo=MONTH(CURRENT_TIMESTAMP)")
    int existFacturaCreada();

    @Query("SELECT COUNT(f) FROM Factura f WHERE f.cliente.id = :clienteId "
            + "AND MONTH(f.createAt) = MONTH(CURRENT_DATE) "
            + "AND YEAR(f.createAt) = YEAR(CURRENT_DATE)")
    int countFacturaClienteMesActual(@Param("clienteId") int clienteId);

    @Query("SELECT new org.wispcrm.modelo.facturas.FacturaDto(f.id, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.telefono, f.valor, c.diapago, DATEDIFF(CURRENT_TIMESTAMP(), f.fechapago)) "
            + "FROM Factura f JOIN f.cliente c WHERE f.estado=true AND c.id = :clienteId")
    List<FacturaDto> facturasPendientesByCliente(@Param("clienteId") int clienteId);

    @Query("SELECT new org.wispcrm.modelo.facturas.ResumenFacturasDTO(" +
            "COUNT(*), " +
            "SUM(valor)) " +
            "FROM Factura " +
            "WHERE estado = true " +
            "AND MONTH(createAt) = MONTH(CURRENT_DATE)")
    ResumenFacturasDTO getResumenFacturasMes();

    @Query("SELECT new org.wispcrm.modelo.facturas.DeudorDTO("
            + "c.id, CONCAT(c.nombres,' ',c.apellidos), c.telefono, COUNT(f), SUM(f.valor)) "
            + "FROM Factura f JOIN f.cliente c WHERE f.estado=true "
            + "GROUP BY c.id, c.nombres, c.apellidos, c.telefono "
            + "ORDER BY COUNT(f) DESC, SUM(f.valor) DESC")
    List<DeudorDTO> topDeudores();

    @Query("SELECT COUNT(DISTINCT f.cliente.id) FROM Factura f WHERE f.estado=true")
    Long clientesConDeuda();

    @Query("SELECT new org.wispcrm.modelo.facturas.FacturaDto(f.id, CONCAT(c.nombres,' ',c.apellidos), "
            + "c.telefono, f.valor, c.diapago, DATEDIFF(CURRENT_TIMESTAMP(), f.fechapago), c.id) "
            + "FROM Factura f JOIN f.cliente c WHERE f.estado=true "
            + "AND f.fechapago BETWEEN CURRENT_DATE AND :hasta "
            + "ORDER BY f.fechapago ASC")
    List<FacturaDto> facturasPorVencer(@Param("hasta") java.util.Date hasta);
}
