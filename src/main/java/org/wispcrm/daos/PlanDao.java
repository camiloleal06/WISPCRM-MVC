package org.wispcrm.daos;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.planes.Plan;
import org.wispcrm.modelo.planes.PlanDTO;

@Repository
public interface PlanDao extends JpaRepository<Plan, Integer> {
    @Cacheable(value = "lista-planes")
    @Query("SELECT new org.wispcrm.modelo.planes.PlanDTO(p.id, p.nombre) FROM Plan p")
    List<PlanDTO> listaPlanes();
}
