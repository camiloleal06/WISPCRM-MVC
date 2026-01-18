package org.wispcrm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wispcrm.modelo.ordenes.Operario;

public interface OperarioDao extends JpaRepository<Operario, Integer> {

}
