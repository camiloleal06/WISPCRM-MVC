package org.wispcrm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.wispcrm.modelo.Plan;
import org.wispcrm.modelo.PlanDTO;

@Repository
public interface PlanDao extends PagingAndSortingRepository<Plan, Integer> {
    @Query("SELECT new org.wispcrm.modelo.PlanDTO(p.id, p.nombre) FROM Plan p")
    List<PlanDTO> listaPlanes();
}
