package org.wispcrm.interfaces;

import java.util.List;
import org.wispcrm.modelo.pagos.Pago;

public interface InterfacePagoService {
	public List<Pago> findAll();
	public void save(Pago pago);

	public Pago findOne(Integer id);

	public void delete(Integer id);
}
