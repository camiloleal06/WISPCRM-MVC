package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wispcrm.modelo.ordenes.Orden;

public interface OrdenDao extends JpaRepository<Orden, Integer> {

}
