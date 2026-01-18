package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.pagos.Pago;
import org.wispcrm.modelo.pagos.PagoDTO;

@Repository
public interface InterfacePagos extends JpaRepository<Pago, Integer> {

    @Query(value = "SELECT sum(pago) FROM Pago WHERE MONTH(fechaPago)=MONTH(CURRENT_TIMESTAMP)")
    public Long pagadas();

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoDTO"
            + "(p.id, p.pago , f.id, CONCAT(c.nombres,' ',c.apellidos), p.fechaPago, f.estado) "
            + "FROM Pago p JOIN p.factura f JOIN f.cliente c")
    public List<PagoDTO> lista();

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoDTO(p.id, p.pago , f.id, CONCAT(c.nombres,' ',c.apellidos), p.fechaPago, f.estado) "
            + "FROM Pago p JOIN p.factura f JOIN f.cliente c WHERE f.periodo=MONTH(CURRENT_TIMESTAMP)")
    public Page<PagoDTO> lista(Pageable pageable);

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoDTO(" +
            "p.id, p.pago, f.id, CONCAT(c.nombres,' ',c.apellidos), p.fechaPago, f.estado) " +
            "FROM Pago p JOIN p.factura f JOIN f.cliente c " +
            "ORDER BY p.fechaPago DESC")
    List<PagoDTO> findLastTenPagos(Pageable pageable);
}
