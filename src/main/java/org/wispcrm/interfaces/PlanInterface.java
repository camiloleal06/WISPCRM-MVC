package org.wispcrm.interfaces;

import java.util.List;
import org.wispcrm.modelo.planes.Plan;

public interface PlanInterface {
	
	public List<Plan> findAll();

	public void save(Plan plan);

	public Plan findOne(Integer id);

}
