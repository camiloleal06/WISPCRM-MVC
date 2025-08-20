package org.wispcrm.interfaces;

import java.util.List;

import org.wispcrm.modelo.Operario;
import org.wispcrm.modelo.Orden;
import org.wispcrm.modelo.TipoOrden;

public interface OrdenInterface {

    List<Orden> findAll();

    List<TipoOrden> findAllTipoOrden();

    Orden createOrden(Orden orden);

    Orden findOrdenById(Integer id);

    List<Operario> findAllOperario();

}
