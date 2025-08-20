package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.Factura;
import org.wispcrm.modelo.FacturaDto;

@Repository
public interface InterfaceFacturas extends CrudRepository<Factura, Integer> {

    Factura findFirstFacturaByCliente(Cliente cliente);

    List<Factura> findByCliente(Cliente cliente);

    List<Factura> findFacturaByEstado(boolean f);

    List<FacturaDto> findFacturaDtoByEstado(boolean f);

    @Query("SELECT new org.wispcrm.modelo.FacturaDto(f.id, CONCAT(c.nombres,' ',c.apellidos) as nombres, "
            + "c.telefono,  f.valor, c.diapago, DATEDIFF(CURRENT_TIMESTAMP() , f.fechapago) as mora) "
            + " FROM Factura f JOIN f.cliente c WHERE f.estado=true ")
    List<FacturaDto> listadoFacturasPendientes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=true and periodo=MONTH(CURRENT_TIMESTAMP)")
    public Long totalFacturasPendientesMes();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=true and periodo=MONTH(CURRENT_TIMESTAMP)")
    public Long totalCantidadFacturasMes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=false and periodo=MONTH(CURRENT_TIMESTAMP)")
    public Long totalFacturasPagadasMes();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=true")
    public Long totalFacturasPendientesHistorico();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=true")
    public Long totalCantidadFacturasHistorico();

    @Query(value = "SELECT sum(valor) FROM Factura WHERE estado=false")
    public Long totalFacturasPagadasHistorico();

    @Query(value = "SELECT count(*) FROM Factura WHERE estado=false and periodo=MONTH(CURRENT_TIMESTAMP)")
    public int existFacturaCreada();
}
