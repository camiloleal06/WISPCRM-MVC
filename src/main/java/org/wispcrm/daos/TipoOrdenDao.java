package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wispcrm.modelo.ordenes.TipoOrden;

public interface TipoOrdenDao extends JpaRepository<TipoOrden, Integer> {

}
