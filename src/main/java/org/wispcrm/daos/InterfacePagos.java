package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.pagos.Pago;
import org.wispcrm.modelo.pagos.PagoDTO;
import org.wispcrm.modelo.pagos.PagoMensualDTO;

@Repository
public interface InterfacePagos extends JpaRepository<Pago, Integer> {

    @Query(value = "SELECT COALESCE(sum(pago), 0) FROM Pago WHERE MONTH(fechaPago)=MONTH(CURRENT_TIMESTAMP) AND YEAR(fechaPago)=YEAR(CURRENT_TIMESTAMP)")
    public Long pagadas();

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoDTO"
            + "(p.id, p.pago , f.id, CONCAT(c.nombres,' ',c.apellidos), p.fechaPago, f.estado) "
            + "FROM Pago p JOIN p.factura f JOIN f.cliente c "
            + "WHERE MONTH(p.fechaPago) = MONTH(CURRENT_DATE) AND YEAR(p.fechaPago) = YEAR(CURRENT_DATE) "
            + "ORDER BY p.fechaPago DESC")
    List<PagoDTO> listaPagosMesActual();

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoDTO(p.id, p.pago , f.id, CONCAT(c.nombres,' ',c.apellidos), p.fechaPago, f.estado) "
            + "FROM Pago p JOIN p.factura f JOIN f.cliente c WHERE f.periodo=MONTH(CURRENT_TIMESTAMP)")
    public Page<PagoDTO> lista(Pageable pageable);

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoDTO(" +
            "p.id, p.pago, f.id, CONCAT(c.nombres,' ',c.apellidos), p.fechaPago, f.estado) " +
            "FROM Pago p JOIN p.factura f JOIN f.cliente c " +
            "ORDER BY p.id DESC")
    List<PagoDTO> findLastTenPagos(Pageable pageable);

    @Query("SELECT new org.wispcrm.modelo.pagos.PagoMensualDTO("
            + "YEAR(p.fechaPago), MONTH(p.fechaPago), SUM(p.pago)) "
            + "FROM Pago p "
            + "WHERE p.fechaPago >= :desde "
            + "GROUP BY YEAR(p.fechaPago), MONTH(p.fechaPago) "
            + "ORDER BY YEAR(p.fechaPago), MONTH(p.fechaPago)")
    List<PagoMensualDTO> pagosPorMes(@Param("desde") java.util.Date desde);
}
