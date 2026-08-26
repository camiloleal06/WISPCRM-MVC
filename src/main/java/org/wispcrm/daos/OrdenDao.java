package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wispcrm.modelo.ordenes.EstadoOrden;
import org.wispcrm.modelo.ordenes.Orden;
import java.util.List;

public interface OrdenDao extends JpaRepository<Orden, Integer> {
    List<Orden> findByEstado(EstadoOrden estado);
}
